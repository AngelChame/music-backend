package com.musicapi.models.dtos

import kotlinx.serialization.Serializable


@Serializable
data class TrackDTO(
    val id: String,
    val title: String,
    val duration: Int,
    val albumId: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateTrackDTO(
    val title: String,
    val duration: Int,
    val albumId: String
)

@Serializable
data class UpdateTrackDTO(
    val title: String?,
    val duration: Int?,
    val albumId: String?
)