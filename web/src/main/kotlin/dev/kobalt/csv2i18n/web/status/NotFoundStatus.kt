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

package dev.kobalt.csv2i18n.web.status

import dev.kobalt.csv2i18n.web.extension.respondHtmlContent
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import kotlinx.html.h1
import kotlinx.html.p


fun StatusPages.Configuration.notFoundStatus() = status(HttpStatusCode.NotFound) {
    val pageTitle = "Not found"
    val pageSubtitle = "This page doesn't exist."
    call.respondHtmlContent(
        title = pageTitle,
        description = pageSubtitle
    ) {
        h1 { text(pageTitle) }
        p { text(pageSubtitle) }
    }
}