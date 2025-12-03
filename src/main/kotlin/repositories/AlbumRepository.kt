package com.musicapi.repositories

import com.musicapi.database.dbQuery
import com.musicapi.models.domain.Album
import com.musicapi.models.dtos.RelatedRecords
import com.musicapi.models.tables.*
import com.musicapi.models.mappers.toAlbum
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID

class AlbumRepository {

    suspend fun getAll(): List<Album> = dbQuery {
        Albums.selectAll()
            .map { it.toAlbum() }
    }

    suspend fun getById(id: UUID): Album? = dbQuery {
        Albums.select { Albums.id eq id }
            .map { it.toAlbum() }
            .singleOrNull()
    }

    suspend fun getByArtistId(artistId: UUID): List<Album> = dbQuery {
        Albums.select { Albums.artistId eq artistId }
            .map { it.toAlbum() }
    }

    suspend fun create(album: Album): Album = dbQuery {
        val artistExists = Artists.select { Artists.id eq album.artistId }
            .count() > 0

        if (!artistExists) {
            throw Exception("El artista con ID ${album.artistId} no existe")
        }

        Albums.insert {
            it[id] = album.id
            it[title] = album.title
            it[releaseYear] = album.releaseYear
            it[artistId] = album.artistId
            it[createdAt] = album.createdAt
            it[updatedAt] = album.updatedAt
        }
        album
    }

    suspend fun update(id: UUID, updatedAlbum: Album): Album? = dbQuery {
        val exists = Albums.select { Albums.id eq id }.singleOrNull()
        if (exists == null) return@dbQuery null

        val artistExists = Artists.select { Artists.id eq updatedAlbum.artistId }
            .count() > 0

        if (!artistExists) {
            throw Exception("El artista con ID ${updatedAlbum.artistId} no existe")
        }

        Albums.update({ Albums.id eq id }) {
            it[title] = updatedAlbum.title
            it[releaseYear] = updatedAlbum.releaseYear
            it[artistId] = updatedAlbum.artistId
            it[updatedAt] = Instant.now()
        }

        Albums.select { Albums.id eq id }
            .map { it.toAlbum() }
            .single()
    }

    suspend fun checkDependencies(id: UUID): RelatedRecords = dbQuery {
        val tracksCount = Tracks.select { Tracks.albumId eq id }.count().toInt()
        RelatedRecords(tracksCount = tracksCount)
    }

    suspend fun delete(id: UUID, force: Boolean = false): Boolean {
        val dependencies = checkDependencies(id)

        if (!force && dependencies.tracksCount!! > 0) {
            throw Exception("No se puede eliminar: el álbum tiene ${dependencies.tracksCount} track(s) relacionado(s)")
        }

        return dbQuery {
            if (force && dependencies.tracksCount!! > 0) {
                Tracks.deleteWhere { albumId eq id }
            }

            Albums.deleteWhere { Albums.id eq id } > 0
        }
    }
}