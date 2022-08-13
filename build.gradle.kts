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
val exposedVersion: String by project

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
//    implementation("io.ktor:ktor-html-builder:1.6.8")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("io.arrow-kt:arrow-core:1.1.2")

    implementation("ch.qos.logback:logback-classic:1.2.11")

    implementation("org.postgresql:postgresql:42.4.1")
    implementation("org.flywaydb:flyway-core:9.1.3")

    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("io.kotest:kotest-assertions-core:5.4.1")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.2.5")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.6"
        languageVersion = "1.6"
        freeCompilerArgs = freeCompilerArgs + listOf("-Xjsr305=strict")
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
                "interview.*",
            )
        }
    }
}

application {
    mainClass.set("ApplicationKt")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/order_manager_db" /*System.getenv("DB_URL")*/
    user = "postgres" /*System.getenv("DB_USER")*/
    password = "postgres" /*System.getenv("DB_PASSWORD")*/
    baselineOnMigrate = true
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}
