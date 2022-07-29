import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.gaussian"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.3.2"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "org.gaussian.vgraph.Main"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-config-yaml:4.3.2")
  implementation("io.vertx:vertx-web-graphql")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-circuit-breaker")
  implementation("io.vertx:vertx-micrometer-metrics")
  implementation("io.vertx:vertx-rx-java3")
  implementation("io.vertx:vertx-reactive-streams")
  implementation("com.google.guava:guava:31.1-jre")
  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("ch.qos.logback:logback-classic:1.2.11")
  implementation("org.valid4j:valid4j:0.5.0")
  implementation("org.valid4j:http-matchers:1.1")
  implementation("org.hamcrest:hamcrest:2.2")
  implementation("org.apache.commons:commons-lang3:3.12.0")
  implementation("com.graphql-java:graphql-java-extended-scalars:18.1")
  implementation("com.swrve:rate-limited-logger:2.0.2")
  implementation("com.google.inject:guice:5.1.0")
  implementation("com.google.inject.extensions:guice-multibindings:4.2.3")
  implementation("com.datadoghq:java-dogstatsd-client:4.0.0")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.13.3")
  implementation("io.vertx:vertx-micrometer-metrics:4.3.2")
  implementation("io.micrometer:micrometer-registry-prometheus:1.9.2")

  compileOnly("org.projectlombok:lombok:1.18.24")
  annotationProcessor("org.projectlombok:lombok:1.18.24")

  runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.78.Final:osx-aarch_64")
  runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.78.Final:osx-x86_64")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
