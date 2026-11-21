package ie.setu.config

import ie.setu.domain.db.Activities
import ie.setu.domain.db.Users
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.postgresql.util.PSQLException

class DbConfig {

    private val logger = KotlinLogging.logger {}
    private lateinit var dbConfig: Database

    fun getDbConnection(): Database {

        val rawHost = System.getenv("POSTGRESQL_HOST") ?: "localhost"
        val PGHOST = rawHost.substringAfterLast("://")
        val PGPORT = System.getenv("POSTGRESQL_SERVICE_PORT") ?: "5432"
        val PGDATABASE = System.getenv("POSTGRESQL_DATABASE") ?: ""
        val PGUSER = System.getenv("POSTGRESQL_USER") ?: "sa"
        val PGPASSWORD = System.getenv("POSTGRESQL_PASSWORD") ?: ""

        try {
            logger.info { "Starting DB Connection...PGHOST: $PGHOST" }

            if (PGHOST == "localhost") {
                logger.info { "Using local h2 instance for development." }
                dbConfig = Database.connect(
                    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                    driver = "org.h2.Driver",
                    user = PGUSER,
                    password = PGPASSWORD
                )
                transaction {
                    SchemaUtils.create(Users)
                    SchemaUtils.create(Activities)
                }
                populateLocalTestData()
            } else {
                logger.info { "Using remote PostgreSQL instance." }
                val dbUrl = "jdbc:postgresql://$rawHost:$PGPORT/$PGDATABASE"

                dbConfig = Database.connect(
                    url = dbUrl,
                    driver = "org.postgresql.Driver",
                    user = PGUSER,
                    password = PGPASSWORD
                )
            }

            transaction {
                exec("SELECT 1;")
            }
            logger.info { "DB Connected Successfully to $PGDATABASE at $rawHost:$PGPORT" }

        } catch (e: PSQLException) {
            logger.error(e) { "Error in DB Connection: ${e.message}" }
            logger.info { "Env vars used: host=$PGHOST, port=$PGPORT, user=$PGUSER, db=$PGDATABASE" }
        }

        return dbConfig
    }

    private fun populateLocalTestData() {
        transaction {
            addUser("Minnie", "Minnie@mouse.com")
            addUser("Mickey", "Mickey@mouse.com")
            addUser("Homer Simpson", "Homer@simpson.com")
            addUser("Marge Simpson", "Marge@simpson.com")
            addUser("Bart Simpson", "Bart@simpson.com")

            addActivity("Morning walk", 30.0, 150, 1)
            addActivity("Yoga", 45.0, 200, 1)
            addActivity("Cycling", 60.0, 500, 2)
            addActivity("Morning run", 45.5, 400, 3)
            addActivity("Weights", 40.0, 300, 3)
            addActivity("Afternoon swim", 25.6, 280, 4)
        }
        logger.info { "Local test data added: 5 users, 6 activities" }
    }

    private fun addUser(userName: String, userEmail: String) {
        Users.insert {
            it[name] = userName
            it[email] = userEmail
        }
    }

    private fun addActivity(activityDescription: String, activityDuration: Double,
                            activityCalories: Int, activityUserId: Int) {
        Activities.insert {
            it[description] = activityDescription
            it[duration] = activityDuration
            it[calories] = activityCalories
            it[started] = DateTime.now()
            it[userId] = activityUserId
        }
    }
}