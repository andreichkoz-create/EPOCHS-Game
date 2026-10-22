plugins { id("com.android.application") }
android {
 namespace = "com.epochs.game"
 compileSdk = 35
 defaultConfig {
 applicationId = "com.epochs.game"
 minSdk = 24
 targetSdk = 35
 versionCode = 1
 versionName = "0.1.0"
 val gameUrl = providers.gradleProperty("epochsGameUrl").orElse("https://example.invalid/").get()
 buildConfigField("String", "GAME_URL", "\"${gameUrl}\"")
 }
 buildFeatures { buildConfig = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}