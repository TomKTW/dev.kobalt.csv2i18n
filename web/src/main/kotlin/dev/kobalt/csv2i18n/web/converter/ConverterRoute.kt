/*
 * dev.kobalt.csv2i18n
 * Copyright (C) 2022 Tom.K
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.kobalt.csv2i18n.web.converter

import dev.kobalt.csv2i18n.web.inputstream.InputStreamSizeLimitReachedException
import dev.kobalt.csv2i18n.web.inputstream.LimitedSizeInputStream
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.ByteArrayOutputStream

fun Route.converterRoute() {
    get {
        call.respondText(application.converter.getIndexPageContent(), ContentType.Text.Html)
    }
    post {
        runCatching {
            // Extract the data.
            val data = when (val part = call.receiveMultipart().readAllParts().find { it.name == "input" }) {
                is PartData.FileItem -> LimitedSizeInputStream(part.streamProvider(), 500 * 1024).readBytes()
                    .decodeToString()

                is PartData.FormItem -> part.value.takeIf { it.length < 500 * 1024 } ?: throw Exception()
                else -> throw Exception()
            }
            // Convert the data.
            val bytes = ByteArrayOutputStream().use {
                application.converter.submit(data, it)
                it.toByteArray()
            }
            // Apply header after conversion to prevent downloading failed page.
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName, "output.zip"
                ).toString()
            )
            // Respond with zipped output stream file.
            call.respondBytes(
                contentType = ContentType.Application.Zip,
                status = HttpStatusCode.OK,
                bytes = bytes
            )
        }.getOrElse {
            call.respondText(
                application.converter.getFailurePageContent().replace(
                    "\$cause\$", when (it) {
                        is InputStreamSizeLimitReachedException -> "Submitted content is bigger than size limit (500 kB)."
                        else -> "Conversion was not successful."
                    }
                ),
                ContentType.Text.Html
            )
        }
    }
}

