// простите меня за этот ужас

import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.Stage
import javafx.util.Duration.seconds
import kotlinx.coroutines.Runnable
import java.io.File
import java.util.regex.Pattern
import kotlin.math.roundToInt


class TrackInfo (trackNumber : Int, trackName : String, trackDuration : Int) {
    var number: Int = trackNumber
    var name: String = trackName
    var duration: Int = trackDuration
}

class MPlayer : Application() {
    internal var selectedFile: File? = null
    internal var mplayer: MediaPlayer? = null
    internal var musicSlider: Slider = Slider()
    internal var volumeSlider: Slider = Slider()
    internal val filenameLabel: Label = Label("\n")
    internal var table: TableView<TrackInfo> = TableView()
    internal val data: ObservableList<TrackInfo> = FXCollections.observableArrayList<TrackInfo>()

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        var openedFilesPath : Array<String> = arrayOf()
        var openedFilesNames : Array<String> = arrayOf()
        val root = object : BorderPane() {
            init {
                var trackNumber = 1
                val fileChooser = FileChooser()
                fileChooser.title = "Open File"
                fileChooser.extensionFilters.addAll(
                    ExtensionFilter("Audio Files", "*.wav")
                )

                val vbox = object : VBox() {
                    init {
                        children.add(filenameLabel)
                        val hbox = object : HBox() {
                            init {
                                musicSlider.min = 0.0
                                musicSlider.max = 100.0

                                musicSlider.setOnMouseClicked {
                                    println(it.x)
                                    mplayer?.seek(seconds(it.x * 0.75))
                                    musicSlider.value = it.x * 0.75
                                }

                                musicSlider.maxWidth = 100.0
                                musicSlider.minWidth = 100.0
                                volumeSlider.maxWidth = 75.0
                                volumeSlider.value = 100.0

                                children.add(musicSlider)
                                children.add(volumeSlider)
                            }
                        }

                        val hbox2 = object : HBox() {
                            init {
                                val playButton = Button("Play")
                                val pauseButton = Button("Pause")
                                val prevButton = Button("Prev")
                                val nextButton = Button("Next")

                                val stopButton = object : Button("Stop") {
                                    init {
                                        setOnAction { mplayer?.stop() }
                                    }
                                }

                                playButton.setOnAction { mplayer?.play() }
                                pauseButton.setOnAction { mplayer?.pause() }

                                prevButton.setOnAction {
                                    mplayer?.stop()
                                    val prevFileNumber : Int = openedFilesPath.indexOf(selectedFile!!.toString()) - 1
                                    val prevFile : String = if (prevFileNumber >= 0) openedFilesPath[prevFileNumber] else openedFilesPath.last()
                                    filenameLabel.text = " Now playing: " + prevFile.split('/').last().split('.')[0]
                                    mplayer = MediaPlayer(Media(prevFile))
                                    selectedFile = File(prevFile)
                                    mplayer?.play()
                                }

                                nextButton.setOnAction {
                                    mplayer?.stop()
                                    val nextFileNumber : Int = (openedFilesPath.indexOf(selectedFile!!.toString()) + 1) % openedFilesPath.size
                                    val nextFile : String = openedFilesPath[nextFileNumber]
                                    filenameLabel.text = " Now playing: " + nextFile.split('/').last().split('.')[0]
                                    mplayer = MediaPlayer(Media(nextFile))
                                    selectedFile = File(nextFile)
                                    mplayer?.play()
                                }
                                children.addAll(playButton, pauseButton, stopButton, prevButton, nextButton)
                            }
                        }
                        children.add(hbox)
                        children.add(hbox2)
                    }
                }
                center = vbox

                val menubar = object : MenuBar() {
                    init {
                        val menu = object : Menu("File") {
                            init {
                                val selectMenuItem = object : MenuItem("Select") {
                                    init {
                                        setOnAction {
                                            val files : List<File> = fileChooser.showOpenMultipleDialog(primaryStage)
                                            selectedFile = files.first()
                                            if (selectedFile != null) {
                                                var media: Media? = null
                                                mplayer?.stop()

                                                val spacePattern = Pattern.compile(" ")
                                                val url = selectedFile!!.toURI()
                                                //val matcher = spacePattern.matcher(url)
                                                //url = matcher.replaceAll("\\ ")
                                                filenameLabel.text = " Now playing: " + url.toString().split('/').last().split('.')[0]
                                                println(url.toString())
                                                media = Media(url.toString())


                                                mplayer = MediaPlayer(media)
                                                mplayer?.play()
                                                for (file in files)
                                                    if (file.toURI().toString() !in openedFilesPath) {
                                                        val filePath : String = file.toURI().toString()
                                                        val fileName : String = filePath.split('/').last().split('.').first()
                                                        val currentFileDuration : Int = (file.length().toDouble() / 10485760 * 60).roundToInt()
                                                        val trackData = TrackInfo(trackNumber, fileName, currentFileDuration)

                                                        openedFilesPath += filePath
                                                        openedFilesNames += fileName
                                                        data.add(trackData)
                                                        table.items = data
                                                        trackNumber++
                                                    }
                                            }
                                        }
                                    }
                                }
                                val pauseMenuItem = MenuItem("Pause")
                                val playMenuItem = MenuItem("Play")
                                val stopMenuItem = MenuItem("Stop")

                                items.addAll(selectMenuItem, playMenuItem, pauseMenuItem, stopMenuItem)
                            }
                        }
                        menus.add(menu)
                    }
                }
                top = menubar

                table.isEditable = true
                val number = TableColumn<TrackInfo, Int>("Number")
                val name = TableColumn<TrackInfo, String>("Name")
                val duration = TableColumn<TrackInfo, Int>("Duration")
                number.minWidth = 78.0
                name.minWidth = 140.0
                duration.minWidth = 78.0
                number.cellValueFactory = PropertyValueFactory<TrackInfo, Int>("number")
                name.cellValueFactory = PropertyValueFactory<TrackInfo, String>("name")
                duration.cellValueFactory = PropertyValueFactory<TrackInfo, Int>("duration")

                table.columns.addAll(number, name, duration)

                table.setRowFactory {
                    val row: TableRow<TrackInfo> = TableRow()
                    row.setOnMouseClicked { event ->
                        if (event.clickCount == 2 && !row.isEmpty) {
                            val rowData: TrackInfo = row.item
                            mplayer?.stop()
                            filenameLabel.text = " Now playing: " + rowData.name
                            mplayer = MediaPlayer(Media(openedFilesPath[openedFilesNames.indexOf(rowData.name)]))
                            selectedFile = File(openedFilesPath[openedFilesNames.indexOf(rowData.name)])
                            mplayer?.play()
                        }
                    }

                    row
                }
                bottom = table
            }
        }

        Thread(Runnable {
            while (true) {
                if (mplayer != null) {
                    val allTime =  mplayer?.stopTime!!.toSeconds()
                    val currentTime = mplayer?.currentTime!!.toSeconds()
                    //val currentTime = musicSlider.value * allTime / 100.0
                    //mplayer?.currentTime = currentTime


                    musicSlider.value = currentTime * 100.0 / allTime
                    mplayer?.volume = volumeSlider.value / 100.0

                    //println("Cur time " + currentTime * 100.0 / allTime)

                    if (musicSlider.value >= musicSlider.max - 0.1) {
                        mplayer?.stop()
                        val nextFileNumber : Int = (openedFilesPath.indexOf(selectedFile!!.toString()) + 1) % openedFilesPath.size
                        val nextFile : String = openedFilesPath[nextFileNumber]
                        filenameLabel.text = " Now playing: " + nextFile.split('/').last().split('.')[0]
                        mplayer = MediaPlayer(Media(nextFile))
                        selectedFile = File(nextFile)
                        mplayer?.play()
                    }
                }
                try {
                    Thread.sleep(40)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }).start()


        val scene = Scene(root, 303.0, 505.0)

        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MPlayer::class.java)
        }
    }
}

