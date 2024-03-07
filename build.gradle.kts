import org.gradle.api.JavaVersion.VERSION_17

plugins {
    application

}

group = "org.example"
version = ""

description = "A plex information bot."

java {
    sourceCompatibility = VERSION_17
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.javacord:javacord:3.8.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

    runtimeOnly("org.apache.logging.log4j:log4j-core:2.19.0")

    implementation("com.github.kekolab:javaplex:3.3.0")
}

application {
    mainClass.set("org.example.Main")
}
val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}"
    manifest {
        attributes["Implementation-Title"] = "Gradle Jar File Example"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "org.example.Main"
    }
    from(configurations.runtimeClasspath.get().map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks.jar.get() as CopySpec)
}

tasks.withType<org.gradle.jvm.tasks.Jar>() {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/BC1024KE.RSA", "META-INF/BC1024KE.SF", "META-INF/BC1024KE.DSA")
    exclude("META-INF/BC2048KE.RSA", "META-INF/BC2048KE.SF", "META-INF/BC2048KE.DSA")
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}
