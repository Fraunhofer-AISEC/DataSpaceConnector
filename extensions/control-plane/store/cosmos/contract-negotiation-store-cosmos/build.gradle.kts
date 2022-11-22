/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
}

dependencies {
    api(project(":spi:control-plane:contract-spi"))
    api(project(":core:common:util"))
    api(project(":extensions:common:azure:azure-cosmos-core"))

    implementation(libs.azure.cosmos)
    implementation(libs.failsafe.core)

    testImplementation(testFixtures(project(":extensions:common:azure:azure-test")))
    testImplementation(testFixtures(project(":extensions:common:azure:azure-cosmos-core")))
    testImplementation(libs.awaitility)

    testImplementation(testFixtures(project(":spi:control-plane:contract-spi")))

}


publishing {
    publications {
        create<MavenPublication>("contract-negotiation-store-cosmos") {
            artifactId = "contract-negotiation-store-cosmos"
            from(components["java"])
        }
    }
}