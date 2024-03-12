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

package dev.kobalt.csv2i18n.jvm

import dev.kobalt.csv2i18n.jvm.converter.Csv2i18nConverter
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import java.io.BufferedInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


fun main(args: Array<String>) {
    val parser = ArgParser("csv2i18n")
    val csvPath by parser.option(ArgType.String, "csvPath", null, null)
    parser.parse(args)
    val converter = Csv2i18nConverter()
    ((csvPath?.let { File(it).readText() }) ?: run {
        BufferedInputStream(System.`in`).use {
            if (it.available() > 0) it.readBytes().decodeToString() else null
        }
    })?.let { input ->
        ZipOutputStream(System.out).use { outputStream ->
            converter.convert(input).let { data ->
                data.forEach { output ->
                    output.data.forEachIndexed { index, it ->
                        val entry = ZipEntry("${output.type}-${it.first}.${output.extension}")
                        outputStream.putNextEntry(entry)
                        outputStream.write(it.second.toByteArray())
                        outputStream.closeEntry()
                    }
                }
            }
        }
    }
}