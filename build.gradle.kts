plugins {
    id("java")
    id("io.freefair.lombok") version "8.3"
    id("org.openjfx.javafxplugin") version "0.1.0"

}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "17.0.8"
    modules("javafx.controls")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}