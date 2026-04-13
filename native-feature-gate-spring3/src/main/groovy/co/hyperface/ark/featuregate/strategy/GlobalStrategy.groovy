package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.model.FlagRule

class GlobalStrategy implements EvaluationStrategy {

    @Override
    boolean evaluate(FlagRule rule, FlagContext context, String flagKey) {
        return true
    }
}
