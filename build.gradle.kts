import org.junit.platform.gradle.plugin.JUnitPlatformPlugin

buildscript {
	repositories {
		jcenter()
	}

	dependencies {
		classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.1")
	}
}

plugins {
	kotlin("jvm") version ("1.1.51")
}
apply<JUnitPlatformPlugin>()

repositories {
	jcenter()
}

val kotlinVersion by project
val freemarkerVersion by project
val javaxMailVersion by project
val junitVersion by project

dependencies {
	compile(kotlin("stdlib-jre8", "${kotlinVersion}"))
	compile("com.sun.mail:javax.mail:${javaxMailVersion}")
	compile("org.freemarker:freemarker:${freemarkerVersion}")

	testCompile("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
}

task<Wrapper>("wrapper") {
	gradleVersion = "4.3"
	distributionType = Wrapper.DistributionType.ALL
}
