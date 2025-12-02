package com.musicapi.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Artists : Table("artistas") {
    val id = uuid("id").autoGenerate().uniqueIndex()
    val name = varchar("name", 100)
    val genre = varchar("genre", 50).nullable()
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)
}