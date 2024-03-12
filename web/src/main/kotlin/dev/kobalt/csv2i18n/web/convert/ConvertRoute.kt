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

package dev.kobalt.csv2i18n.web.convert

import dev.kobalt.csv2i18n.web.extension.respondHtmlContent
import dev.kobalt.csv2i18n.web.html.LimitedSizeInputStream
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import java.io.ByteArrayOutputStream


fun Route.convertRoute() {
    route("/") {
        get {
            call.respondHtmlContent(
                title = ConvertRepository.pageTitle,
                description = ConvertRepository.pageSubtitle,
                head = {
                    meta("Content-Security-Policy", "frame-src 'none'")
                    style {
                        unsafe {
                            raw(
                                """
                                .content1,
                                .content2 {
                                    display: none;
                                    padding: 20px;
                                    border-top: 2px solid #999999;
                                }

                                input[type='radio'] {
                                    width: 0;
                                    height: 0;
                                    opacity: 0;
                                }

                                label.tab {
                                    cursor: pointer;
                                    display: inline-flex;
                                    justify-content: center;
                                    align-items: center;
                                    width: 80px;
                                    height: 30px;
                                    background-color: #dddddd;
                                    border-style: solid solid none solid;
                                    border-width: 2px;
                                    border-color: transparent;
                                }

                                #tab1:checked+label {
                                    border-color: #999999;
                                }

                                #tab2:checked+label {
                                    border-color: #999999;
                                }

                                #tab1:checked~.content1 {
                                    display: block;
                                }

                                #tab2:checked~.content2 {
                                    display: block;
                                }
                            """.trimIndent()
                            )
                        }
                    }
                }
            ) {
                div("tab-layout") {
                    input(InputType.radio) {
                        name = "tab"
                        id = "tab1"
                        checked = true
                    }
                    label {
                        classes = setOf("tab")
                        htmlFor = "tab1"
                        text("File")
                    }
                    input(InputType.radio) {
                        name = "tab"
                        id = "tab2"
                        checked = false
                    }
                    label {
                        classes = setOf("tab")
                        htmlFor = "tab2"
                        text("Text")
                    }
                    div("content1") {
                        style = "background: red; width: 100%; height: 100%;"
                        form {
                            method = FormMethod.post
                            encType = FormEncType.multipartFormData
                            name = "messagebox"
                            label { text("File") }
                            input { type = InputType.file; name = "input" }
                            input { type = InputType.submit; name = "submit"; value = "Submit" }
                        }
                    }
                    div("content2") {
                        style = "background: green; width: 100%; height: 100%;"
                        form {
                            method = FormMethod.post
                            encType = FormEncType.multipartFormData
                            name = "messagebox"
                            label { text("Text") }
                            textArea { name = "input" }
                            input { type = InputType.submit; name = "submit"; value = "Submit" }
                        }
                    }
                }
            }
        }
        post {
            runCatching {
                val data = when (val part = call.receiveMultipart().readAllParts().find { it.name == "input" }) {
                    is PartData.FileItem -> LimitedSizeInputStream(part.streamProvider(), 500 * 1024).readBytes()
                        .decodeToString()

                    is PartData.FormItem -> part.value.takeIf { it.length < 500 * 1024 } ?: throw Exception()
                    else -> throw Exception()
                }
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "output.zip")
                        .toString()
                )
                call.respondBytes(
                    contentType = ContentType.Application.Zip,
                    status = HttpStatusCode.OK,
                    bytes = ByteArrayOutputStream().use {
                        ConvertRepository.submit(data, it)
                        it.toByteArray()
                    }
                )
            }.getOrElse {
                call.respondHtmlContent(
                    title = ConvertRepository.pageTitle,
                    description = ConvertRepository.pageSubtitle
                ) {
                    h3 { text("Failure") }
                    p { text("Conversion process was not successful.") }
                }
            }
        }
    }
}

