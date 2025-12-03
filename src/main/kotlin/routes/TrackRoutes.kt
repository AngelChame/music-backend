package com.musicapi.routes

import com.musicapi.models.dtos.*
import com.musicapi.models.mappers.*
import com.musicapi.repositories.TrackRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.trackRoutes() {
    val repository = TrackRepository()

    route("/tracks") {
        get {
            try {
                val albumId = call.request.queryParameters["albumId"]

                val tracks = if (albumId != null) {
                    repository.getByAlbumId(UUID.fromString(albumId))
                } else {
                    repository.getAll()
                }

                val dtos = tracks.map { it.toDTO() }
                call.respond(HttpStatusCode.OK, dtos)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Error desconocido")
                )
            }
        }

        get("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val track = repository.getById(id)

                if (track != null) {
                    call.respond(HttpStatusCode.OK, track.toDTO())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Track no encontrado")
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_UUID", "ID inválido")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Error desconocido")
                )
            }
        }

        post {
            try {
                val createDTO = call.receive<CreateTrackDTO>()

                if (createDTO.title.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", "El título es requerido")
                    )
                    return@post
                }

                if (createDTO.duration <= 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", "La duración debe ser mayor a 0")
                    )
                    return@post
                }

                val domainModel = createDTO.toDomain()
                val createdTrack = repository.create(domainModel)

                call.respond(HttpStatusCode.Created, createdTrack.toDTO())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("CREATION_ERROR", e.message ?: "Error al crear track")
                )
            }
        }

        put("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val updateDTO = call.receive<UpdateTrackDTO>()

                updateDTO.duration?.let { duration ->
                    if (duration <= 0) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("VALIDATION_ERROR", "La duración debe ser mayor a 0")
                        )
                        return@put
                    }
                }

                val existingTrack = repository.getById(id)
                if (existingTrack == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Track no encontrado")
                    )
                    return@put
                }

                val updatedTrack = existingTrack.copy(
                    title = updateDTO.title ?: existingTrack.title,
                    duration = updateDTO.duration ?: existingTrack.duration,
                    albumId = updateDTO.albumId?.let { UUID.fromString(it) } ?: existingTrack.albumId,
                    updatedAt = java.time.Instant.now()
                )

                val result = repository.update(id, updatedTrack)

                if (result != null) {
                    call.respond(HttpStatusCode.OK, result.toDTO())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Track no encontrado")
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_UUID", "ID inválido")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("UPDATE_ERROR", e.message ?: "Error al actualizar")
                )
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val deleted = repository.delete(id)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Track eliminado exitosamente")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Track no encontrado")
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_UUID", "ID inválido")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Error desconocido")
                )
            }
        }
    }
}