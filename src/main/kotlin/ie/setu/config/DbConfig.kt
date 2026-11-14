package ie.setu.config

import ie.setu.domain.db.Activities
import ie.setu.domain.db.Users
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
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

            if (PGHOST == "localhost"){
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
}
