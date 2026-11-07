package ie.setu.config

import ie.setu.controllers.ActivityController
import ie.setu.controllers.UserController
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
        // User routes
        config.routes.get("/api/users", UserController::getAllUsers)
        config.routes.get("/api/users/{user-id}", UserController::getUserByUserId)
        config.routes.post("/api/users", UserController::addUser)
        config.routes.get("/api/users/email/{email}", UserController::getUserByEmail)
        config.routes.delete("/api/users/{user-id}", UserController::deleteUser)
        config.routes.patch("/api/users/{user-id}", UserController::updateUser)

        // Activity routes
        config.routes.get("/api/activities", ActivityController::getAllActivities)
        config.routes.post("/api/activities", ActivityController::addActivity)
        config.routes.get("/api/activities/{activity-id}", ActivityController::getActivityByActivityId)
        config.routes.patch("/api/activities/{activity-id}", ActivityController::updateActivity)
        config.routes.delete("/api/activities/{activity-id}", ActivityController::deleteActivityByActivityId)
        config.routes.get("/api/users/{user-id}/activities", ActivityController::getActivitiesByUserId)
        config.routes.delete("/api/users/{user-id}/activities", ActivityController::deleteActivityByUserId)
    }

    private fun getRemoteAssignedPort(): Int {
        val remotePort = System.getenv("PORT")
        return if (remotePort != null) {
            Integer.parseInt(remotePort)
        } else 8080
    }

}

