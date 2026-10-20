package ie.setu.domain.repository

import ie.setu.domain.User
import ie.setu.domain.db.Users
import ie.setu.utils.mapToUser
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

class UserDAO {

    fun getAll(): ArrayList<User> {
        val userList: ArrayList<User> = arrayListOf()
        transaction {
            Users.selectAll().map {
                userList.add(mapToUser(it)) }
        }
        return userList
    }

    fun findById(id: Int): User?{
        return transaction {
            Users.selectAll().where { Users.id eq id }
                .map{mapToUser(it)}
                .firstOrNull()
        }
    }

    fun save(user: User){
        transaction {
            Users.insert {
                it[name] = user.name
                it[email] = user.email
            }
        }
    }

    fun findByEmail(email: String) :User?{
        return transaction {
            Users.selectAll().where { Users.email eq email}
                .map{mapToUser(it)}
                .firstOrNull()
        }
    }

    fun delete(id: Int):Int{
        return transaction{
            Users.deleteWhere{ Users.id eq id }
        }
    }

    fun update(id: Int, user: User){
        transaction {
            Users.update ({
                Users.id eq id}) {
                it[name] = user.name
                it[email] = user.email
            }
        }
    }


}
