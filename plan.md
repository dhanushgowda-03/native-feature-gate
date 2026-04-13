# native-feature-gate

A native Java/Spring feature flag and config management library for Hyperface microservices.
Replaces the Unleash SDK dependency with a self-hosted, DB-backed evaluation engine.

---

## Problem

`feature-utils` currently wraps the Unleash client SDK which requires an external Unleash server.
We only use 3% of Unleash's capabilities — just `isEnabled(flagName, context)`.
This library owns the full stack: flag storage, evaluation, caching, and admin API.

---

## What We Are Building

| Capability | Description |
|---|---|
| **Feature Flags** | On/off boolean per user / segment / percentage |
| **Remote Config** | Typed runtime values (String, Int, BigDecimal, JSON) without redeployment |
| **Segments** | Reusable named groups of userIds / tenantIds targeted by flags |
| **Tenant-Level Flags** | First-class tenantId in evaluation context |

---

## What We Are NOT Building

- UI dashboard (use a DB client or Retool)
- Push-based cache invalidation (polling is enough)
- A/B experiment analytics
- Flag scheduling (enable at time X)
- SDK for other languages
- Role-based access on admin API
- Real-time WebSocket updates
- Flag dependencies (flag B only if flag A is on)

---

## API Contract (public surface, never changes)

```groovy
// Inject this in your microservice
@Autowired NativeFeatureGate featureGate

// ── FEATURE FLAGS ────────────────────────────────────────────

// 1. Global check — is this flag on for everyone?
featureGate.isEnabled("disbursement-enabled")

// 2. Per-user check — is this flag on for this user?
featureGate.isEnabled("new-rewards-engine", userId)

// 3. Per-tenant check — is this flag on for this tenant?
featureGate.isEnabled("new-dispute-flow", userId, tenantId)

// 4. Context-aware — arbitrary key/value properties
featureGate.isEnabled("premium-benefits", ["tier": "premium", "region": "IN"])

// ── REMOTE CONFIG ────────────────────────────────────────────

// Typed config values, refreshed from DB every 30s
featureGate.getString("payment-gateway-url")           // → "https://..."
featureGate.getInt("max-retry-count")                  // → 3
featureGate.getBigDecimal("late-fee-amount")           // → 250.00
featureGate.getJson("cashback-rate-config")            // → Map / parsed object

// With fallback default if key is missing
featureGate.getString("payment-gateway-url", "https://default-gw.com")
featureGate.getInt("max-retry-count", 3)
```

Feature flags always return `false` on any error.
Remote config always returns the provided default on any error.
**Never throws. Never crashes a request.**

---

## Evaluation Strategies (Feature Flags)

| Strategy | How it works | Example use case |
|---|---|---|
| `GLOBAL_ON` / `GLOBAL_OFF` | Flag on/off for everyone | Kill switch, maintenance mode |
| `USER_WHITELIST` | Enabled for explicit list of userIds | Internal testing, beta users |
| `TENANT_WHITELIST` | Enabled for explicit list of tenantIds | Per-issuer rollouts |
| `SEGMENT` | Enabled for members of a named segment | Reusable groups (beta-testers, premium-cards) |
| `PERCENTAGE_ROLLOUT` | `hash(userId + flagKey) % 100 < threshold` | Gradual rollout to X% of users |

Evaluation is **deterministic and stateless** — same userId always gets same result for percentage rollout.

---

## Module Structure

```
the-ark/
└── native-feature-gate/
    ├── plan.md                              ← this file
    ├── native-feature-gate-core/            ← Phase 1: engine, models, DB, cache
    ├── native-feature-gate-spring3/         ← Phase 2: Spring Boot 3 starter
    ├── native-feature-gate-spring2/         ← Phase 4: Spring Boot 2 starter
    └── native-feature-gate-admin-api/       ← Phase 3: REST CRUD for flags + config
```

### native-feature-gate-core
Pure Groovy, no Spring dependency. Can be unit tested without a Spring context.

