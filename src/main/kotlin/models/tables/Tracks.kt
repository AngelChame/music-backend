package com.musicapi.models.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import java.time.Instant

object Tracks : Table("tracks") {
    val id = uuid("id").autoGenerate().uniqueIndex()
    val title = varchar("title", 150)
    val duration = integer("duration").check { it greater 0 }
    val albumId = uuid("album_id").references(Albums.id)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)
}