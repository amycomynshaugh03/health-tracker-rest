package ie.setu.controllers

import ie.setu.domain.User
import ie.setu.domain.db.Users
import helpers.ServerContainer
import helpers.nonExistingEmail
import helpers.populateUserTable
import helpers.users
import helpers.validEmail
import helpers.validName
import ie.setu.utils.jsonToObject
import kong.unirest.core.HttpResponse
import kong.unirest.core.JsonNode
import kong.unirest.core.Unirest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

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
                SchemaUtils.create(Users)
            }
        }
    }

    @Nested
    inner class CreateUsers {

        @Test
        fun `add a user with correct details returns a 201 response`() {
            val addResponse = addUser(validName, validEmail)
            assertEquals(201, addResponse.status)

            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            val retrievedUser: User = jsonToObject(addResponse.body.toString())
            assertEquals(validEmail, retrievedUser.email)
            assertEquals(validName, retrievedUser.name)

            val deleteResponse = deleteUser(retrievedUser.id)
            assertEquals(204, deleteResponse.status)
        }

        @Test
        fun `multiple users added to table can be retrieved successfully`() {
            transaction {
                val userDAO = populateUserTable()

                assertEquals(3, userDAO.getAll().size)
                assertEquals(users[0].name, userDAO.findByEmail(users[0].email)?.name)
                assertEquals(users[1].name, userDAO.findByEmail(users[1].email)?.name)
                assertEquals(users[2].name, userDAO.findByEmail(users[2].email)?.name)
            }
        }

        @Test
        fun `getting a user by id when id exists, returns a 200 response`() {
            val addResponse = addUser(validName, validEmail)
            val addedUser: User = jsonToObject(addResponse.body.toString())

            val retrieveResponse = retrieveUserById(addedUser.id)
            assertEquals(200, retrieveResponse.status)

            deleteUser(addedUser.id)
        }

        @Test
        fun `getting a user by email when email exists, returns a 200 response`() {
            addUser(validName, validEmail)

            val retrieveResponse = retrieveUserByEmail(validEmail)
            assertEquals(200, retrieveResponse.status)

            val retrievedUser: User = jsonToObject(retrieveResponse.body.toString())
            deleteUser(retrievedUser.id)
        }
    }

    @Nested
    inner class ReadUsers {

        @Test
        fun `get all users from the database returns 200 or 404 response`() {
            val response = Unirest.get(origin + "/api/users").asString()
            if (response.status == 200) {
                val retrievedUsers: ArrayList<User> = jsonToObject(response.body.toString())
                assertNotEquals(0, retrievedUsers.size)
            } else {
                assertEquals(404, response.status)
            }
        }

        @Test
        fun `get user by id when user does not exist returns 404 response`() {
            val id = Integer.MIN_VALUE
            val retrieveResponse = Unirest.get(origin + "/api/users/${id}").asString()
            assertEquals(404, retrieveResponse.status)
        }

        @Test
        fun `get user by email when user does not exist returns 404 response`() {
            val retrieveResponse = Unirest.get(origin + "/api/users/email/${nonExistingEmail}").asString()
            assertEquals(404, retrieveResponse.status)
        }
    }

    @Nested
    inner class UpdateUsers {

        @Test
        fun `updating a user when it exists, returns a 204 response`() {
            val updatedName = "Updated Name"
            val updatedEmail = "Updated Email"
            val addedResponse = addUser(validName, validEmail)
            val addedUser: User = jsonToObject(addedResponse.body.toString())

            assertEquals(204, updateUser(addedUser.id, updatedName, updatedEmail).status)

            val updatedUserResponse = retrieveUserById(addedUser.id)
            val updatedUser: User = jsonToObject(updatedUserResponse.body.toString())
            assertEquals(updatedName, updatedUser.name)
            assertEquals(updatedEmail, updatedUser.email)

            deleteUser(addedUser.id)
        }

        @Test
        fun `updating a user when it doesn't exist, returns a 404 response`() {
            val updatedName = "Updated Name"
            val updatedEmail = "Updated Email"
            assertEquals(404, updateUser(-1, updatedName, updatedEmail).status)
        }
    }

    @Nested
    inner class DeleteUsers {

        @Test
        fun `deleting a user when it doesn't exist, returns a 404 response`() {
            assertEquals(404, deleteUser(-1).status)
        }

        @Test
        fun `deleting a user when it exists, returns a 204 response`() {
            val addedResponse = addUser(validName, validEmail)
            val addedUser: User = jsonToObject(addedResponse.body.toString())

            assertEquals(204, deleteUser(addedUser.id).status)
            assertEquals(404, retrieveUserById(addedUser.id).status)
        }
    }

    private fun addUser(name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.post(origin + "/api/users")
            .body("{\"id\":0, \"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }

    private fun deleteUser(id: Int): HttpResponse<String> {
        return Unirest.delete(origin + "/api/users/$id").asString()
    }

    private fun retrieveUserByEmail(email: String): HttpResponse<String> {
        return Unirest.get(origin + "/api/users/email/${email}").asString()
    }

    private fun retrieveUserById(id: Int): HttpResponse<String> {
        return Unirest.get(origin + "/api/users/${id}").asString()
    }

    private fun updateUser(id: Int, name: String, email: String): HttpResponse<JsonNode> {
        return Unirest.patch(origin + "/api/users/$id")
            .body("{\"id\":$id, \"name\":\"$name\", \"email\":\"$email\"}")
            .asJson()
    }
}