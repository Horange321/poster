import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "horange"
version = "1.1.3"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/apache-snapshots")
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

compose.desktop {
    application {
        mainClass = "horange.poster.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Pkg, TargetFormat.Exe)
            packageName = "poster"
            packageVersion = version.toString()
        }

        buildTypes.release {
            proguard {
                configurationFiles.from(project.file("proguard-rules.pro"))
                obfuscate = true
                optimize = true
            }
        }
    }
}