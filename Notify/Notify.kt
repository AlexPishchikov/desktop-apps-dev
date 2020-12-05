package sample

import javafx.animation.FadeTransition
import javafx.animation.TranslateTransition
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.ImagePattern
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import java.io.File
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.ln
import kotlin.math.sign
import kotlin.random.Random

class Controller {
    @FXML
    lateinit var image_box : HBox
    lateinit var buttons_box : HBox
    lateinit var input_box : HBox
    lateinit var text_box : HBox
}

var primaryConfig = Config()

class Config {
    var pos = Notify.Position.RIGHT_TOP

    var title = "TITLE"
    var msg = "MESSAGE"
    var appName = "app_name"

    var iconBorder = Notify.Border.CIRCLE
    var iconPath = "https://softboxmarket.com/images/thumbnails/618/540/detailed/3/official-bts-wings-2nd-album-cd-poster-po_00.jpg"

    var textColor = "#FFFFFF"

    var bgColor = "#000000"
    var bgOpacity = 0.6
    var waitTime = 5000

    var signalPath : String = "Harp.wav"

    var mode : Notify.Modes = Notify.Modes.DEFAULT

    var pulse = false

    var okButton : Button? = null
    var cancelButton : Button? = null
    var inputField : TextArea? = null
    var dropdown : ComboBox<String>? = null

    var isPressed : Boolean = false
    var dropdownOptions : Array<String> = arrayOf()

    var onClickOk : () -> Unit = {
        if (!isPressed) {
            // println("you pressed ok") // OK_CANCEL
            //println(inputField!!.text) // INPUT
            //println(dropdown!!.value) // DROPDOWN
        }
        isPressed = true
    }

    var onClickCancel : () -> Unit = {
        if (!isPressed) {
            println("you pressed cancel")
        }
        isPressed = true
    }

}

class Notify {
    var controller = Controller()
    var content = HBox()
    var pane = BorderPane()

    enum class Modes {
        DEFAULT,
        OK_CANCEL,
        INPUT,
        DROPDOWN
    }

    enum class Position {
        RIGHT_BOTTOM,
        RIGHT_TOP,
        LEFT_BOTTOM,
        LEFT_TOP
    }

    enum class Border {
        SQUARE,
        CIRCLE
    }

    var defWidth = 600.0
    var defHeight = 300.0

    var config = primaryConfig

    var popup = Stage()

    lateinit var pulseWindow : Stage

    fun start(primaryStage: Stage?) {
        //popup.scene = Scene(load<Parent>(Notify.javaClass.getResource("Notify.fxml")))
        buildDefault()

        var screenRect = Screen.getPrimary().bounds

        var shift = 80.0

        var panelHeight = 40.0

        when (config.pos) {
            Position.LEFT_BOTTOM -> {
                popup.x = shift
                popup.y = screenRect.height - defHeight - shift
            }
            Position.LEFT_TOP -> {
                popup.x = shift
                popup.y = shift
            }
            Position.RIGHT_BOTTOM -> {
                popup.x = screenRect.width - defWidth - shift
                popup.y = screenRect.height - defHeight - shift - panelHeight
            }
            Position.RIGHT_TOP -> {
                popup.x = screenRect.width - defWidth - shift
                popup.y = shift
            }
        }

        val task = object: Task<Void>() {
            @Throws(InterruptedException::class)
            override fun call(): Void? {
                try {
                    val sound = Media(File(config.signalPath).toURI().toString())
                    val mediaPlayer = MediaPlayer(sound)
                    mediaPlayer.play()
                }
                catch (e: Exception) {
                    println(e)
                }
                if (primaryConfig.mode == Modes.DEFAULT) {
                    Thread.sleep(config.waitTime.toLong())
                    closeAnim()
                }
                return null
            }
        }
        Thread(task).start()

        popup.addEventFilter(MouseEvent.MOUSE_PRESSED) {
            if (config.mode == Modes.DEFAULT || config.isPressed) closeAnim()
        }
        popup.scene = Scene(content)

        popup.initOwner(primaryStage)
        popup.initStyle(StageStyle.UNDECORATED)

        popup.opacity = if (config.pulse) {1.0} else config.bgOpacity
        popup.isAlwaysOnTop = true
        openAnim()
    }

    fun openAnim() {
        val ft = FadeTransition(Duration.millis(1500.0), content)
        ft.fromValue = 0.0
        ft.toValue = if (config.pulse) {1.0} else config.bgOpacity
        ft.cycleCount = 1

        val tt = TranslateTransition(Duration.millis(1000.0), content)
        tt.byX = if (config.pos == Position.RIGHT_BOTTOM || config.pos == Position.RIGHT_TOP) -600.0 else 600.0
        tt.fromX = if (config.pos == Position.RIGHT_BOTTOM || config.pos == Position.RIGHT_TOP) 600.0 else -600.0
        tt.play()
        ft.play()

        fun inc(i :  Int) : Int {
            var k = i
            if (k >= 15)
                k = -1
            if (k <= -15)
                k = 1
            if (k > 0) k++
            if (k < 0) k--
            return k
        }

        if (config.pulse) {
            pulseWindow = Stage()
            var emptyHBox = HBox()
            emptyHBox.setPrefSize(defWidth + 20, defHeight + 20)
            pulseWindow.scene = Scene(emptyHBox)
            pulseWindow.initOwner(popup)
            pulseWindow.initStyle(StageStyle.UNDECORATED)

            pulseWindow.x = popup.x
            pulseWindow.y = popup.y
            emptyHBox.style = "-fx-background-color:" + config.bgColor
            popup.isAlwaysOnTop = false
            pulseWindow.show()
            popup.show()

            val animTimer = Timer()
            animTimer.scheduleAtFixedRate(object : TimerTask() {
                var i = -2
                override fun run() {
                    if (i.absoluteValue < 15) {
                        pulseWindow.x += (i.toDouble() / (ln(i.absoluteValue.toDouble()) * ln(i.absoluteValue.toDouble()))).toInt()
                        pulseWindow.y += (i.toDouble() / (ln(i.absoluteValue.toDouble()) * ln(i.absoluteValue.toDouble()))).toInt()
                        pulseWindow.width -= (i.toDouble() / (ln(i.absoluteValue.toDouble()) * ln(i.absoluteValue.toDouble()))).toInt()
                        pulseWindow.height -= (i.toDouble() / (ln(i.absoluteValue.toDouble()) * ln(i.absoluteValue.toDouble()))).toInt()
                        pulseWindow.width -= (i.toDouble() / (ln(i.absoluteValue.toDouble()) * ln(i.absoluteValue.toDouble()))).toInt()
                        pulseWindow.height -= (i.toDouble() / (ln(i.absoluteValue.toDouble()) * ln(i.absoluteValue.toDouble()))).toInt()
                        Thread.sleep((1 / i.absoluteValue * 35).toLong())
                    }
                    i = inc(i)
                }
            }, 100, 30)
        }
        else
            popup.show()
    }

