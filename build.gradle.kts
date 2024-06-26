import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "horange"
version = "2.3.4"
val versionCode = 4

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://maven.aliyun.com/repository/apache-snapshots")
    maven("https://maven.aliyun.com/repository/google")
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
    implementation("org.jetbrains.compose.material3:material3-desktop:1.5.11")
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