import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    jacoco
    kotlin("jvm") version "1.7.10"
    application

    id("org.flywaydb.flyway") version "9.1.3"

    kotlin("plugin.serialization").version("1.7.10")
}

group = "interview"
version = "1.0-SNAPSHOT"

val ktorVersion: String by project
val arrowVersion: String by project
val kotlinVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("io.arrow-kt:arrow-core:$arrowVersion")

    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.postgresql:postgresql:42.4.1")
    implementation("org.flywaydb:flyway-core:9.1.3")
    implementation ("com.zaxxer:HikariCP:5.0.1")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.5")
    testImplementation("org.skyscreamer:jsonassert:1.5.1")
    testImplementation("io.kotest:kotest-assertions-core:5.4.1")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.2.5")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.6"
        languageVersion = "1.6"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xjsr305=strict")
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}

tasks.withType<Test> {
    environment("ENVIRONMENT", "test")
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(false)
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                minimum = "1.0".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "1.0".toBigDecimal()
            }
            limit {
                counter = "CLASS"
                minimum = "1.0".toBigDecimal()
            }
            excludes = listOf(
                "interview.configuration.*",
                "interview.plugins.*",
                "interview.plugins.*",
                "interview.plugins.models.*serializer",
            )
        }
    }
}

application {
    mainClass.set("ApplicationKt")
}

// TODO: get values from the environment variables
flyway {
    url = "jdbc:postgresql://localhost:5432/order_manager_db" /*System.getenv("DB_URL")*/
    user = "postgres" /*System.getenv("DB_USER")*/
    password = "postgres" /*System.getenv("DB_PASSWORD")*/
    baselineOnMigrate = true
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}
