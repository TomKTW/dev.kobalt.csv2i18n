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

package dev.kobalt.csv2i18n.jvm.converter

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

class Csv2i18nConverter {

    private val parser = csvReader()

    private fun String.fromCsvWithHeader(): List<Map<String, String>> = parser.readAllWithHeader(this)

    fun convert(csv: String): List<Output> {
        val list = csv.fromCsvWithHeader()
        val languages = list.firstOrNull()?.keys?.drop(1).orEmpty()
        val entries = list.map { item ->
            (item.values.firstOrNull().orEmpty()) to (item.filterKeys { languages.contains(it) }
                .map { it.key to it.value })
        }
        val androidResult = languages.map { language ->
            language to StringBuilder().apply {
                append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n")
                entries.forEach { entry ->
                    entry.second.find { it.first == language }?.let {
                        append("\t<string name=\"${entry.first}\">${it.second}</string>\n")
                    }
                }
                append("</resources>")
            }.toString()
        }
        val iosResult = languages.map { language ->
            language to StringBuilder().apply {
                entries.forEach { entry ->
                    entry.second.find { it.first == language }?.let {
                        append("\"${entry.first}\" = \"${it.second}\";\n")
                    }
                }
            }.removeSuffix("\n").toString()
        }
        return listOf(Output("Android", "xml", androidResult), Output("iOS", "strings", iosResult))
    }

    data class Output(
        val type: String,
        val extension: String,
        val data: List<Pair<String, String>>
    )

}