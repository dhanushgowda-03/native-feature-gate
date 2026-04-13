package co.hyperface.ark.featuregate.model

import groovy.transform.Canonical

@Canonical
class FlagContext {

    String userId
    String tenantId
    Map<String, String> properties = [:]

    static FlagContext empty() {
        new FlagContext()
    }

    static FlagContext of(String userId) {
        new FlagContext(userId: userId)
    }

    static FlagContext of(String userId, String tenantId) {
        new FlagContext(userId: userId, tenantId: tenantId)
    }

    static FlagContext withProperties(Map<String, String> props) {
        new FlagContext(properties: props ?: [:])
    }
}
