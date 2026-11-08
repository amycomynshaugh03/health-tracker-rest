package ie.setu.controllers

import ie.setu.domain.Activity
import ie.setu.domain.repository.ActivityDAO
import ie.setu.domain.repository.UserDAO
import ie.setu.utils.jsonToObject
import io.javalin.http.Context

object ActivityController {

    private val userDao = UserDAO()
    private val activityDAO = ActivityDAO()

    fun getAllActivities(ctx: Context) {
        val activities = activityDAO.getAll()
        if (activities.isNotEmpty()) {
            ctx.status(200)
        } else {
            ctx.status(404)
        }
        ctx.json(activities)
    }

    fun getActivitiesByUserId(ctx: Context) {
        if (userDao.findById(ctx.pathParam("user-id").toInt()) != null) {
            val activities = activityDAO.findByUserId(ctx.pathParam("user-id").toInt())
            if (activities.isNotEmpty()) {
                ctx.json(activities)
                ctx.status(200)
            } else {
                ctx.status(404)
            }
        } else {
            ctx.status(404)
        }
    }

    fun getActivityByActivityId(ctx: Context) {
        val activity = activityDAO.findByActivityId(ctx.pathParam("activity-id").toInt())
        if (activity != null) {
            ctx.json(activity)
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    fun addActivity(ctx: Context) {
        val activity: Activity = jsonToObject(ctx.body())
        if (userDao.findById(activity.userId) != null) {
            activity.id = activityDAO.save(activity)
            ctx.json(activity)
            ctx.status(201)
        } else {
            ctx.status(404)
        }
    }

    fun deleteActivityByUserId(ctx: Context) {
        if (activityDAO.deleteByUserId(ctx.pathParam("user-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun deleteActivityByActivityId(ctx: Context) {
        if (activityDAO.deleteByActivityId(ctx.pathParam("activity-id").toInt()) != 0)
            ctx.status(204)
        else
            ctx.status(404)
    }

    fun updateActivity(ctx: Context) {
        val activity: Activity = jsonToObject(ctx.body())
        if (activityDAO.updateByActivityId(
                activityId = ctx.pathParam("activity-id").toInt(),
                activityToUpdate = activity
            ) != 0
        )
            ctx.status(204)
        else
            ctx.status(404)
    }
}