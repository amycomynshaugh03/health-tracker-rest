package ie.setu.controllers

import ie.setu.domain.User
import ie.setu.domain.repository.UserDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

object UserController {

    private val userDao = UserDAO()

    fun getAllUsers(ctx: Context) {
        val users = userDao.getAll()
        if (users.size != 0) {
            ctx.status(200)
        }
        else {
            ctx.status(404)
        }
        ctx.json(users)
    }

    fun getUserByUserId(ctx: Context) {
        val user = userDao.findById(ctx.pathParam("user-id").toInt())
        if (user != null) {
            ctx.json(user)
            ctx.status(200)
        }
        else {
            ctx.status(404)
        }
    }

    fun addUser(ctx: Context) {
        val user: User = jsonToObject(ctx.body())
        val userId = userDao.save(user)
        if (userId != null) {
            user.id = userId
            ctx.json(user)
            ctx.status(201)
        }
    }

    fun getUserByEmail(ctx: Context) {
        val email = ctx.pathParam("email")
        val user = userDao.findByEmail(email)
        if (user != null) {
            ctx.json(user)
            ctx.status(200)
        }
        else {
            ctx.status(404)
        }
    }

    fun deleteUser(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        if (userDao.delete(userId) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun updateUser(ctx: Context) {
        val userId = ctx.pathParam("user-id").toInt()
        val userUpdates: User = jsonToObject(ctx.body())
        if (userDao.update(userId, userUpdates) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }
}