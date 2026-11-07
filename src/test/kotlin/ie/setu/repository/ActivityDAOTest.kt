package ie.setu.repository

import ie.setu.domain.Activity
import ie.setu.domain.db.Activities
import ie.setu.domain.repository.ActivityDAO
import helpers.activities
import helpers.populateActivityTable
import helpers.populateUserTable
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ActivityDAOTest {

    companion object {
        @BeforeAll
        @JvmStatic
        internal fun setupInMemoryDatabaseConnection() {
            Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")
        }
    }

    private val activity1 = activities.get(0)
    private val activity2 = activities.get(1)
    private val activity3 = activities.get(2)

    @Nested
    inner class CreateActivities {

        @Test
        fun `multiple activities added to table can be retrieved successfully`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(3, activityDAO.getAll().size)
                assertEquals(activity1, activityDAO.findByActivityId(activity1.id))
                assertEquals(activity2, activityDAO.findByActivityId(activity2.id))
                assertEquals(activity3, activityDAO.findByActivityId(activity3.id))
            }
        }
    }

    @Nested
    inner class ReadActivities {

        @Test
        fun `getting all activites from a populated table returns all rows`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(3, activityDAO.getAll().size)
            }
        }

        @Test
        fun `get activity by user id that has no activities, results in no record returned`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(0, activityDAO.findByUserId(3).size)
            }
        }

        @Test
        fun `get activity by user id that exists, results in a correct activitie(s) returned`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(activity1, activityDAO.findByUserId(1).get(0))
                assertEquals(activity2, activityDAO.findByUserId(1).get(1))
                assertEquals(activity3, activityDAO.findByUserId(2).get(0))
            }
        }

        @Test
        fun `get all activities over empty table returns none`() {
            transaction {
                SchemaUtils.create(Activities)
                val activityDAO = ActivityDAO()
                assertEquals(0, activityDAO.getAll().size)
            }
        }

        @Test
        fun `get activity by activity id that has no records, results in no record returned`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(null, activityDAO.findByActivityId(4))
            }
        }

        @Test
        fun `get activity by activity id that exists, results in a correct activity returned`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(activity1, activityDAO.findByActivityId(1))
                assertEquals(activity3, activityDAO.findByActivityId(3))
            }
        }
    }

    @Nested
    inner class UpdateActivities {

        @Test
        fun `updating existing activity in table results in successful update`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                val activity3updated = Activity(id = 3, description = "Cardio", duration = 42.0,
                    calories = 220, started = DateTime.now(), userId = 2)
                activityDAO.updateByActivityId(activity3updated.id, activity3updated)
                assertEquals(activity3updated, activityDAO.findByActivityId(3))
            }
        }

        @Test
        fun `updating non-existant activity in table results in no updates`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                val activity4updated = Activity(id = 4, description = "Cardio", duration = 42.0,
                    calories = 220, started = DateTime.now(), userId = 2)
                activityDAO.updateByActivityId(4, activity4updated)
                assertEquals(null, activityDAO.findByActivityId(4))
                assertEquals(3, activityDAO.getAll().size)
            }
        }
    }

    @Nested
    inner class DeleteActivities {

        @Test
        fun `deleting a non-existant activity (by id) in table results in no deletion`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(3, activityDAO.getAll().size)
                activityDAO.deleteByActivityId(4)
                assertEquals(3, activityDAO.getAll().size)
            }
        }

        @Test
        fun `deleting an existing activity (by id) in table results in record being deleted`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(3, activityDAO.getAll().size)
                activityDAO.deleteByActivityId(activity3.id)
                assertEquals(2, activityDAO.getAll().size)
            }
        }

        @Test
        fun `deleting activities when none exist for user id results in no deletion`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(3, activityDAO.getAll().size)
                activityDAO.deleteByUserId(3)
                assertEquals(3, activityDAO.getAll().size)
            }
        }

        @Test
        fun `deleting activities when 1 or more exist for user id results in deletion`() {
            transaction {
                val userDAO = populateUserTable()
                val activityDAO = populateActivityTable()
                assertEquals(3, activityDAO.getAll().size)
                activityDAO.deleteByUserId(1)
                assertEquals(1, activityDAO.getAll().size)
            }
        }
    }
}