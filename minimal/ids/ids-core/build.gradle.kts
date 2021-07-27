/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

val infoModelVersion: String by project
val rsApi: String by project

plugins {
    `java-library`
}


dependencies {
    api(project(":core:spi"))
    api(project(":common:util"))
    api(project(":minimal:ids:ids-spi"))

    api("de.fraunhofer.iais.eis.ids.infomodel:java:${infoModelVersion}")

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
    implementation(project(":core:policy:policy-engine"))
}


publishing {
    publications {
        create<MavenPublication>("ids-core") {
            artifactId = "edc.ids-core"
            from(components["java"])
        }
    }
}