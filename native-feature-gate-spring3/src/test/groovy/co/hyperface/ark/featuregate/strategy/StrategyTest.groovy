package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.model.FlagRule
import spock.lang.Specification

class StrategyTest extends Specification {

    def "GlobalStrategy always returns true"() {
        given:
        def strategy = new GlobalStrategy()
        def rule = new FlagRule(strategy: StrategyType.GLOBAL_ON)

        expect:
        strategy.evaluate(rule, FlagContext.empty(), "any-flag")
        strategy.evaluate(rule, FlagContext.of("u1"), "any-flag")
        strategy.evaluate(rule, FlagContext.withProperties(["tier": "premium"]), "any-flag")
    }

    def "UserWhitelistStrategy returns true only for listed userIds"() {
        given:
        def strategy = new UserWhitelistStrategy()
        def rule = new FlagRule(
            strategy: StrategyType.USER_WHITELIST,
            parameters: '{"userIds": ["user-1", "user-2"]}'
        )

        expect:
        strategy.evaluate(rule, FlagContext.of("user-1"), "flag")
        strategy.evaluate(rule, FlagContext.of("user-2"), "flag")
        !strategy.evaluate(rule, FlagContext.of("user-99"), "flag")
        !strategy.evaluate(rule, FlagContext.empty(), "flag")
    }

    def "UserWhitelistStrategy returns false when parameters is null or empty"() {
        given:
        def strategy = new UserWhitelistStrategy()
        def rule = new FlagRule(strategy: StrategyType.USER_WHITELIST, parameters: params)

        expect:
        !strategy.evaluate(rule, FlagContext.of("user-1"), "flag")

        where:
        params << [null, '{}', '{"userIds": []}']
    }

    def "PercentageRolloutStrategy is deterministic for same userId and flagKey"() {
        given:
        def strategy = new PercentageRolloutStrategy()
        def rule = new FlagRule(strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 50}')
        def context = FlagContext.of("stable-user-id")

        when:
        boolean first = strategy.evaluate(rule, context, "my-flag")
        boolean second = strategy.evaluate(rule, context, "my-flag")
        boolean third = strategy.evaluate(rule, context, "my-flag")

        then:
        first == second
        second == third
    }

    def "PercentageRolloutStrategy returns false when userId is absent"() {
        given:
        def strategy = new PercentageRolloutStrategy()
        def rule = new FlagRule(strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 100}')

        expect:
        !strategy.evaluate(rule, FlagContext.empty(), "my-flag")
    }

    def "PercentageRolloutStrategy returns false for 0% and true for 100%"() {
        given:
        def strategy = new PercentageRolloutStrategy()
        def context = FlagContext.of("any-user")

        expect:
        !strategy.evaluate(new FlagRule(strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 0}'), context, "flag")
        strategy.evaluate(new FlagRule(strategy: StrategyType.PERCENTAGE_ROLLOUT, parameters: '{"percentage": 100}'), context, "flag")
    }
}
