import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint)
}

val ktlintEngineVersion = libs.versions.ktlintEngine.get()

allprojects {
    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<KtlintExtension> {
            version.set(ktlintEngineVersion)
            outputToConsole.set(true)
            ignoreFailures.set(false)
            filter {
                include("**/*.kt")
                include("**/*.kts")
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.register("qualityKtlintCheck") {
    group = "verification"
    description = "Runs ktlint checks for the root build scripts and every Gradle subproject."
    dependsOn("ktlintCheck")
    dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
}
