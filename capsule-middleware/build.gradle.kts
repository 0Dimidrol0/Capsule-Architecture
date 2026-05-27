plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":capsule-core"))
    implementation(libs.coroutines.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit4)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