    fun closeAnim() {
        val ft = FadeTransition(Duration.millis(1000.0), content)
        ft.fromValue = config.bgOpacity
        ft.toValue = 0.0
        ft.cycleCount = 1
        val tt = TranslateTransition(Duration.millis(1000.0), content)
        tt.byX = if (config.pos == Position.RIGHT_BOTTOM || config.pos == Position.RIGHT_TOP) 600.0 else -600.0
        tt.fromX = 0.0
        ft.setOnFinished {
            println("close")
            if(config.pulse)
                pulseWindow.close()
            popup.close()
        }
        tt.play()
        ft.play()
    }

    fun buildDefault() {
        content.setPrefSize(defWidth, defHeight)
        content.setPadding(Insets(5.0, 5.0, 5.0, 5.0))
        content.spacing = 10.0
        content.style = "-fx-background-color:" + config.bgColor

        var path = config.iconPath
        if (!config.iconPath.isEmpty()) {
            if (config.iconPath.substring(0, 4) != "http") {
                path = File(config.iconPath).toURI().toURL().toString();
            }

            var icoBorder = if (config.iconBorder == Border.CIRCLE) {
                Circle(defHeight / 2 , defHeight /2 , defHeight / 2)
            } else {
                Rectangle(defHeight / 2 , defHeight /2, defHeight, defHeight)
            }
            icoBorder.setFill(ImagePattern(Image(path)))
            content.children.add(icoBorder)
        }


        var msgLayout = BorderPane()

        var title = Label(config.title + "\n")
        title.font = Font(24.0)
        title.style = "-fx-font-weight: bold; -fx-text-fill:" + config.textColor

        var message = Label("\n" + config.msg)
        message.font = Font(20.0)
        message.style = "-fx-text-fill:" + config.textColor
        message.maxHeight = 200.0
        message.maxWidth = 400.0
        message.isWrapText = true

        var app = Label(config.appName + "\n" + "\n")
        app.font = Font(16.0)
        app.style = "-fx-text-fill:" + config.textColor
        app.isWrapText = true

        val a = when (config.mode) {
            Modes.OK_CANCEL -> buildButtons()
            Modes.INPUT -> buildInput()
            Modes.DROPDOWN -> buildDropdown()
            else -> null
        }

        msgLayout.top = title
        msgLayout.center = message

        var bottomContent = BorderPane()
        bottomContent.top = app

        pane.top = msgLayout
        if (a != null) {
            bottomContent.bottom = a
            pane.bottom = bottomContent
        }

        content.children.add(pane)
    }

    fun buildButtons() : HBox {
        var hbox : HBox = HBox()

        if (config.okButton == null || config.cancelButton == null) {
            config.okButton = Button()
            config.cancelButton = Button()
        }

        config.okButton!!.text = if (config.okButton!!.text == "") { "Ok" } else { config.okButton!!.text }
        config.okButton!!.setOnAction { config.onClickOk() }

        config.cancelButton!!.text = if (config.cancelButton!!.text == "") { "Cancel" } else { config.cancelButton!!.text }
        config.cancelButton!!.setOnAction { config.onClickCancel() }

        hbox.style = "-fx-alignment: bottom-center;"
        hbox.spacing = 10.0
        hbox.children.addAll(config.okButton, config.cancelButton)

        return hbox
    }

    fun buildInput() : VBox {
        var vbox : VBox = VBox()
        vbox.spacing = 10.0
        if (config.inputField == null)
            config.inputField = TextArea()
        vbox.style = "-fx-alignment: bottom-center;"
        config.inputField!!.maxHeight = 10.0

        vbox.children.addAll(config.inputField, buildButtons())
        return vbox
    }

    fun buildDropdown() : VBox {
        var vbox : VBox = VBox()
        vbox.spacing = 10.0

        if (config.dropdown == null)
            config.dropdown = ComboBox()

        config.dropdown!!.items.addAll(config.dropdownOptions)

        vbox.style = "-fx-alignment: bottom-center;"

        vbox.children.addAll(config.dropdown, buildButtons())
        return vbox
    }

    companion object {
        @JvmStatic
        fun main(primaryStage: Stage?, someConfig : Config = Config()) {
            primaryConfig = someConfig
            var a = Notify()
            a.start(primaryStage)
        }
    }
}
