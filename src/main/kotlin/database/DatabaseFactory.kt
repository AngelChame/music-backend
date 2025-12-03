package com.musicapi.database

import com.musicapi.models.tables.Albums
import com.musicapi.models.tables.Artists
import com.musicapi.models.tables.Tracks
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.musicapi.models.tables.*
import io.ktor.server.config.ApplicationConfig

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val dbConfig = config.config("db")

        val database = Database.connect(hikari(dbConfig))

        transaction(database) {
            SchemaUtils.create(Artists, Albums, Tracks)
        }
    }

    private fun hikari(dbConfig: ApplicationConfig): HikariDataSource {
        val hikariConfig = HikariConfig().apply {
            driverClassName = dbConfig.property("driver").getString()
            jdbcUrl = dbConfig.property("url").getString()
            username = dbConfig.property("user").getString()
            password = dbConfig.property("password").getString()
            maximumPoolSize = dbConfig.property("poolSize").getString().toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(hikariConfig)
    }
}


suspend fun <T> dbQuery(block: () -> T): T =
    org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction { block() }