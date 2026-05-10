package co.hyperface.ark.featuregate.engine

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.strategy.StrategyType
import spock.lang.Specification

class FlagEvaluationEngineTest extends Specification {

    FlagEvaluationEngine engine = new FlagEvaluationEngine()

    def "disabled flag always returns false"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: false, strategy: StrategyType.GLOBAL_ON)

        expect:
        !engine.evaluate(flag, FlagContext.empty())
    }

    def "enabled flag with no strategy returns true for everyone"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: true)

        expect:
        engine.evaluate(flag, FlagContext.empty())
        engine.evaluate(flag, FlagContext.of("any-user"))
    }

    def "enabled flag with GLOBAL_ON strategy returns true"() {
        given:
        def flag = new FeatureFlag(flagKey: "flag", enabled: true, strategy: StrategyType.GLOBAL_ON)

        expect:
        engine.evaluate(flag, FlagContext.empty())
    }

    def "enabled flag with USER_WHITELIST returns true only for whitelisted users"() {
        given:
        def flag = new FeatureFlag(
            flagKey: "flag",
            enabled: true,
            strategy: StrategyType.USER_WHITELIST,
            parameters: '{"userIds": ["allowed-user"]}'
        )

        expect:
        engine.evaluate(flag, FlagContext.of("allowed-user"))
        !engine.evaluate(flag, FlagContext.of("other-user"))
        !engine.evaluate(flag, FlagContext.empty())
    }
}
