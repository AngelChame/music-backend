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