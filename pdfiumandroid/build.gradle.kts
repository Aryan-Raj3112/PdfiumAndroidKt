import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jreleaser.model.Active
import org.jreleaser.model.Signing


plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jreleaser)
    `maven-publish`
}
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xstring-concat=inline")
    }
}

android {
    namespace = "io.legere.pdfiumandroid"
    compileSdk = 36

    ndkVersion = "29.0.13846066"

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
//        maybeCreate("qa")
//        getByName("qa") {
//            matchingFallbacks += listOf("release")
//            isMinifyEnabled = true
//            signingConfig = signingConfigs.getByName("debug")
//        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
//            version = "4.0.3"
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }
    publishing {
        singleVariant("release") {
            // if you don't want sources/javadoc, remove these lines
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    compileOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.androidx.annotation.jvm)
    compileOnly(libs.kotlin.stdlib)
    implementation(libs.guava)

    testImplementation(libs.junit)

    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.espresso.core)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.core.testing)
}

fun getRepositoryUsername(): String =
    if (rootProject.hasProperty("JRELEASER_MAVENCENTRAL_USERNAME")) {
        rootProject.properties["JRELEASER_MAVENCENTRAL_USERNAME"] as String
    } else {
        ""
    }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.legere"
            artifactId = "pdfiumandroid"
            version = project.findProperty("VERSION_NAME") as? String ?: "v1.0.35-custom"

            pom {
                name.set("pdfiumandroid")
                description.set(project.findProperty("POM_DESCRIPTION") as? String ?: "Pdfium Android Library")
                url.set(project.findProperty("POM_URL") as? String ?: "https://github.com/Aryan-Raj3112/PdfiumAndroidKt")
                
                licenses {
                    license {
                        name.set(project.findProperty("POM_LICENCE_NAME") as? String ?: "The Apache License, Version 2.0")
                        url.set(project.findProperty("POM_LICENCE_URL") as? String ?: "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set(project.findProperty("POM_LICENCE_DIST") as? String ?: "repo")
                    }
                }
                developers {
                    developer {
                        id.set(project.findProperty("POM_DEVELOPER_ID") as? String ?: "aryan")
                        name.set(project.findProperty("POM_DEVELOPER_NAME") as? String ?: "Aryan")
                    }
                }
                scm {
                    connection.set(project.findProperty("POM_SCM_CONNECTION") as? String ?: "")
                    developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION") as? String ?: "")
                    url.set(project.findProperty("POM_SCM_URL") as? String ?: "")
                }
            }
            
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            url =
                uri(layout.buildDirectory.dir("target/staging-deploy"))
        }
    }
}

jreleaser {
    project {
        inceptionYear = "2023"
        author("@johngray1965")
        description = rootProject.properties["POM_DESCRIPTION"] as String
        version = rootProject.properties["VERSION_NAME"] as String
    }
    gitRootSearch = true
    signing {
        active = Active.ALWAYS
        mode = Signing.Mode.MEMORY
        armored = true
        verify = true
    }
    release {
        github {
            repoOwner = "johngray1965"
            overwrite = true
        }
    }
//    distributions {
//        create("zip") {
//            artifacts {
//                add(
//                    layout.buildDirectory
//                        .dir("libs")
//                        .map {
//                            it.file("pdfiumandroid.zip")
//                        }
//                )
//            }
//        }
//    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                verifyPom = false
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(
                    layout.buildDirectory
                        .dir("target/staging-deploy")
                        .get()
                        .toString(),
                )
                username = getRepositoryUsername()
            }
        }
    }
}
