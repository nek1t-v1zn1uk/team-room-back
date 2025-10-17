plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // JPA(jakarta) - simplify database interactions
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Web - RESTful API
    implementation("org.springframework.boot:spring-boot-starter-web")

    // DevTools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Kotlin Reflect - to inspect and manipulate Kotlin`s structure and behavior at runtime
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Lombok - reduce boilerplate
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    // Postgres
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Docker
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // .env
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Jackson - convert Kotlin data classes to and from JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5") // For Jackson for JWT JSON processing

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // Http
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
