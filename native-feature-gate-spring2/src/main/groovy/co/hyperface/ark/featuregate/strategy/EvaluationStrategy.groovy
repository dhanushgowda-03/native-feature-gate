package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext

interface EvaluationStrategy {
    boolean evaluate(FeatureFlag flag, FlagContext context)
}
