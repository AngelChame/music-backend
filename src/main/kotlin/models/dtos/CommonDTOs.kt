package com.musicapi.models.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

@Serializable
data class SuccessResponse(
    val message: String
)

@Serializable
data class DeleteValidationResponse(
    val canDelete: Boolean,
    val message: String,
    val relatedRecords: RelatedRecords? = null
)

@Serializable
data class RelatedRecords(
    val albumsCount: Int? = null,
    val tracksCount: Int? = null
)

@Serializable
data class ArtistWithRelationsDTO(
    val id: String,
    val name: String,
    val genre: String?,
    val createdAt: String,
    val updatedAt: String,
    val albums: List<AlbumWithTracksDTO> = emptyList()
)

@Serializable
data class AlbumWithTracksDTO(
    val id: String,
    val title: String,
    val releaseYear: Int,
    val artistId: String,
    val createdAt: String,
    val updatedAt: String,
    val tracks: List<TrackDTO> = emptyList()
)
