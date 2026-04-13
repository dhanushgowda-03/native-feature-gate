# Graph Report - .  (2026-04-13)

## Corpus Check
- Corpus is ~1,912 words - fits in a single context window. You may not need a graph.

## Summary
- 67 nodes · 101 edges · 13 communities detected
- Extraction: 78% EXTRACTED · 22% INFERRED · 0% AMBIGUOUS · INFERRED: 22 edges (avg confidence: 0.82)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Flag Evaluation Engine (Spring3)|Flag Evaluation Engine (Spring3)]]
- [[_COMMUNITY_Flag Data Model & Admin API|Flag Data Model & Admin API]]
- [[_COMMUNITY_Spring2 Core Entities|Spring2 Core Entities]]
- [[_COMMUNITY_Spring2 Admin & DTOs|Spring2 Admin & DTOs]]
- [[_COMMUNITY_Spring2 Strategy Layer|Spring2 Strategy Layer]]
- [[_COMMUNITY_Spring3 Strategy Layer|Spring3 Strategy Layer]]
- [[_COMMUNITY_Project Design Intent|Project Design Intent]]
- [[_COMMUNITY_Cache & Config|Cache & Config]]
- [[_COMMUNITY_Spring3 Build Setup|Spring3 Build Setup]]
- [[_COMMUNITY_Spring3 Auto-Configuration|Spring3 Auto-Configuration]]
- [[_COMMUNITY_Spring2 Audit Log|Spring2 Audit Log]]
- [[_COMMUNITY_Spring2 Build Setup|Spring2 Build Setup]]
- [[_COMMUNITY_Runtime Config|Runtime Config]]

## God Nodes (most connected - your core abstractions)
1. `Native Feature Gate Plan` - 10 edges
2. `FlagAdminController REST Controller` - 9 edges
3. `FeatureFlag Entity` - 7 edges
4. `StrategyType Enum` - 7 edges
5. `FlagCache Component` - 6 edges
6. `FlagRule Entity` - 6 edges
7. `FlagEvaluationEngine` - 6 edges
8. `FeatureGateProperties (Spring2)` - 6 edges
9. `FlagEvaluationEngine (Spring2)` - 6 edges
10. `StrategyTest` - 5 edges

## Surprising Connections (you probably didn't know these)
- `Rationale: In-Memory Cache Over Redis` --rationale_for--> `FeatureGateProperties (Spring2)`  [INFERRED]
  plan.md → native-feature-gate-spring2/src/main/groovy/co/hyperface/ark/featuregate/config/FeatureGateProperties.groovy
- `Spring3 Database Schema SQL` --implements--> `Native Feature Gate Plan`  [INFERRED]
  native-feature-gate-spring3/src/main/resources/schema.sql → plan.md
- `FlagAdminController (Spring2)` --implements--> `Rationale: Single Shared Audit Log Table`  [INFERRED]
  native-feature-gate-spring2/src/main/groovy/co/hyperface/ark/featuregate/admin/FlagAdminController.groovy → plan.md
- `FlagAdminController (Spring2)` --implements--> `Rationale: Silent Error Behavior`  [INFERRED]
  native-feature-gate-spring2/src/main/groovy/co/hyperface/ark/featuregate/admin/FlagAdminController.groovy → plan.md
- `Rationale: Polling Cache Refresh` --rationale_for--> `FeatureGateProperties (Spring2)`  [INFERRED]
  plan.md → native-feature-gate-spring2/src/main/groovy/co/hyperface/ark/featuregate/config/FeatureGateProperties.groovy

## Hyperedges (group relationships)
- **Feature Flag Evaluation Pipeline** — nativefeaturegate_service, flagcache_component, flagevaluationengine_engine [EXTRACTED 1.00]
- **Feature Flag Domain Data Model** — featureflag_model, flagrule_model, strategytype_enum [EXTRACTED 1.00]
- **Admin CRUD and Audit Flow** — flagadmincontroller_controller, flagauditlogrepository_repo, flagauditlog_model [EXTRACTED 1.00]
- **Spring3 Strategy Pattern: EvaluationStrategy Implementations** — s3_EvaluationStrategy, s3_GlobalStrategy, s3_PercentageRolloutStrategy, s3_UserWhitelistStrategy [EXTRACTED 1.00]
- **Spring3 Engine dispatches strategies via StrategyType** — s3_FlagEvaluationEngine, s3_StrategyType, s3_EvaluationStrategy [EXTRACTED 1.00]
- **Spring2 Flag Evaluation Pipeline** — s2_NativeFeatureGate, s2_FlagCache, s2_FlagEvaluationEngine [EXTRACTED 1.00]
- **Spring2 Evaluation Strategy Implementations** — spring2_globalstrategy, spring2_userwhiteliststrategy, spring2_percentagerolloutstrategy [EXTRACTED 1.00]
- **Spring2 Admin Controller DTO Cluster** — spring2_flagadmincontroller, spring2_createflagrequest, spring2_flagresponse [EXTRACTED 1.00]
- **Plan Cache Strategy Decisions** — plan_inmemorycache, plan_pollingcache, spring2_featuregateproperties [INFERRED 0.80]

## Communities

