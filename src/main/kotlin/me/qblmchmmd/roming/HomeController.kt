package me.qblmchmmd.roming

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import javafx.util.Callback
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLDecoder
import java.util.*

class DownloadRomDialog(rom: Rom) {
    private val alert = Alert(Alert.AlertType.NONE)
    private val progressBar = ProgressBar(0.0)

    val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        alert.title = "Download ${rom.title}"
        alert.buttonTypes.add(ButtonType.CLOSE)
        val link = rom.links?.firstOrNull()
        val url = link?.url
        if (url == null) {
            alert.headerText = "Cannot Download BAD URL"
        } else {
            alert.headerText = "Downloading ${rom.title}: 0%"
            val content = VBox(10.0, progressBar)
            progressBar.maxWidth = Double.MAX_VALUE
            alert.dialogPane.content = content

            alert.resultProperty().addListener { _, _, newValue ->
                if (newValue == ButtonType.CLOSE) {
                    downloadScope.cancel()
                    alert.close()
                }
            }
            val downloadsPath = System.getProperty("user.home") + "/Downloads"
            val outputFile = File(downloadsPath, URLDecoder.decode(link.url.substringAfterLast('/'), Charsets.UTF_8))
            downloadScope.launch {
                val client = HttpClient(Java)
                try {
                    client.prepareGet(url).execute { response ->
                        if (!response.status.isSuccess()) {
                            withContext(Dispatchers.Main) {
                                alert.headerText = response.status.toString()
                            }
                            return@execute
                        }
                        val totalBytes = response.contentLength() ?: -1L
                        var downloadedBytes = 0L
                        val channel: ByteReadChannel = response.body()
                        FileOutputStream(outputFile).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (!channel.isClosedForRead) {
                                val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                                if (bytesRead > 0) {
                                    output.write(buffer, 0, bytesRead)
                                    downloadedBytes += bytesRead
                                    updateProgress((downloadedBytes.toDouble() / totalBytes.toDouble()))
                                    withContext(Dispatchers.Main) {
                                        alert.headerText = "Downloading ${rom.title}: " +
                                            "${((downloadedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toInt()}%"
                                    }
                                }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            alert.headerText = "Download ${rom.title} Success"
                        }
                    }
                } catch (e: Exception) {
                    println("ERROR $e")
                    if (outputFile.exists()) outputFile.delete()
                    withContext(Dispatchers.Main) {
                        alert.headerText = "Error: ${e.localizedMessage}"
                    }
                } finally {
                    client.close()
                }
            }
        }
    }

    fun show() {
        Platform.runLater { alert.show() }
    }

    fun close() {
        Platform.runLater { alert.close() }
    }

    fun updateProgress(progress: Double) {
        Platform.runLater {
            progressBar.progress = progress
        }
    }
}

class CrocDB {
    private val client = HttpClient(Java) {
        expectSuccess = true
        install(Logging) {
            logger = Logger.DEFAULT
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            url("https://api.crocdb.net")
        }
        install(ContentNegotiation) {
            json(Json {
                explicitNulls = false
            })
        }
    }

    suspend fun search(body: SearchBody = SearchBody()): RemoteResult<Response<SearchResponseData>> {
        return runCatching {
            RemoteResult.Success(client.post("search") {
                setBody(body)
            }.body<Response<SearchResponseData>>())
        }.getOrElse {
            RemoteResult.Error(Exception(it))
        }
    }
}


val cacheImage = mutableMapOf<String, Image>()

fun cacheImage(url: String): Image {
    return cacheImage.getOrPut(url) { Image(url, true) }
}

class RomListCellController {

    @FXML
    private lateinit var image: ImageView

    @FXML
    private lateinit var title: Label

    @FXML
    private lateinit var platform: Label

    @FXML
    private lateinit var region: Label

    @FXML
    private lateinit var download: Button

    fun setData(rom: Rom) {
        title.text = rom.title
        platform.text = rom.platform
        region.text = rom.regions?.joinToString(", ")
        val link = rom.links?.firstOrNull()
        download.isVisible = link?.url != null
        if (link?.url != null) {
            download.text = "Download" + (link.sizeStr?.let { " ($it)" } ?: "")
            download.onAction = EventHandler {
                DownloadRomDialog(rom).show()
            }
        }
        if (rom.boxArtUrl != null)
            image.image = cacheImage(rom.boxArtUrl)
        else
            image.image = null
    }
}

class HomeController : Initializable {

    private val crocDB = CrocDB()

//    @FXML
//    private lateinit var list: ListView<Rom>

    @FXML
    private lateinit var search: TextField

    @FXML
    lateinit var pagination: Pagination

    @FXML
    private fun onSearchAction() {
        if (pagination.currentPageIndex == 0) {
            pagination.pageFactory = Callback { createPage(it) }
        } else {
            pagination.currentPageIndex = 0
        }
        pagination.pageCount = 1

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
    }

    fun createPage(pageIndex: Int): Node {
        val listView = ListView<Rom>()
        listView.cellFactory = Callback {
            object : ListCell<Rom>() {
                private val loader: FXMLLoader by lazy {
                    FXMLLoader(HomeController::class.java.getResource("rom-list-cell.fxml")).also {
                        it.load<Parent>()
                    }
                }
                private val controller: RomListCellController
                    get() = loader.getController()

                override fun updateItem(item: Rom?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (item == null) {
                        graphic = null
                        return
                    }

                    graphic = loader.getRoot()
                    controller.setData(item)
                }
            }
        }
        GlobalScope.launch {
            val result = crocDB.search(SearchBody(searchKey = search.text, page = pageIndex + 1))
            if (result is RemoteResult.Success) {
                result.data.data?.results?.let {
                    withContext(Dispatchers.Main) {
                        result.data.data.totalPages?.let { pagination.pageCount = it }
                        listView.items.addAll(it.filterNotNull())
                        listView.refresh()
                    }
                }
            }
        }
        return listView
    }
}

fun showInfoDialog(message: String) {
    val alert = Alert(AlertType.INFORMATION)
    alert.title = "Information"
    alert.headerText = null
    alert.contentText = message
    alert.showAndWait()
}