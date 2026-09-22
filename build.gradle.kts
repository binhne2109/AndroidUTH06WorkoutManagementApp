plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.5" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}

// Tự động chuyển thư mục build vào Temp hệ thống nếu đường dẫn chứa ký tự có dấu (Unicode)
if (rootProject.projectDir.absolutePath.any { it.code > 127 }) {
    val tempDir = System.getProperty("java.io.tmpdir").replace('\\', '/')
    allprojects {
        layout.buildDirectory.set(file("$tempDir/AndroidBuild/${rootProject.name}/${project.name}"))
    }
}
