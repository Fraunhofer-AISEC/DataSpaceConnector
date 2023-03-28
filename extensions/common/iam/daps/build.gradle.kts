/*
 *
 */

plugins {
    `java-library`
}

dependencies {
    api(project(":spi:common:core-spi"))
    api(project(":spi:common:oauth2-spi"))

    testImplementation(project(":extensions:common:vault:vault-filesystem"))
    testImplementation(project(":core:common:junit"))

    testImplementation(libs.okhttp)

    implementation("de.fhg.aisec.ids:idscp2-daps-aisec:0.14.1")
    // replace above line by the following, when idscp2 is included in GradlePlugins project
    //implementation(libs.idscp2.daps.aisec)
}

publishing {
    publications {
        create<MavenPublication>("daps") {
            artifactId = "daps"
            from(components["java"])
        }
    }
}
