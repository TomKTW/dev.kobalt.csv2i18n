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

import dev.kobalt.csv2i18n.web.resource.ResourceRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.*

object ConvertRepository {

    val pageTitle = "Convert"
    val pageSubtitle = "Upload your content in this form to convert it."
    val pageRoute = "convert/"
    val pageContent = ResourceRepository.getText("convert.md")!!

    var jarPath: String? = null

    private val semaphore = Semaphore(5)

    suspend fun convert(data: String, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(
            "java",
            "-jar", jarPath!!
        ).start()
        val stdout = BufferedInputStream(process.inputStream)
        val stderr = BufferedReader(InputStreamReader(process.errorStream))
        val stdin = BufferedWriter(OutputStreamWriter(process.outputStream))
        val stdinJob = launch(Dispatchers.IO) {
            stdin.write(data)
            stdin.flush()
            stdin.close()
        }
        val stderrJob = launch(Dispatchers.IO) {
            println(stderr.readText())
        }
        val stdoutJob = async(Dispatchers.IO) {
            outputStream.write(stdout.readBytes())
        }
        stdinJob.join()
        stderrJob.join()
        stdoutJob.await()
        outputStream
    }

    suspend fun submit(data: String, outputStream: OutputStream): OutputStream {
        return withContext(Dispatchers.IO) {
            semaphore.withPermit {
                withTimeout(5000) {
                    convert(data, outputStream)
                }
            }
        }
    }

}