```
src/main/groovy/co/hyperface/ark/featuregate/
├── model/
│   ├── FeatureFlag.groovy               ← JPA entity: feature_flags
│   ├── FlagRule.groovy                  ← JPA entity: flag_rules
│   ├── FlagAuditLog.groovy              ← JPA entity: flag_audit_log
│   ├── Segment.groovy                   ← JPA entity: segments
│   ├── SegmentMember.groovy             ← JPA entity: segment_members
│   ├── RemoteConfig.groovy              ← JPA entity: remote_configs
│   └── FlagContext.groovy               ← Value object: userId, tenantId, properties
├── strategy/
│   ├── EvaluationStrategy.groovy        ← Interface: evaluate(flag, context) → boolean
│   ├── GlobalStrategy.groovy            ← GLOBAL_ON / GLOBAL_OFF
│   ├── UserWhitelistStrategy.groovy     ← USER_WHITELIST
│   ├── TenantWhitelistStrategy.groovy   ← TENANT_WHITELIST
│   ├── SegmentStrategy.groovy           ← SEGMENT
│   └── PercentageRolloutStrategy.groovy ← PERCENTAGE_ROLLOUT
├── engine/
│   └── FlagEvaluationEngine.groovy      ← Resolves strategy, runs evaluation
├── cache/
│   ├── FlagCache.groovy                 ← ConcurrentHashMap flags + scheduled refresh
│   └── ConfigCache.groovy               ← ConcurrentHashMap configs + scheduled refresh
└── repository/
    ├── FeatureFlagRepository.groovy
    ├── SegmentRepository.groovy
    ├── SegmentMemberRepository.groovy
    ├── RemoteConfigRepository.groovy
    └── FlagAuditLogRepository.groovy
```

### native-feature-gate-spring3
Spring Boot 3 starter. Depends on core.

```
src/main/groovy/co/hyperface/ark/featuregate/spring/
├── NativeFeatureGate.groovy             ← Public facade — what microservices inject
├── config/
│   ├── FeatureGateAutoConfiguration.groovy
│   └── FeatureGateProperties.groovy
└── resources/
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### native-feature-gate-admin-api
Embed in any service or run standalone.

```
Feature Flag endpoints:
  POST   /feature-flags                    ← create flag
  GET    /feature-flags                    ← list all for environment
  GET    /feature-flags/{key}              ← get one flag with rules
  PUT    /feature-flags/{key}              ← update name, description, rules
  PUT    /feature-flags/{key}/toggle       ← flip enabled true/false
  DELETE /feature-flags/{key}              ← delete flag
  GET    /feature-flags/{key}/audit        ← audit history

Remote Config endpoints:
  POST   /remote-configs                   ← create config entry
  GET    /remote-configs                   ← list all for environment
  GET    /remote-configs/{key}             ← get one config
  PUT    /remote-configs/{key}             ← update value
  DELETE /remote-configs/{key}             ← delete config
  GET    /remote-configs/{key}/audit       ← audit history

Segment endpoints:
  POST   /segments                         ← create segment
  GET    /segments                         ← list all segments
  GET    /segments/{name}                  ← get segment with members
  POST   /segments/{name}/members          ← add members (userIds / tenantIds)
  DELETE /segments/{name}/members          ← remove members
  DELETE /segments/{name}                  ← delete segment
```

---

## Database Schema

```sql
-- ── FEATURE FLAGS ────────────────────────────────────────────

CREATE TABLE feature_flags (
    id              BIGSERIAL PRIMARY KEY,
    flag_key        VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    enabled         BOOLEAN NOT NULL DEFAULT FALSE,
    environment     VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (flag_key, environment)
);

