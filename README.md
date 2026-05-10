# native-feature-gate

Self-hosted feature flag library for Spring Boot microservices. Embeds directly — no external server required. Flags are stored in the microservice's own database and managed via REST endpoints exposed on that service.

---

## Features

- `isEnabled(flagKey)` — simple on/off check
- `isEnabled(flagKey, userId)` — user-scoped evaluation
- `isEnabled(flagKey, userId, tenantId)` — tenant-scoped evaluation
- `isEnabled(flagKey, Map<String, String> properties)` — arbitrary context
- Three evaluation strategies: `GLOBAL_ON`, `USER_WHITELIST`, `PERCENTAGE_ROLLOUT`
- Stale-while-revalidate in-memory cache (configurable TTL)
- Full audit log on every flag mutation
- Admin REST API for CRUD on flags (exposed on the consuming microservice)
- Micrometer metrics (SigNoz / OTLP compatible)
- Fail-safe: never throws, always returns `false` on error

---

## Setup

### 1. Add dependency

**Spring Boot 2:**
```groovy
implementation 'co.hyperface.ark:native-feature-gate-spring2:1.0.0'
```

**Spring Boot 3:**
```groovy
implementation 'co.hyperface.ark:native-feature-gate-spring3:1.0.0'
```

### 2. Run schema

The library ships `schema.sql`. Spring Boot will auto-run it on startup if you have:

```yaml
spring:
  sql:
    init:
      mode: always
```

This creates `feature_flags` and `gate_audit_log` tables in your service's DB.

> Requires MySQL 8.0+. Schema uses `CREATE INDEX IF NOT EXISTS` which is not supported on MySQL 5.7.

### 3. Configure (optional)

```yaml
ark:
  feature-gate:
    cache-ttl-ms: 120000      # default 2 min — how long a flag stays fresh
    max-stale-ms: 3600000     # default 1 hr — max age before blocking DB fetch
```

`max-stale-ms` must be greater than `cache-ttl-ms` — app will fail fast on startup if misconfigured.

### 4. Secure admin endpoints

The library exposes admin endpoints at `/feature-gate/**` on your microservice. Add to your Spring Security config:

```java
// Spring Boot 3
.requestMatchers("/feature-gate/**").authenticated()

// Spring Boot 2
.antMatchers("/feature-gate/**").authenticated()
```

Without this, the endpoints inherit your service's default security rules.

---

## Usage

Inject `NativeFeatureGate` anywhere in your service:

```groovy
@Autowired
NativeFeatureGate featureGate

// Simple check
featureGate.isEnabled("new-disbursement-flow")

// User-scoped
featureGate.isEnabled("new-rewards-engine", userId)

// Tenant-scoped
featureGate.isEnabled("premium-benefits", userId, tenantId)

// Arbitrary properties
featureGate.isEnabled("beta-feature", ["tier": "premium", "region": "IN"])
```

Always returns `false` if flag doesn't exist or on any error.

---

## Admin Endpoints

All endpoints are on the consuming microservice's host/port.

### Create flag
```
POST /feature-gate/flags
```
```json
{
  "flagKey": "new-disbursement-flow",
  "name": "New Disbursement Flow",
  "description": "Enables redesigned disbursement logic",
  "strategy": "PERCENTAGE_ROLLOUT",
  "parameters": "{\"percentage\": 10}"
}
```

### List all flags
```
GET /feature-gate/flags
```

### Get flag
```
GET /feature-gate/flags/{flagKey}
```

### Update flag
```
PUT /feature-gate/flags/{flagKey}
```
```json
{
  "name": "New Disbursement Flow",
  "strategy": "USER_WHITELIST",
  "parameters": "{\"userIds\": [\"user-123\", \"user-456\"]}"
}
```

### Toggle on/off
```
PUT /feature-gate/flags/{flagKey}/toggle
```

### Delete flag
```
DELETE /feature-gate/flags/{flagKey}
```

### Audit log
```
GET /feature-gate/flags/{flagKey}/audit?limit=20
```

---

## Strategies

| Strategy | Parameters | Behaviour |
|---|---|---|
| `GLOBAL_ON` | none | Flag enabled for everyone |
| `USER_WHITELIST` | `{"userIds": ["id1", "id2"]}` | Enabled only for listed user IDs |
| `PERCENTAGE_ROLLOUT` | `{"percentage": 10}` | Enabled for N% of users (deterministic by userId) |

Omit `strategy` on create → treated as `GLOBAL_ON`.

---

## Metrics (SigNoz)

Add to consuming service:

```groovy
implementation 'io.micrometer:micrometer-registry-otlp'
```

```yaml
management:
  otlp:
    metrics:
      export:
        url: http://<signoz-otel-collector>:4318/v1/metrics
```

Metrics emitted:

| Metric | Tags | What it shows |
|---|---|---|
| `feature.gate.cache` | `result=hit\|stale\|miss` | Cache efficiency |
| `feature.gate.strategy` | `type=GLOBAL_ON\|USER_WHITELIST\|PERCENTAGE_ROLLOUT\|none\|unknown` | Strategy usage |
| `feature.gate.eval` | `flagKey=<key>` | Eval latency (Timer) |
| `feature.gate.eval.result` | `flagKey=<key>`, `result=true\|false\|error` | Per-flag enable rate |

