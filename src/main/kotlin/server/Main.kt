package server

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import server.config.WebAppConfigs
import server.http.JsonWebResponse
import server.http.TextWebResponse
import server.http.WebResponse
import server.mappers.KtorJsonWebResponse
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers

private val log = LoggerFactory.getLogger("server.Main")

fun main(args: Array<String>) {

    log.debug("Program arguments: ${args.joinToString()}")
    val env = System.getenv("SERVER_ENV") ?: "local"
    log.info("Running in $env environment")

    val webAppConfig = createWebAppConfig(env)
    logProperties(webAppConfig)

    //embeddedServer is a function that takes a Netty engine and a port number
    //and starts the server.
    embeddedServer(Netty, port = webAppConfig.httpPort) {
        serverApplication(log)
    }.start(wait = true)
}

fun Application.serverApplication(log: Logger) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            log.error("Server Error", cause)
            call.respondText(
                text = "500: $cause",
                status = HttpStatusCode.InternalServerError
            )
        }
    }
    //Webapp defined routes
    routing {

        get("/", webResponse {
            TextWebResponse("Hello World!")
                .header("X-test-header", "test-value")
        })

        get("/json", webResponse {
            JsonWebResponse(mapOf("foo" to "bar"))
                .header("X-test-header", "test-value")
        })
    }
}

//Kotlin's single 'expression function' syntax allows us to omit the curly braces
//and the return type declaration.
private fun createWebAppConfig(env: String) = ConfigFactory
    .parseResources("app-$env.conf")
    .withFallback(ConfigFactory.parseResources("app.conf"))
    .resolve()
    .let {
        //let receives the value of the lambda expression as the parameter and
        //returns the value of the lambda expression.
        //value is a Config! object from resolve() call
        WebAppConfigs(
            httpPort = it.getInt("httpPort")
        )
    }

//Custom extension function to return the response types defined
//in the WebResponse interface. It extends ktor route.get() function.
fun webResponse(
    handler: suspend PipelineContext<Unit, ApplicationCall>.(
    ) -> WebResponse
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

//Logs properties of the WebAppConfigs object, masks the sensitive ones.
private fun logProperties(webAppConfig: WebAppConfigs) {
    val secretsRegex = "password|secret|key"
        .toRegex(RegexOption.IGNORE_CASE)

    //Exemplifying Kotlin's metaprogramming capabilities (Java reflection)
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
