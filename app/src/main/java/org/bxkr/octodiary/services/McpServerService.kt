package org.bxkr.octodiary.services


import androidx.compose.material.icons.Icons
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import org.bxkr.octodiary.BuildConfig
import java.io.*

class McpServerService : Service() {
    private val TAG = "McpServerService"
    private var serverProcess: Process? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MCP Server Service created")
        ensureMcpServerFiles()
        startMcpServer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "MCP Server Service started")
        return START_STICKY
    }

    private fun ensureMcpServerFiles() {
        val contentServerDir = File(applicationContext.filesDir, "content-server")
        val serverFile = File(contentServerDir, "index.js")

        if (!serverFile.exists()) {
            Log.d(TAG, "MCP server files not found, copying from assets")
            contentServerDir.mkdirs()

            try {
                applicationContext.assets.open("content-server/index.js").use { input ->
                    FileOutputStream(serverFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d(TAG, "MCP server files copied successfully")
            } catch (e: IOException) {
                Log.e(TAG, "Error copying MCP server files", e)
            }
        } else {
            Log.d(TAG, "MCP server files already exist")
        }
    }

    private fun startMcpServer() {
        serviceScope.launch {
            try {
                // Путь к MCP серверу
                val serverPath = File(applicationContext.filesDir, "content-server/index.js").absolutePath

                // Проверяем, что Node.js доступен
                val nodeCommand = "node"
                val testProcess = ProcessBuilder(nodeCommand, "--version").start()
                val exitCode = testProcess.waitFor()

                if (exitCode != 0) {
                    Log.e(TAG, "Node.js is not available on this device")
                    return@launch
                }

                val serverArgs = arrayOf(serverPath)
                Log.d(TAG, "Starting MCP server with command: $nodeCommand ${serverArgs.joinToString(" ")}")

                val processBuilder = ProcessBuilder(nodeCommand, *serverArgs).apply {
                    redirectErrorStream(true)
                    directory(File(applicationContext.filesDir, "content-server"))
                    environment()["NODE_PATH"] = applicationContext.filesDir.absolutePath
                }

                serverProcess = processBuilder.start()

                // Читаем вывод сервера в отдельном потоке
                launch {
                    try {
                        val reader = BufferedReader(InputStreamReader(serverProcess!!.inputStream))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            Log.d(TAG, "MCP Server output: $line")
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Error reading MCP server output", e)
                    }
                }

                // Ждем завершения процесса (MCP сервер должен работать постоянно)
                val processExitCode = serverProcess!!.waitFor()
                Log.d(TAG, "MCP server process exited with code: $processExitCode")

            } catch (e: Exception) {
                Log.e(TAG, "Error starting MCP server", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MCP Server Service destroyed")
        serverProcess?.destroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}