### Community 0 - "Flag Evaluation Engine (Spring3)"
Cohesion: 0.3
Nodes (12): FlagContext Model, FlagEvaluationEngine, FlagEvaluationEngineTest, FlagRule Entity, GlobalStrategy, NativeFeatureGate Service, PercentageRolloutStrategy, SpringContextTest (+4 more)

### Community 1 - "Flag Data Model & Admin API"
Cohesion: 0.33
Nodes (10): CreateFlagRequest DTO, FeatureFlag Entity, FeatureFlagRepository, FeatureGateProperties Config, FlagAdminController REST Controller, FlagAuditLog Entity, FlagAuditLogRepository, FlagCache Component (+2 more)

### Community 2 - "Spring2 Core Entities"
Cohesion: 0.38
Nodes (7): FeatureFlag (Spring2), FeatureFlagRepository (Spring2), FlagCache (Spring2), FlagContext (Spring2), FlagRule (Spring2), NativeFeatureGate (Spring2), Spring2 build.gradle

### Community 3 - "Spring2 Admin & DTOs"
Cohesion: 0.33
Nodes (7): Rationale: Silent Error Behavior, Rationale: Single Shared Audit Log Table, CreateFlagRequest (Spring2), FlagAdminController (Spring2), FlagResponse (Spring2), FlagRuleRequest (Spring2), StrategyType Enum (Spring2)

### Community 4 - "Spring2 Strategy Layer"
Cohesion: 0.57
Nodes (7): Concept: Deterministic Percentage Rollout, Concept: Five Evaluation Strategies, EvaluationStrategy Interface (Spring2), FlagEvaluationEngine (Spring2), GlobalStrategy (Spring2), PercentageRolloutStrategy (Spring2), UserWhitelistStrategy (Spring2)

### Community 5 - "Spring3 Strategy Layer"
Cohesion: 0.73
Nodes (6): EvaluationStrategy Interface (Spring3), FlagEvaluationEngine (Spring3), GlobalStrategy (Spring3), PercentageRolloutStrategy (Spring3), StrategyType Enum (Spring3), UserWhitelistStrategy (Spring3)

### Community 6 - "Project Design Intent"
Cohesion: 0.4
Nodes (5): Rationale: In-Memory Cache Over Redis, Native Feature Gate Plan, Concept: Remote Config (Typed Runtime Values), Rationale: Replace Unleash SDK, Concept: Reusable Segments

### Community 7 - "Cache & Config"
Cohesion: 0.4
Nodes (5): Rationale: Polling Cache Refresh, FeatureGateConfig (Spring2), FeatureGateProperties (Spring2), Spring3 Database Schema SQL, Spring3 Test Application Config

### Community 8 - "Spring3 Build Setup"
Cohesion: 1.0
Nodes (2): Spring3 Module Build Config, native-feature-gate-spring3 Project

### Community 9 - "Spring3 Auto-Configuration"
Cohesion: 1.0
Nodes (2): FeatureGateConfig (Spring3), FeatureGateProperties (Spring3)

### Community 10 - "Spring2 Audit Log"
Cohesion: 1.0
Nodes (2): FlagAuditLog (Spring2), FlagAuditLogRepository (Spring2)

### Community 11 - "Spring2 Build Setup"
Cohesion: 1.0
Nodes (1): Spring2 settings.gradle

### Community 12 - "Runtime Config"
Cohesion: 1.0
Nodes (1): Spring3 Build Config (config.yml)

## Knowledge Gaps
- **17 isolated node(s):** `native-feature-gate-spring3 Project`, `Spring3 Module Build Config`, `TestApplication Spring Boot Test App`, `FeatureGateConfig (Spring3)`, `FeatureGateProperties (Spring3)` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Spring3 Build Setup`** (2 nodes): `Spring3 Module Build Config`, `native-feature-gate-spring3 Project`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Spring3 Auto-Configuration`** (2 nodes): `FeatureGateConfig (Spring3)`, `FeatureGateProperties (Spring3)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Spring2 Audit Log`** (2 nodes): `FlagAuditLog (Spring2)`, `FlagAuditLogRepository (Spring2)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Spring2 Build Setup`** (1 nodes): `Spring2 settings.gradle`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Runtime Config`** (1 nodes): `Spring3 Build Config (config.yml)`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Native Feature Gate Plan` connect `Project Design Intent` to `Spring2 Admin & DTOs`, `Spring2 Strategy Layer`, `Cache & Config`?**
  _High betweenness centrality (0.063) - this node is a cross-community bridge._
- **Why does `FlagAdminController REST Controller` connect `Flag Data Model & Admin API` to `Flag Evaluation Engine (Spring3)`?**
  _High betweenness centrality (0.033) - this node is a cross-community bridge._
- **Why does `Concept: Five Evaluation Strategies` connect `Spring2 Strategy Layer` to `Project Design Intent`?**
  _High betweenness centrality (0.029) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `StrategyType Enum` (e.g. with `GlobalStrategy` and `UserWhitelistStrategy`) actually correct?**
  _`StrategyType Enum` has 3 INFERRED edges - model-reasoned connections that need verification._
- **What connects `native-feature-gate-spring3 Project`, `Spring3 Module Build Config`, `TestApplication Spring Boot Test App` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._