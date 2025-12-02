package com.musicapi.models.domain

import java.time.Instant
import java.util.UUID

data class Track(
    val id: UUID,
    val title: String,
    val durationSeconds: Int,
    val albumId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
)