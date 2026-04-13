package co.hyperface.ark.featuregate.engine

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.model.FlagRule
import co.hyperface.ark.featuregate.strategy.StrategyType
import spock.lang.Specification

class FlagEvaluationEngineTest extends Specification {

    FlagEvaluationEngine engine = new FlagEvaluationEngine()

    def "disabled flag always returns false regardless of rules"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: false, rules: [
            new FlagRule(strategy: StrategyType.GLOBAL_ON)
        ])

        expect:
        !engine.evaluate(flag, FlagContext.empty())
    }

    def "enabled flag with no rules returns true for everyone"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: true, rules: [])

        expect:
        engine.evaluate(flag, FlagContext.empty())
        engine.evaluate(flag, FlagContext.of("any-user"))
    }

    def "enabled flag with GLOBAL_ON rule returns true"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: true, rules: [
            new FlagRule(strategy: StrategyType.GLOBAL_ON)
        ])

        expect:
        engine.evaluate(flag, FlagContext.empty())
    }

    def "enabled flag with USER_WHITELIST returns true only for whitelisted users"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: true, rules: [
            new FlagRule(
                strategy: StrategyType.USER_WHITELIST,
                parameters: '{"userIds": ["allowed-user"]}'
            )
        ])

        expect:
        engine.evaluate(flag, FlagContext.of("allowed-user"))
        !engine.evaluate(flag, FlagContext.of("other-user"))
        !engine.evaluate(flag, FlagContext.empty())
    }
}
