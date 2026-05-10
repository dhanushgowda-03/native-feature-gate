package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext

class GlobalStrategy implements EvaluationStrategy {

    @Override
    boolean evaluate(FeatureFlag flag, FlagContext context) {
        return true
    }
}
