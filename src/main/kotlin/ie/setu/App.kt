package ie.setu

import ie.setu.config.DbConfig
import ie.setu.config.ServerConfig

fun main() {
    DbConfig().getDbConnection()
    ServerConfig().startJavalinService()
}
