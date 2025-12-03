package com.musicapi.routes

import com.musicapi.models.dtos.*
import com.musicapi.models.mappers.*
import com.musicapi.repositories.AlbumRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.albumRoutes() {
    val repository = AlbumRepository()

    route("/albumes") {
        get {
            try {
                val artistId = call.request.queryParameters["artistId"]

                val albums = if (artistId != null) {
                    repository.getByArtistId(UUID.fromString(artistId))
                } else {
                    repository.getAll()
                }

                val dtos = albums.map { it.toDTO() }
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
                val album = repository.getById(id)

                if (album != null) {
                    call.respond(HttpStatusCode.OK, album.toDTO())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Álbum no encontrado")
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
                val createDTO = call.receive<CreateAlbumDTO>()

                if (createDTO.title.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", "El título es requerido")
                    )
                    return@post
                }

                if (createDTO.releaseYear < 1900) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", "El año debe ser mayor a 1900")
                    )
                    return@post
                }

                val domainModel = createDTO.toDomain()
                val createdAlbum = repository.create(domainModel)

                call.respond(HttpStatusCode.Created, createdAlbum.toDTO())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("CREATION_ERROR", e.message ?: "Error al crear álbum")
                )
            }
        }

        put("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val updateDTO = call.receive<UpdateAlbumDTO>()

                val existingAlbum = repository.getById(id)
                if (existingAlbum == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Álbum no encontrado")
                    )
                    return@put
                }

                val updatedAlbum = existingAlbum.copy(
                    title = updateDTO.title ?: existingAlbum.title,
                    releaseYear = updateDTO.releaseYear ?: existingAlbum.releaseYear,
                    artistId = updateDTO.artistId?.let { UUID.fromString(it) } ?: existingAlbum.artistId,
                    updatedAt = java.time.Instant.now()
                )

                val result = repository.update(id, updatedAlbum)

                if (result != null) {
                    call.respond(HttpStatusCode.OK, result.toDTO())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Álbum no encontrado")
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

        get("/{id}/check-delete") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val dependencies = repository.checkDependencies(id)

                val canDelete = dependencies.tracksCount == 0
                val message = if (canDelete) {
                    "El álbum se puede eliminar de forma segura"
                } else {
                    "El álbum tiene ${dependencies.tracksCount} track(s) relacionado(s)"
                }

                call.respond(
                    HttpStatusCode.OK,
                    DeleteValidationResponse(canDelete, message, dependencies)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Error desconocido")
                )
            }
        }

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val force = call.request.queryParameters["force"]?.toBoolean() ?: false

                val deleted = repository.delete(id, force)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Álbum eliminado exitosamente")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Álbum no encontrado")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("DELETE_CONFLICT", e.message ?: "No se puede eliminar")
                )
            }
        }
    }
}