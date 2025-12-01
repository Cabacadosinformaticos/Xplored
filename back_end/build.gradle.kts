plugins {
	java
	id("org.springframework.boot") version "3.5.7"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.Xplored"
version = "0.0.1-SNAPSHOT"
description = "Xplored API"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// MySQL driver, version managed by the Spring Boot BOM
	runtimeOnly("com.mysql:mysql-connector-j")
	implementation("org.springframework.security:spring-security-core") // for BCrypt
}

tasks.withType<Test> {
	useJUnitPlatform()
}
