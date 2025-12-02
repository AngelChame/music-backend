package com.musicapi.models.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ArtistDTO(
    val id: String,
    val name: String,
    val genre: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateArtistDTO(
    val name: String,
    val genre: String?
)

@Serializable
data class UpdateArtistDTO(
    val name: String?,
    val genre: String?
)