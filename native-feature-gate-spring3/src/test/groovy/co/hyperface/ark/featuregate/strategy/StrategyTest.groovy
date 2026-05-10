package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import spock.lang.Specification

class StrategyTest extends Specification {

    def "GlobalStrategy always returns true"() {
        given:
        def strategy = new GlobalStrategy()
        def flag = new FeatureFlag(flagKey: "any-flag", strategy: StrategyType.GLOBAL_ON)

        expect:
        strategy.evaluate(flag, FlagContext.empty())
        strategy.evaluate(flag, FlagContext.of("u1"))
        strategy.evaluate(flag, FlagContext.withProperties(["tier": "premium"]))
    }

    def "UserWhitelistStrategy returns true only for listed userIds"() {
        given:
        def strategy = new UserWhitelistStrategy()
        def flag = new FeatureFlag(
            flagKey: "flag",
            strategy: StrategyType.USER_WHITELIST,
            parameters: '{"userIds": ["user-1", "user-2"]}'
        )

        expect:
        strategy.evaluate(flag, FlagContext.of("user-1"))
        strategy.evaluate(flag, FlagContext.of("user-2"))
        !strategy.evaluate(flag, FlagContext.of("user-99"))
        !strategy.evaluate(flag, FlagContext.empty())
    }

    def "UserWhitelistStrategy returns false when parameters is null or empty"() {
        given:
        def strategy = new UserWhitelistStrategy()
        def flag = new FeatureFlag(flagKey: "flag", strategy: StrategyType.USER_WHITELIST, parameters: params)

        expect:
        !strategy.evaluate(flag, FlagContext.of("user-1"))

        where:
        params << [null, '{}', '{"userIds": []}']
    }

    def "PercentageRolloutStrategy is deterministic for same userId and flagKey"() {
        given:
        def strategy = new PercentageRolloutStrategy()
        def flag = new FeatureFlag(flagKey: "my-flag", strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 50}')
        def context = FlagContext.of("stable-user-id")

        when:
        boolean first = strategy.evaluate(flag, context)
        boolean second = strategy.evaluate(flag, context)
        boolean third = strategy.evaluate(flag, context)

        then:
        first == second
        second == third
    }

    def "PercentageRolloutStrategy returns false when userId is absent"() {
        given:
        def strategy = new PercentageRolloutStrategy()
        def flag = new FeatureFlag(flagKey: "my-flag", strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 100}')

        expect:
        !strategy.evaluate(flag, FlagContext.empty())
    }

    def "PercentageRolloutStrategy returns false for 0% and true for 100%"() {
        given:
        def strategy = new PercentageRolloutStrategy()
        def context = FlagContext.of("any-user")
        def flag0 = new FeatureFlag(flagKey: "flag", strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 0}')
        def flag100 = new FeatureFlag(flagKey: "flag", strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 100}')

        expect:
        !strategy.evaluate(flag0, context)
        strategy.evaluate(flag100, context)
    }
}
