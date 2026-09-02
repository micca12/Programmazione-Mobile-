// Top-level build file: qui si dichiarano i plugin, si applicano nei moduli.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp.plugin) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
