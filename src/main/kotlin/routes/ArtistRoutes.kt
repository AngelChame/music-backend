package com.musicapi.routes

import com.musicapi.models.dtos.*
import com.musicapi.models.mappers.*
import com.musicapi.repositories.ArtistRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.artistRoutes() {
    val repository = ArtistRepository()

    route("/artistas") {
        get {
            try {
                val artists = repository.getAll()
                val dtos = artists.map { it.toDTO() }
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
                val artist = repository.getById(id)

                if (artist != null) {
                    call.respond(HttpStatusCode.OK, artist.toDTO())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Artista no encontrado")
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
                val createDTO = call.receive<CreateArtistDTO>()

                if (createDTO.name.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", "El nombre es requerido")
                    )
                    return@post
                }

                val domainModel = createDTO.toDomain()
                val createdArtist = repository.create(domainModel)

                call.respond(HttpStatusCode.Created, createdArtist.toDTO())
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", e.message ?: "Error desconocido")
                )
            }
        }

        put("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val updateDTO = call.receive<UpdateArtistDTO>()

                val existingArtist = repository.getById(id)
                if (existingArtist == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Artista no encontrado")
                    )
                    return@put
                }

                val updatedArtist = existingArtist.copy(
                    name = updateDTO.name ?: existingArtist.name,
                    genre = updateDTO.genre ?: existingArtist.genre,
                    updatedAt = java.time.Instant.now()
                )

                val result = repository.update(id, updatedArtist)

                if (result != null) {
                    call.respond(HttpStatusCode.OK, result.toDTO())
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Artista no encontrado")
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

        get("/{id}/check-delete") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val dependencies = repository.checkDependencies(id)

                val canDelete = dependencies.albumsCount == 0
                val message = if (canDelete) {
                    "El artista se puede eliminar de forma segura"
                } else {
                    "El artista tiene ${dependencies.albumsCount} álbum(es) relacionado(s)"
                }

                call.respond(
                    HttpStatusCode.OK,
                    DeleteValidationResponse(canDelete, message, dependencies)
                )
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

        delete("/{id}") {
            try {
                val id = UUID.fromString(call.parameters["id"])
                val force = call.request.queryParameters["force"]?.toBoolean() ?: false

                val deleted = repository.delete(id, force)

                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        SuccessResponse("Artista eliminado exitosamente")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Artista no encontrado")
                    )
                }
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("INVALID_UUID", "ID inválido")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("DELETE_CONFLICT", e.message ?: "No se puede eliminar")
                )
            }
        }
    }
}