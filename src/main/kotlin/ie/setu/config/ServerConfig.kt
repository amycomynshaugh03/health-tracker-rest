package ie.setu.config

import ie.setu.controllers.HealthTrackerController
import io.javalin.Javalin
import io.javalin.config.JavalinConfig

class ServerConfig {

    fun startJavalinService(): Javalin {

        val app = Javalin.create { config ->
            config.routes.exception(Exception::class.java) { e, ctx ->
                e.printStackTrace()
            }
            config.routes.error(404) { ctx ->
                ctx.json("404 - Not Found")
            }
            registerRoutes(config)
        }.start(7001)

        return app
    }

    private fun registerRoutes(config: JavalinConfig) {
        config.routes.get("/api/users", HealthTrackerController::getAllUsers)
        config.routes.get("/api/users/{user-id}", HealthTrackerController::getUserByUserId)
        config.routes.post("/api/users", HealthTrackerController::addUser)
    }
}

