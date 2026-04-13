package co.hyperface.ark.featuregate

import co.hyperface.ark.featuregate.cache.FlagCache
import co.hyperface.ark.featuregate.engine.FlagEvaluationEngine
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import static org.junit.jupiter.api.Assertions.*

@SpringBootTest(classes = TestApplication)
class SpringContextTest {

    @Autowired NativeFeatureGate featureGate
    @Autowired FlagCache flagCache
    @Autowired FlagEvaluationEngine engine

    @Test
    void "spring context loads and all core beans are present"() {
        assertNotNull(featureGate)
        assertNotNull(flagCache)
        assertNotNull(engine)
    }

    @Test
    void "isEnabled returns false for unknown flag key"() {
        assertFalse(featureGate.isEnabled("non-existent-flag"))
        assertFalse(featureGate.isEnabled("non-existent-flag", "some-user"))
        assertFalse(featureGate.isEnabled("non-existent-flag", ["region": "IN"]))
    }
}
