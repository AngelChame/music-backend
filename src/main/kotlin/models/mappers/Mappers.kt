package com.musicapi.models.mappers

import com.musicapi.models.domain.*
import com.musicapi.models.dtos.*
import com.musicapi.models.tables.*
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

// MAPPERS: ResultRow (DB) → Domain Model

fun ResultRow.toArtist() = Artist(
    id = this[Artists.id],
    name = this[Artists.name],
    genre = this[Artists.genre],
    createdAt = this[Artists.createdAt],
    updatedAt = this[Artists.updatedAt]
)

fun ResultRow.toAlbum() = Album(
    id = this[Albums.id],
    title = this[Albums.title],
    releaseYear = this[Albums.releaseYear],
    artistId = this[Albums.artistId],
    createdAt = this[Albums.createdAt],
    updatedAt = this[Albums.updatedAt]
)

fun ResultRow.toTrack() = Track(
    id = this[Tracks.id],
    title = this[Tracks.title],
    duration = this[Tracks.duration],
    albumId = this[Tracks.albumId],
    createdAt = this[Tracks.createdAt],
    updatedAt = this[Tracks.updatedAt]
)

// MAPPERS: Domain Model → DTO (API)

fun Artist.toDTO() = ArtistDTO(
    id = this.id.toString(),
    name = this.name,
    genre = this.genre,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)

fun Album.toDTO() = AlbumDTO(
    id = this.id.toString(),
    title = this.title,
    releaseYear = this.releaseYear,
    artistId = this.artistId.toString(),
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)

fun Track.toDTO() = TrackDTO(
    id = this.id.toString(),
    title = this.title,
    duration = this.duration,
    albumId = this.albumId.toString(),
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)

// MAPPERS: Create DTO → Domain Model

fun CreateArtistDTO.toDomain(id: UUID = UUID.randomUUID()) = Artist(
    id = id,
    name = this.name,
    genre = this.genre,
    createdAt = java.time.Instant.now(),
    updatedAt = java.time.Instant.now()
)

fun CreateAlbumDTO.toDomain(id: UUID = UUID.randomUUID()) = Album(
    id = id,
    title = this.title,
    releaseYear = this.releaseYear,
    artistId = UUID.fromString(this.artistId),
    createdAt = java.time.Instant.now(),
    updatedAt = java.time.Instant.now()
)

fun CreateTrackDTO.toDomain(id: UUID = UUID.randomUUID()) = Track(
    id = id,
    title = this.title,
    duration = this.duration,
    albumId = UUID.fromString(this.albumId),
    createdAt = java.time.Instant.now(),
    updatedAt = java.time.Instant.now()
)