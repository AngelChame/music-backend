package com.musicapi.repositories

import com.musicapi.database.dbQuery
import com.musicapi.models.domain.Artist
import com.musicapi.models.dtos.AlbumWithTracksDTO
import com.musicapi.models.dtos.ArtistWithRelationsDTO
import com.musicapi.models.dtos.RelatedRecords
import com.musicapi.models.dtos.TrackDTO
import com.musicapi.models.tables.*
import com.musicapi.models.mappers.toArtist
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class ArtistRepository {

    suspend fun getAll(): List<Artist> = dbQuery {
        Artists.selectAll()
            .map { it.toArtist() }
    }

    suspend fun getById(id: UUID): Artist? = dbQuery {
        Artists.select { Artists.id eq id }
            .map { it.toArtist() }
            .singleOrNull()
    }

    suspend fun create(artist: Artist): Artist = dbQuery {
        Artists.insert {
            it[id] = artist.id
            it[name] = artist.name
            it[genre] = artist.genre
            it[createdAt] = artist.createdAt
            it[updatedAt] = artist.updatedAt
        }
        artist
    }

    suspend fun update(id: UUID, updatedArtist: Artist): Artist? = dbQuery {
        val exists = Artists.select { Artists.id eq id }.singleOrNull()
        if (exists == null) return@dbQuery null

        Artists.update({ Artists.id eq id }) {
            it[name] = updatedArtist.name
            it[genre] = updatedArtist.genre
            it[updatedAt] = Instant.now()
        }

        Artists.select { Artists.id eq id }
            .map { it.toArtist() }
            .single()
    }

    suspend fun checkDependencies(id: UUID): RelatedRecords = dbQuery {
        val albumsCount = Albums.select { Albums.artistId eq id }.count().toInt()
        RelatedRecords(albumsCount = albumsCount)
    }

    suspend fun delete(id: UUID, force: Boolean = false): Boolean {
        val dependencies = checkDependencies(id)

        if (!force && dependencies.albumsCount!! > 0) {
            throw Exception("No se puede eliminar: el artista tiene ${dependencies.albumsCount} álbum(es) relacionado(s)")
        }

        return dbQuery {
            if (force && dependencies.albumsCount!! > 0) {
                val albumIds = Albums.select { Albums.artistId eq id }
                    .map { it[Albums.id] }

                Tracks.deleteWhere { albumId inList albumIds }
                Albums.deleteWhere { artistId eq id }
            }

            Artists.deleteWhere { Artists.id eq id } > 0
        }
    }
    suspend fun getByIdWithRelations(id: UUID): ArtistWithRelationsDTO? = dbQuery {
        val artistRow = Artists.select { Artists.id eq id }.singleOrNull() ?: return@dbQuery null

        val albums = Albums.select { Albums.artistId eq id }.map { albumRow ->
            val tracks = Tracks.select { Tracks.albumId eq albumRow[Albums.id] }.map { trackRow ->
                TrackDTO(
                    id = trackRow[Tracks.id].toString(),
                    title = trackRow[Tracks.title],
                    duration = trackRow[Tracks.duration],
                    albumId = trackRow[Tracks.albumId].toString(),
                    createdAt = trackRow[Tracks.createdAt].toString(),
                    updatedAt = trackRow[Tracks.updatedAt].toString()
                )
            }

            AlbumWithTracksDTO(
                id = albumRow[Albums.id].toString(),
                title = albumRow[Albums.title],
                releaseYear = albumRow[Albums.releaseYear],
                artistId = albumRow[Albums.artistId].toString(),
                createdAt = albumRow[Albums.createdAt].toString(),
                updatedAt = albumRow[Albums.updatedAt].toString(),
                tracks = tracks
            )
        }

        ArtistWithRelationsDTO(
            id = artistRow[Artists.id].toString(),
            name = artistRow[Artists.name],
            genre = artistRow[Artists.genre],
            createdAt = artistRow[Artists.createdAt].toString(),
            updatedAt = artistRow[Artists.updatedAt].toString(),
            albums = albums
        )
    }
}