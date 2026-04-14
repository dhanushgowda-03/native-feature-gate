# Native Feature Gate

A lightweight **Spring Boot starter** for feature flags backed by your own database.

No external services. No infra dependencies. Just drop it into your microservice and start gating features.

---

## 🚀 Overview

`native-feature-gate` provides a simple and reliable way to control feature rollouts using:

* In-memory cache for fast evaluation
* Database-backed configuration
* Deterministic rollout strategies
* Fail-safe behavior (never breaks your service)

---

## 🧠 How It Works (High Level)

```
Your Service
    │
    ▼
NativeFeatureGate.isEnabled("my-flag", userId)
    │
    ▼
Flag Cache (in-memory, refresh every 30s)
    │  on miss
    ▼
Database (feature_flags + flag_rules)
    │
    ▼
Flag Evaluation Engine
    │
    ▼
true / false  (fail-safe → false)
```

---

## 🔁 Core Evaluation Flow

```
isEnabled(flagKey, userId)
        │
        ▼
Fetch flag from cache
        │
        ├── Flag NOT FOUND → false
        ├── Flag DISABLED → false
        ├── No rules → true (GLOBAL ON)
        ▼
Evaluate rules (OR logic)
        ├── USER_WHITELIST match → true
        ├── PERCENTAGE match → true
        └── No match → false
```

---

## 🏗️ Architecture

### 1. Storage Layer

Three tables power the system:

#### `feature_flags`

* Stores flag metadata
* Whether the flag is enabled
* Environment scoping

#### `flag_rules`

* Rules associated with a flag
* Strategy type + config (JSON)

#### `gate_audit_log`

* Immutable history of all changes
* Tracks who changed what and when

---

### 2. Evaluation Layer

When `isEnabled()` is called:

* Disabled flag → returns `false`
* Enabled flag + no rules → returns `true`
* Enabled flag + rules → evaluates rules using **OR logic**

---

### 3. Rule Strategies

| Strategy             | Description                         |
| -------------------- | ----------------------------------- |
| `GLOBAL_ON`          | Enabled for everyone                |
| `USER_WHITELIST`     | Enabled only for specific users     |
| `PERCENTAGE_ROLLOUT` | Deterministic rollout using hashing |

#### Percentage Rollout Logic

```
hash(userId:flagKey) % 100 < rolloutPercentage
```

* Same user always gets the same result
* No random flickering

---

## ⚡ Caching Strategy

* All flags are stored in a `ConcurrentHashMap`
* Cache refreshes every **30 seconds**
* Atomic replacement (no partial state)

```
Every 30s:
    Fetch flags + rules from DB
    Replace cache
```

---

## 🛠️ Admin API

Built-in REST endpoints:

```
/feature-gate/flags
```

Supports:

* Create flag
* Update flag
* Toggle flag
* Delete flag

Every change is:

* Persisted in DB
* Logged in `gate_audit_log`

---

## 🔐 Key Design Principles

### Cache-First

* Evaluation happens in-memory
* DB is not hit on hot path

### Fail-Safe

* Any exception → returns `false`
* Never breaks caller service

### Deterministic Behavior

* Percentage rollout is stable per user

### Environment Scoped

* Flags are isolated per environment (`dev`, `prod`, etc.)

### Zero External Dependency

* No Redis
* No Kafka
* No third-party flag service

---

## 📦 Usage

### 1. Add Dependency

```xml
<dependency>
  <groupId>your.group</groupId>
  <artifactId>native-feature-gate</artifactId>
</dependency>
```

### 2. Configure Environment

```yaml
ark:
  feature-gate:
    environment: prod
```

### 3. Use in Code

```java
boolean enabled = NativeFeatureGate.isEnabled("my-flag", userId);

if (enabled) {
    // new logic
} else {
    // old logic
}
```

---

## 🧩 Design Trade-offs

| Decision              | Trade-off                             |
| --------------------- | ------------------------------------- |
| 30s cache refresh     | Slight delay in propagation           |
| No push-based updates | Simpler system, no infra              |
| Fail-safe = false     | Safer, but may hide misconfigurations |

---

## 📌 Summary

`native-feature-gate` is designed for:

* Simplicity
* Reliability
* Zero external dependencies
* High performance (in-memory evaluation)

Perfect for teams that want **feature flags without operating another system**.

---

## 🚧 Future Enhancements 

* Real-time updates (push-based)
* Segment-based targeting
* Admin UI dashboard
* Metrics + observability


