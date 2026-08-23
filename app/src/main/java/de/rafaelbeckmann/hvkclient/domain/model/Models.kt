package de.rafaelbeckmann.hvkclient.domain.model

data class FeatureFlag(
    val featureFlags: Map<String, Boolean>
)