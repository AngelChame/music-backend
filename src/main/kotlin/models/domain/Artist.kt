package com.musicapi.models.domain


import java.time.Instant
import java.util.UUID

data class Artist(
    val id: UUID,
    val name: String,
    val genre: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)