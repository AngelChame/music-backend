package com.musicapi.models.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AlbumDTO(
    val id: String,
    val title: String,
    val releaseYear: Int,
    val artistId: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateAlbumDTO(
    val title: String,
    val releaseYear: Int,
    val artistId: String
)

@Serializable
data class UpdateAlbumDTO(
    val title: String?,
    val releaseYear: Int?,
    val artistId: String?
)