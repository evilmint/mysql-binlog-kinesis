import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.61"
}

group = "pl.alorenc.me"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    compile("com.github.shyiko", "mysql-binlog-connector-java", "0.18.1")// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    compile("software.amazon.kinesis", "amazon-kinesis-client", "2.2.4")// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    compile("org.slf4j", "slf4j-simple", "1.7.25")// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation(kotlin("stdlib-jdk8"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}