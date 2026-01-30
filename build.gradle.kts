plugins {
    application

}

group = "org.plexinfobot"
version = ""

description = "A plex information bot."

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.javacord:javacord:3.8.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation ("org.apache.httpcomponents.client5:httpclient5:5.3")
    implementation ("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.0-rc2")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
}

application {
    mainClass.set("org.plexinfobot.Main")
}

// Configure Gradle Java toolchain to use Java 21 and compile to Java 21 bytecode
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    // Ensure generated classes target Java 21
    options.release.set(21)
}

val fatJar = tasks.register<Jar>("fatJar") {
    archiveBaseName.set(project.name)
    archiveVersion.set(project.version.toString())
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = project.version.toString()
        attributes["Main-Class"] = "org.plexinfobot.Main"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks.withType<org.gradle.jvm.tasks.Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/BC1024KE.RSA", "META-INF/BC1024KE.SF", "META-INF/BC1024KE.DSA")
    exclude("META-INF/BC2048KE.RSA", "META-INF/BC2048KE.SF", "META-INF/BC2048KE.DSA")
}

// Ensure distribution and start scripts run after fatJar to avoid implicit dependency errors
tasks.named("startScripts") {
    dependsOn(fatJar)
}
tasks.named("distTar") {
    dependsOn(fatJar)
}
tasks.named("distZip") {
    dependsOn(fatJar)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