-- Targeting rules attached to a flag
-- strategy: GLOBAL_ON | GLOBAL_OFF | USER_WHITELIST | TENANT_WHITELIST | SEGMENT | PERCENTAGE_ROLLOUT
-- parameters examples:
--   USER_WHITELIST      → {"userIds": ["u1", "u2"]}
--   TENANT_WHITELIST    → {"tenantIds": ["HDFC", "ICICI"]}
--   SEGMENT             → {"segmentName": "beta-testers"}
--   PERCENTAGE_ROLLOUT  → {"percentage": 10}
--   GLOBAL_ON/OFF       → null
CREATE TABLE flag_rules (
    id              BIGSERIAL PRIMARY KEY,
    flag_id         BIGINT NOT NULL REFERENCES feature_flags(id) ON DELETE CASCADE,
    strategy        VARCHAR(50) NOT NULL,
    parameters      JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── SEGMENTS ─────────────────────────────────────────────────

CREATE TABLE segments (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    member_type     VARCHAR(50) NOT NULL,   -- USER | TENANT
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE segment_members (
    id              BIGSERIAL PRIMARY KEY,
    segment_id      BIGINT NOT NULL REFERENCES segments(id) ON DELETE CASCADE,
    member_id       VARCHAR(255) NOT NULL,  -- userId or tenantId
    added_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (segment_id, member_id)
);

-- ── REMOTE CONFIG ─────────────────────────────────────────────

-- value_type: STRING | INTEGER | DECIMAL | JSON
CREATE TABLE remote_configs (
    id              BIGSERIAL PRIMARY KEY,
    config_key      VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    value           TEXT NOT NULL,
    value_type      VARCHAR(20) NOT NULL,
    environment     VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (config_key, environment)
);

-- ── AUDIT LOG (shared) ───────────────────────────────────────

-- entity_type: FEATURE_FLAG | REMOTE_CONFIG | SEGMENT
CREATE TABLE gate_audit_log (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL,
    entity_key      VARCHAR(255) NOT NULL,
    environment     VARCHAR(50),
    changed_by      VARCHAR(255),
    change_type     VARCHAR(50) NOT NULL,   -- CREATED | UPDATED | TOGGLED | DELETED
    old_value       JSONB,
    new_value       JSONB,
    changed_at      TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## Caching Strategy

Both feature flags and remote configs use the same pattern:

- In-memory `ConcurrentHashMap` keyed by `key:environment`
- `@Scheduled` background thread refreshes all entries every 30s from DB
- On cache miss → fetch from DB directly, warm cache entry
- If DB is unreachable during refresh → serve stale cache, log warning
- On application startup → eager load all entries for configured environment

Segments are loaded as part of flag cache (embedded in flag rule resolution).

No Redis dependency — avoids infra coupling. 30s staleness is acceptable for both flags and config.

---

## Configuration (application.yml in consuming service)

```yaml
ark:
  featureGate:
    environment: production              # which env's data to load
    cacheRefreshIntervalSec: 30          # how often to refresh from DB
    # points to shared flags DB or service's own DB
    datasource:
      url: jdbc:postgresql://host/flags-db
      username: ${DB_USER}
      password: ${DB_PASS}
```

---

## How Microservices Use It

**1. Add dependency (build.gradle):**
```groovy
implementation 'co.hyperface.ark:native-feature-gate-spring3:1.0.0'
```

**2. Configure (application.yml):**
```yaml
ark:
  featureGate:
    environment: production
    cacheRefreshIntervalSec: 30
```

**3. Feature flag examples:**
```groovy
@Autowired NativeFeatureGate featureGate

// Kill switch
if (!featureGate.isEnabled("disbursement-enabled")) {
    return Response.error("Disbursements temporarily paused")
}

// Per-user rollout (10% of users)
if (featureGate.isEnabled("new-rewards-engine", userId)) {
    return newRewardsEngine.compute(cardId)
}

// Per-tenant rollout
if (featureGate.isEnabled("new-dispute-flow", userId, tenantId)) {
    return newDisputeService.raise(req)
}

// Segment-based (beta-testers segment)
if (featureGate.isEnabled("new-statement-ui", userId)) {
    return newStatementService.generate(accountId)
}
```

**4. Remote config examples:**
```groovy
// Fee and limit values — no redeployment needed to change
BigDecimal lateFee = featureGate.getBigDecimal("late-fee-amount", 250.00)
int maxRetries     = featureGate.getInt("payment-max-retries", 3)
String gatewayUrl  = featureGate.getString("payment-gateway-url", "https://default-gw.com")

// Complex config as JSON → parse to your DTO
Map rateCard = featureGate.getJson("cashback-rate-config")
// {"gold": 2.0, "platinum": 3.5, "titanium": 5.0}
BigDecimal rate = rateCard[cardTier] as BigDecimal
```

**5. Segment management (via admin API):**
```
# Create a beta-testers segment
POST /segments
{ "name": "beta-testers", "memberType": "USER", "description": "Internal QA + early access users" }

# Add members
POST /segments/beta-testers/members
{ "memberIds": ["u001", "u002", "u003"] }

# Now point any flag at this segment — no userId list duplication
PUT /feature-flags/new-statement-ui
{ "rules": [{ "strategy": "SEGMENT", "parameters": { "segmentName": "beta-testers" } }] }
```

---

## Build Phases

### Phase 1 — Core (Day 1-2)
- [ ] Gradle project setup for `native-feature-gate-core`
- [ ] DB entities: `FeatureFlag`, `FlagRule`, `Segment`, `SegmentMember`, `RemoteConfig`, `GateAuditLog`
- [ ] JPA repositories for all entities
- [ ] `FlagContext` value object (userId, tenantId, properties map)
- [ ] `EvaluationStrategy` interface + 5 implementations
- [ ] `FlagEvaluationEngine` — resolves strategy, runs evaluation
- [ ] `FlagCache` + `ConfigCache` — ConcurrentHashMap + scheduled refresh
- [ ] Unit tests for each strategy

### Phase 2 — Spring Starter (Day 3)
- [ ] Gradle project setup for `native-feature-gate-spring3`
- [ ] `FeatureGateProperties` — bind `ark.featureGate.*`
- [ ] `FeatureGateAutoConfiguration` — wire all beans
- [ ] `NativeFeatureGate` — public facade: all `isEnabled` + all `get*` overloads
- [ ] Integration test — Spring context loads, flag and config evaluate correctly

### Phase 3 — Admin API (Day 4)
- [ ] Gradle project setup for `native-feature-gate-admin-api`
- [ ] Feature flag CRUD controller + DTOs
- [ ] Remote config CRUD controller + DTOs
- [ ] Segment CRUD + member management controller + DTOs
- [ ] Audit log written on every mutating operation

### Phase 4 — Spring 2 + Publish (Day 5)
- [ ] `native-feature-gate-spring2` variant (same code, Spring Boot 2 deps)
- [ ] `config.yml` for each module (for the-ark CI pipeline)
- [ ] Publish to CodeArtifact
- [ ] Update the-ark README

---

## Decision Log

| Decision | Choice | Reason |
|---|---|---|
| API style | Programmatic only (`isEnabled`, `get*`) | Annotation swaps full beans — too coarse for fintech logic |
| Cache backend | In-memory only | No Redis infra dependency; 30s staleness acceptable |
| Cache refresh | Polling (30s) | Simpler than push; flag/config changes are not time-critical |
| DB | Consumer's own DB or shared flags DB | Flexible — service decides |
| Spring2/3 | Both variants | Matches the-ark convention, some services still on Spring 2 |
| Flag strategies | 5 (Global, UserWhitelist, TenantWhitelist, Segment, Percentage) | Covers all real targeting patterns |
| Segments | Separate table, not embedded in flag rules | Reusable across flags, avoids userId list duplication |
| Remote config | Same caching pattern as flags | Minimal extra code, huge operational value |
| Error behavior | flags → `false`, config → caller's default | Never crash a request because of a flag/config lookup |
| Audit log | Single shared table (`gate_audit_log`) | Simpler than per-entity audit tables |
