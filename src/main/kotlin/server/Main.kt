package server

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.config.WebAppConfigs
import server.config.WebAppConfigurer
import server.http.JsonWebResponse
import server.http.TextWebResponse
import server.http.WebResponse
import server.mappers.DbMapper
import server.mappers.KtorJsonWebResponse
import javax.sql.DataSource
import kotlin.reflect.full.declaredMemberProperties

private val log = LoggerFactory.getLogger("server.Main")

fun main(args: Array<String>) {
    log.debug("Program arguments: ${args.joinToString()}")
    val env = System.getenv("SERVER_ENV") ?: "local"
    log.info("Running in $env environment")

    val webAppConfig = WebAppConfigurer.createWebAppConfig(env)
    // Create the DB Connection and trigger DB Migrations (if any)
    val dataSource = createAndMigrateDatasource(webAppConfig)

    // embeddedServer is a function that takes a Netty engine and a port number
    // and starts the server.
    embeddedServer(Netty, port = webAppConfig.httpPort) {
        serverApplication(log, dataSource)
    }.start(wait = true)
}

/**
 * Method Serves the endpoints, including erroneous ones.
 * It also includes a 'health' endpoint that check the DB for its status.
 */
fun Application.serverApplication(log: Logger, dataSource: DataSource) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            log.error("Server Error", cause)
            call.respondText(
                text = "500: $cause",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
    // Webapp defined routes
    routing {
        get(
            "/",
            webResponse {
                TextWebResponse("Hello World!")
                    .header("X-test-header", "test-value")
            }
        )

        get(
            "/json",
            webResponse {
                JsonWebResponse(mapOf("foo" to "bar"))
                    .header("X-test-header", "test-value")
            }
        )

        // Health check endpoint
        get(
            "/health",
            webResponse {
                TextWebResponse(dbMapperHealthCheck(dataSource))
                    .header("X-test-header", "test-value")
            }
        )
    }
}

// Custom extension function to return the response types defined
// in the WebResponse interface. It extends ktor route.get() function.
private fun webResponse(
    handler: suspend PipelineContext<Unit, ApplicationCall>.() -> WebResponse
): PipelineInterceptor<Unit, ApplicationCall> {
    return {
        val resp = this.handler()
        for ((name, values) in resp.headers())
            for (value in values)
                call.response.header(name, value)
        val statusCode = HttpStatusCode.fromValue(
            resp.statusCode
        )
        when (resp) {
            is TextWebResponse -> {
                call.respondText(
                    text = resp.body,
                    status = statusCode
                )
            }

            is JsonWebResponse -> {
                call.respond(
                    KtorJsonWebResponse(
                        body = resp.body,
                        status = statusCode
                    )
                )
            }
        }
    }
}

private fun createAndMigrateDatasource(configs: WebAppConfigs): DataSource {
    val dataSource = createDatasource(configs).also {
        migrateDataSource(it)
    }
    return dataSource
}

/**
 * The following method setups the connection (and pool)
 */
private fun createDatasource(configs: WebAppConfigs) =
    HikariDataSource().apply {
        jdbcUrl = configs.dbUrl
        username = configs.dbUserName
        password = configs.dbPassword
    }

private fun migrateDataSource(dataSource: DataSource) {
    Flyway.configure()
        .dataSource(dataSource)
        .locations("db/migration")
        .table("flyway_schema_history")
        .load()
        .migrate()
}
private fun healthCheck(dataSource: DataSource): String {
    dataSource.connection.use { conn ->
        conn.createStatement().use { stmt ->
            stmt.executeQuery("SELECT 1")
        }
    }
    return "DATABASE STATUS: OK"
}

private fun dbMapperHealthCheck(dataSource: DataSource): String {
    val queryResult = DbMapper.run {
        executeSingleRowResultQuery(dataSource, "SELECT 1")
    }

    val statusFailed = queryResult?.isEmpty() ?: true

    return if (statusFailed) {
        "DATABASE STATUS: FAILED"
    } else {
        "DATABASE STATUS: OK"
    }
}
