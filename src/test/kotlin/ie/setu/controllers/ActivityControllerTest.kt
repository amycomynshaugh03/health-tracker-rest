package ie.setu.controllers

import helpers.ServerContainer
import helpers.activities
import helpers.updatedCalories
import helpers.updatedDescription
import helpers.updatedDuration
import helpers.updatedStarted
import helpers.validEmail
import helpers.validName
import ie.setu.domain.Activity
import ie.setu.domain.User
import ie.setu.domain.db.Activities
import ie.setu.domain.db.Users
import ie.setu.utils.jsonToObject
import kong.unirest.core.HttpResponse
import kong.unirest.core.JsonNode
import kong.unirest.core.Unirest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityControllerTest {

    private val app = ServerContainer.instance
    private val origin = "http://localhost:" + app.port()

    companion object {
        @BeforeAll
        @JvmStatic
        fun setupInMemoryDatabase() {
            Database.connect(
                url = "jdbc:h2:mem:integration;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )
            transaction {
                SchemaUtils.create(Users, Activities)
            }
        }
    }

    @Nested
    inner class CreateActivities {

        @Test
        fun `add an activity when a user exists for it, returns a 201 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `add an activity when no user exists for it, returns a 404 response`() {
            val userId = -1
            assertEquals(404, retrieveUserById(userId).status)

            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, userId
            )
            assertEquals(404, addActivityResponse.status)
        }
    }

    @Nested
    inner class ReadActivities {

        @Test
        fun `get all activities from the database returns 200 or 404 response`() {
            val response = retrieveAllActivities()
            if (response.status == 200) {
                val retrievedActivities = jsonNodeToObject<Array<Activity>>(response)
                assertNotEquals(0, retrievedActivities.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get all activities by user id when user and activities exists returns 200 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            addActivity(activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id)
            addActivity(activities[1].description, activities[1].duration,
                activities[1].calories, activities[1].started, addedUser.id)
            addActivity(activities[2].description, activities[2].duration,
                activities[2].calories, activities[2].started, addedUser.id)

            val response = retrieveActivitiesByUserId(addedUser.id)
            assertEquals(200, response.status)
            val retrievedActivities = jsonNodeToObject<Array<Activity>>(response)
            assertEquals(3, retrievedActivities.size)

            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all activities by user id when no activities exist returns 404 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())

            val response = retrieveActivitiesByUserId(addedUser.id)
            assertEquals(404, response.status)

            assertEquals(204, deleteUser(addedUser.id).status)
        }

        @Test
        fun `get all activities by user id when no user exists returns 404 response`() {
            val response = retrieveActivitiesByUserId(-1)
            assertEquals(404, response.status)
        }

        @Test
        fun `get activity by activity id when no activity exists returns 404 response`() {
            val response = retrieveActivityByActivityId(-1)
            assertEquals(404, response.status)
        }

        @Test
        fun `get activity by activity id when activity exists returns 200 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)

            val response = retrieveActivityByActivityId(addedActivity.id)
            assertEquals(200, response.status)

            assertEquals(204, deleteUser(addedUser.id).status)
        }
    }

    @Nested
    inner class UpdateActivities {

        @Test
        fun `updating an activity by activity id when it doesn't exist, returns a 404 response`() {
            val userId = -1
            val activityId = -1
            assertEquals(404, retrieveUserById(userId).status)

            assertEquals(
                404, updateActivity(
                    activityId, updatedDescription, updatedDuration,
                    updatedCalories, updatedStarted, userId
                ).status
            )
        }

        @Test
        fun `updating an activity by activity id when it exists, returns 204 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)
            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)

            val updatedActivityResponse = updateActivity(
                addedActivity.id, updatedDescription,
                updatedDuration, updatedCalories, updatedStarted, addedUser.id
            )
            assertEquals(204, updatedActivityResponse.status)

            val retrievedActivityResponse = retrieveActivityByActivityId(addedActivity.id)
            val updatedActivity = jsonNodeToObject<Activity>(retrievedActivityResponse)
            assertEquals(updatedDescription, updatedActivity.description)
            assertEquals(updatedDuration, updatedActivity.duration, 0.1)
            assertEquals(updatedCalories, updatedActivity.calories)
            assertEquals(updatedStarted, updatedActivity.started)

            deleteUser(addedUser.id)
        }
    }

    @Nested
    inner class DeleteActivities {

        @Test
        fun `deleting an activity by activity id when it doesn't exist, returns a 404 response`() {
            assertEquals(404, deleteActivityByActivityId(-1).status)
        }

        @Test
        fun `deleting activities by user id when it doesn't exist, returns a 404 response`() {
            assertEquals(404, deleteActivitiesByUserId(-1).status)
        }

        @Test
        fun `deleting an activity by id when it exists, returns a 204 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse = addActivity(
                activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id
            )
            assertEquals(201, addActivityResponse.status)

            val addedActivity = jsonNodeToObject<Activity>(addActivityResponse)
            assertEquals(204, deleteActivityByActivityId(addedActivity.id).status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `deleting all activities by userid when it exists, returns a 204 response`() {
            val addedUser: User = jsonToObject(addUser(validName, validEmail).body.toString())
            val addActivityResponse1 = addActivity(activities[0].description, activities[0].duration,
                activities[0].calories, activities[0].started, addedUser.id)
            assertEquals(201, addActivityResponse1.status)
            val addActivityResponse2 = addActivity(activities[1].description, activities[1].duration,
                activities[1].calories, activities[1].started, addedUser.id)
            assertEquals(201, addActivityResponse2.status)
            val addActivityResponse3 = addActivity(activities[2].description, activities[2].duration,
                activities[2].calories, activities[2].started, addedUser.id)
            assertEquals(201, addActivityResponse3.status)

            assertEquals(204, deleteActivitiesByUserId(addedUser.id).status)

            val addedActivity1 = jsonNodeToObject<Activity>(addActivityResponse1)
            val addedActivity2 = jsonNodeToObject<Activity>(addActivityResponse2)
            val addedActivity3 = jsonNodeToObject<Activity>(addActivityResponse3)
            assertEquals(404, retrieveActivityByActivityId(addedActivity1.id).status)
            assertEquals(404, retrieveActivityByActivityId(addedActivity2.id).status)
            assertEquals(404, retrieveActivityByActivityId(addedActivity3.id).status)

            deleteUser(addedUser.id)
        }
    }

    private inline fun <reified T : Any> jsonNodeToObject(response: HttpResponse<JsonNode>): T {
        return jsonToObject(response.body.toString())
    }

    private fun addUser(name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/users")
            .body("{\"id\":0, \"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    private fun deleteUser(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id").asString()
    }

    private fun retrieveUserById(id: Int): HttpResponse<String> {
        return Unirest.get(origin + "/api/users/${id}").asString()
    }

    private fun retrieveAllActivities(): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/activities").asJson()
    }

    private fun retrieveActivitiesByUserId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/users/${id}/activities").asJson()
    }

    private fun retrieveActivityByActivityId(id: Int): HttpResponse<JsonNode> {
        return Unirest.get(origin + "/api/activities/${id}").asJson()
    }

    private fun deleteActivityByActivityId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/activities/$id").asString()
    }

    private fun deleteActivitiesByUserId(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id/activities").asString()
    }

    private fun addActivity(description: String, duration: Double, calories: Int,
                            started: DateTime, userId: Int): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/activities")
            .body("""
                {
                   "id":0,
                   "description":"$description",
                   "duration":$duration,
                   "calories":$calories,
                   "started":"$started",
                   "userId":$userId
                }
            """.trimIndent())
            .asJson()
    }

    private fun updateActivity(id: Int, description: String, duration: Double, calories: Int,
                               started: DateTime, userId: Int): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/activities/$id")
            .body("""
                {
                  "id":$id,
                  "description":"$description",
                  "duration":$duration,
                  "calories":$calories,
                  "started":"$started",
                  "userId":$userId
                }
            """.trimIndent())
            .asJson()
    }
}