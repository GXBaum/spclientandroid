package de.rafaelbeckmann.hvkclient.features.other.domain

data class FeatureFlag(
    val featureFlags: Map<String, Boolean>
)