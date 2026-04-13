package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.model.FlagRule

interface EvaluationStrategy {
    boolean evaluate(FlagRule rule, FlagContext context, String flagKey)
}
