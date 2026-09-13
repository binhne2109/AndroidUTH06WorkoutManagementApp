plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.5" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}

allprojects {
    layout.buildDirectory.set(file("C:/Users/zphuo/AppData/Local/Temp/AndroidBuild/${rootProject.name}/${project.name}"))
}