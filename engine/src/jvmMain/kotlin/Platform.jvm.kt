
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Platform {
    actual fun getEnv(name: String) = System.getenv(name)
}