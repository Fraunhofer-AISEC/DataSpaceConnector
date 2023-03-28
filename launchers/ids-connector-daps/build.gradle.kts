/*
 *
 */

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":core:control-plane:control-plane-core"))

    implementation(project(":data-protocols:ids"))

    implementation(project(":extensions:common:configuration:configuration-filesystem"))
    implementation(project(":extensions:common:vault:vault-filesystem"))

    implementation(project(":extensions:common:iam:daps"))

    implementation(project(":extensions:control-plane:api:management-api"))

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("dataspace-connector-daps.jar")
}
