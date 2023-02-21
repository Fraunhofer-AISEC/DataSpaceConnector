/*
 *  Copyright (c) 2022 Fraunhofer AISEC
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer AISEC
 *
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
   id("org.jetbrains.kotlin.jvm") version "1.8.10"
}

dependencies {
    api(project(":spi:data-plane:data-plane-spi"))
    implementation(project(":core:common:util"))
    implementation(project(":core:data-plane:data-plane-util"))
    implementation(libs.idscp2.core)
    implementation(libs.idscp2.daps.aisec)
    testImplementation(project(":core:common:junit"))
}

publishing {
    publications {
        create<MavenPublication>("data-plane-idscp2") {
            artifactId = "data-plane-idscp2"
            from(components["java"])
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}