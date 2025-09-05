plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.zorro.easy.permissions"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}

dependencies {
    compileOnly(libs.androidx.appcompat)
    compileOnly(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
}

//////////////////mavenPublishing////////////////////////

mavenPublishing {

    publishToMavenCentral()

    signAllPublications()

    coordinates("io.github.pangli", "easy-permissions", "0.0.3")

    pom {
        name = project.name
        description = "Quick permission request."
        url = "https://github.com/pangli/EasyPermissions/tree/main/easypermissions"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                name = "Zorro"
                email = "zorro.pang@gmail.com"
            }
        }

        scm {
            connection = "scm:git:git://github.com/pangli/EasyPermissions.git"
            developerConnection = "scm:git:ssh://github.com/pangli/EasyPermissions.git"
            url = "https://github.com/pangli/EasyPermissions/tree/main/easypermissions"
        }
    }
}

