package com.musicapi.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant


object Albums : Table("albumes") {
    val id = uuid("id").autoGenerate().uniqueIndex()
    val title = varchar("title", 150)
    val releaseYear = integer("release_year").check { it greaterEq 1900 }
    val artistId = uuid("artist_id").references(Artists.id)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(id)
}