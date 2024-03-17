package server.config

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredMemberProperties

/**
 *  Class primary purpose is to load and configure application settings from external configuration files.
 *  It provides a centralized way to manage application configurations such as server port, database
 *  credentials, and other necessary settings. This approach helps in maintaining a clear separation
 *  between the code and configuration, enhancing security and making the application easier to
 *  configure across different environments.
 */
// Object in Kotlin rather than a class, it acts as an util-static type of object.
object WebAppConfigurer {

    private val log = LoggerFactory.getLogger("server.WebConfigurer")

    // Kotlin's single 'expression function' syntax allows us to omit the curly braces
    // and the return type declaration.
    fun createWebAppConfig(env: String) = ConfigFactory
        .parseResources("app-$env.conf")
        .withFallback(ConfigFactory.parseResources("app.conf"))
        .resolve()
        .let {
            // let receives the value of the lambda expression as the parameter and
            // returns the value of the lambda expression.
            // value is a Config! object from resolve() call
            WebAppConfigs(
                httpPort = it.getInt("httpPort"),
                dbUserName = it.getString("dbUserName"),
                dbPassword = it.getString("dbPassword"),
                dbUrl = it.getString("dbUrl")
            ).also { logProperties(it) }
        }

    // Logs properties of the WebAppConfigs object, masks the sensitive ones.
    private fun logProperties(webAppConfig: WebAppConfigs) {
        val secretsRegex = "password|secret|key"
            .toRegex(RegexOption.IGNORE_CASE)

        // Exemplifying Kotlin's metaprogramming capabilities (Java reflection)
        val propertiesString = WebAppConfigs::class.declaredMemberProperties
            .sortedBy { it.name }
            .map {
                if (secretsRegex.containsMatchIn(it.name)) {
                    "${it.name}: ******"
                } else {
                    "${it.name}: ${it.get(webAppConfig)}"
                }
            }
            .joinToString("\n")

        log.info("Loaded properties: $propertiesString")
    }
}
