package server.config

/**
 * Class holds configurations loaded from app.conf file(s) for different
 * environments.
*/
data class WebAppConfigs(
    val httpPort: Int,
    val dbUserName: String? = null,
    val dbPassword: String? = null,
    val dbUrl: String? = null
)
