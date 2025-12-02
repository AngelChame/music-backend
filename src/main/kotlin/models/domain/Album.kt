package com.musicapi.models.domain

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID
import java.time.Instant

data class Album(
    val id: UUID,
    val title: String,
    val releaseYear: Int,
    val artistId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
)