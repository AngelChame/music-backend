package com.musicapi.repositories

import com.musicapi.database.dbQuery
import com.musicapi.models.domain.Track
import com.musicapi.models.tables.*
import com.musicapi.models.mappers.toTrack
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class TrackRepository {

    suspend fun getAll(): List<Track> = dbQuery {
        Tracks.selectAll()
            .map { it.toTrack() }
    }

    suspend fun getById(id: UUID): Track? = dbQuery {
        Tracks.select { Tracks.id eq id }
            .map { it.toTrack() }
            .singleOrNull()
    }

    suspend fun getByAlbumId(albumId: UUID): List<Track> = dbQuery {
        Tracks.select { Tracks.albumId eq albumId }
            .map { it.toTrack() }
    }

    suspend fun create(track: Track): Track = dbQuery {
        val albumExists = Albums.select { Albums.id eq track.albumId }
            .count() > 0

        if (!albumExists) {
            throw Exception("El álbum con ID ${track.albumId} no existe")
        }

        Tracks.insert {
            it[id] = track.id
            it[title] = track.title
            it[duration] = track.duration
            it[albumId] = track.albumId
            it[createdAt] = track.createdAt
            it[updatedAt] = track.updatedAt
        }
        track
    }

    suspend fun update(id: UUID, updatedTrack: Track): Track? = dbQuery {
        val exists = Tracks.select { Tracks.id eq id }.singleOrNull()
        if (exists == null) return@dbQuery null

        val albumExists = Albums.select { Albums.id eq updatedTrack.albumId }
            .count() > 0

        if (!albumExists) {
            throw Exception("El álbum con ID ${updatedTrack.albumId} no existe")
        }

        Tracks.update({ Tracks.id eq id }) {
            it[title] = updatedTrack.title
            it[duration] = updatedTrack.duration
            it[albumId] = updatedTrack.albumId
            it[updatedAt] = Instant.now()
        }

        Tracks.select { Tracks.id eq id }
            .map { it.toTrack() }
            .single()
    }

    suspend fun delete(id: UUID): Boolean = dbQuery {
        Tracks.deleteWhere { Tracks.id eq id } > 0
    }
}