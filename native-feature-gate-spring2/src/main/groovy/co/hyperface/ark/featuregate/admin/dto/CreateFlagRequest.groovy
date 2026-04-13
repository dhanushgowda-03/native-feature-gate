package co.hyperface.ark.featuregate.admin.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import javax.validation.Valid
import javax.validation.constraints.NotBlank

@JsonIgnoreProperties(ignoreUnknown = true)
class CreateFlagRequest {

    @NotBlank
    String flagKey

    @NotBlank
    String name

    String description

    boolean enabled = false

    @Valid
    List<FlagRuleRequest> rules = []
}
