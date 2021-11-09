plugins {
    java
    `java-library`
    `maven-publish`
}

group = "net.azisaba"
version = "0.0.1"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo2.acrylicstyle.xyz") }
}

dependencies {
    // some stuffs required to transform classes
    api("net.blueberrymc:native-util:1.2.5")
    api("org.javassist:javassist:3.28.0-GA")

    // logging
    api("org.apache.logging.log4j:log4j-api:2.14.1")
    api("org.apache.logging.log4j:log4j-core:2.14.1")

    // jetbrains annotations
    compileOnly("org.jetbrains:annotations:22.0.0")

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<ProcessResources> {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.rootProject.name,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(projectDir) { include("LICENSE") }
    }
}
