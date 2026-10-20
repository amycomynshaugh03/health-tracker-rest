package ie.setu.config

import ie.setu.controllers.HealthTrackerController
import ie.setu.utils.jsonObjectMapper
import io.javalin.Javalin
import io.javalin.config.JavalinConfig
import io.javalin.json.JavalinJackson

class ServerConfig {

    fun startJavalinService(): Javalin {

        val app = Javalin.create { config ->
            config.jsonMapper(
                JavalinJackson(jsonObjectMapper())
            )
            config.routes.exception(Exception::class.java) { e, ctx ->
                e.printStackTrace()
            }
            config.routes.error(404) { ctx ->
                ctx.json("404 - Not Found")
            }
            registerRoutes(config)
        }.start(getRemoteAssignedPort())

        return app
    }

    private fun registerRoutes(config: JavalinConfig) {
        config.routes.get("/api/users", HealthTrackerController::getAllUsers)
        config.routes.get("/api/users/{user-id}", HealthTrackerController::getUserByUserId)
        config.routes.post("/api/users", HealthTrackerController::addUser)
        config.routes.get("api/users/email/{email}", HealthTrackerController::getUserByEmail)
        config.routes.delete("/api/users/{user-id}", HealthTrackerController::deleteUser)
        config.routes.patch("/api/users/{user-id}", HealthTrackerController::updateUser)
        config.routes.get("/api/activities", HealthTrackerController::getAllActivities)
        config.routes.post("/api/activities", HealthTrackerController::addActivity)
        config.routes.get("/api/users/{user-id}/activities", HealthTrackerController::getActivitiesByUserId)
    }

    private fun getRemoteAssignedPort(): Int {
        val remotePort = System.getenv("PORT")
        return if (remotePort != null) {
            Integer.parseInt(remotePort)
        } else 8080
    }

}

