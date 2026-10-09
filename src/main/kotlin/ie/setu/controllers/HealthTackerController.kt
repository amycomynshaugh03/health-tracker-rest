package ie.setu.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ie.setu.domain.User
import ie.setu.domain.repository.UserDAO
import io.javalin.http.Context

object HealthTrackerController {

    private val userDao = UserDAO()

    fun getAllUsers(ctx: Context) {
        ctx.json(userDao.getAll())
    }

    fun getUserByUserId(ctx: Context) {
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null) {
            ctx.json(user)
        }
    }

    fun addUser(ctx: Context) {
        val mapper = jacksonObjectMapper()
        val user = mapper.readValue<User>(ctx.body())
        userDao.save(user)
        ctx.json(user)
    }

    fun getUserByEmail(ctx: Context) {
        val email = ctx.pathParam("email")
        val user = userDao.findByEmail(email)
        if (user != null) {
            ctx.json(user)
        }
    }

    fun deleteUser(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        userDao.delete(userId)
    }

    fun updateUser(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val mapper = jacksonObjectMapper()
        val userUpdates = mapper.readValue<User>(ctx.body())
        userDao.update(userId, userUpdates)
    }
}
