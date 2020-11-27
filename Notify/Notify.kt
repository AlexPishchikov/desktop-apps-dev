package sample

import javafx.animation.FadeTransition
import javafx.animation.TranslateTransition
import javafx.application.Application
import javafx.concurrent.Task
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
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
    var bgOpacity = 0.9

    var waitTime = 5000

    var signalPath : String = "Harp.wav"

    var mode : Notify.Modes = Notify.Modes.DEFAULT

    var okButton : Button? = null
    var cancelButton : Button? = null
    var inputField : TextArea? = null
    var dropdown : ComboBox<String>? = null

    var isPressed : Boolean = false
    var dropdownOptions : Array<String> = arrayOf()

    var onClickOk = {
        if (!isPressed) {
            // println("you pressed ok") // OK_CANCEL
            // println(inputField!!.text) // INPUT
            println(dropdown!!.value) // DROPDOWN
        }
        isPressed = true
    }

    var onClickCancel = {
        if (!isPressed) {
            println("you pressed cancel")
        }
        isPressed = true
    }

}

class Notify: Application() {
    var content = HBox()

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

    override fun start(primaryStage: Stage?) {
        buildDefault()

        var screenRect = Screen.getPrimary().bounds

        var shift = 10.0

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
                popup.y = screenRect.height - defHeight - shift
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
                Thread.sleep(config.waitTime.toLong())
                closeAnim()
                return null
            }
        }
        Thread(task).start()

        popup.addEventFilter(MouseEvent.MOUSE_PRESSED) {
            if (config.mode == Modes.DEFAULT || config.isPressed) closeAnim()
        }

        popup.scene = Scene(content)
        popup.initOwner(primaryStage)
        popup.initStyle(StageStyle.TRANSPARENT)
        popup.opacity = config.bgOpacity
        popup.show()

        openAnim()
    }

    fun openAnim() {
        val ft = FadeTransition(Duration.millis(1500.0), content)
        ft.fromValue = 0.0
        ft.toValue = config.bgOpacity
        ft.cycleCount = 1
        val tt = TranslateTransition(Duration.millis(1400.0), content)
        tt.byX = -600.0
        tt.fromX = 600.0
        tt.play()
        ft.play()
    }

    fun closeAnim() {
        val ft = FadeTransition(Duration.millis(1500.0), content)
        ft.fromValue = config.bgOpacity
        ft.toValue = 0.0
        ft.cycleCount = 1
        val tt = TranslateTransition(Duration.millis(1400.0), content)
        tt.byX = 600.0
        tt.fromX = -0.0
        ft.setOnFinished {
            println("close")
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


        var msgLayout = VBox()

        var title = Label(config.title)
        title.font = Font(24.0)
        title.style = "-fx-font-weight: bold; -fx-text-fill:" + config.textColor

        var message = Label(config.msg)
        message.font = Font(20.0)
        message.style = "-fx-text-fill:" + config.textColor

        var app = Label(config.appName)
        app.font = Font(16.0)
        app.style = "-fx-text-fill:" + config.textColor


        val a = when (config.mode) {
            Modes.OK_CANCEL -> buildButtons()
            Modes.INPUT -> buildInput()
            Modes.DROPDOWN -> buildDropdown()
            else -> null
        }
        msgLayout.children.addAll(title, message, app)
        if (a != null) {
            msgLayout.children.add(a)
        }
        content.children.add(msgLayout)

    }

    fun buildButtons() : HBox {
        var hbox : HBox = HBox()

        if (config.okButton == null || config.cancelButton == null) {
            config.okButton = Button()
            config.cancelButton = Button()
        }

        config.okButton!!.text = if (config.okButton!!.text == "") {"Ok"} else { config.okButton!!.text }
        config.okButton!!.setOnAction { config.onClickOk() }

        config.cancelButton!!.text = if (config.cancelButton!!.text == "") {"Cancel"} else { config.cancelButton!!.text }
        config.cancelButton!!.setOnAction { config.onClickCancel() }

        hbox.children.addAll(config.okButton, config.cancelButton)
        hbox.spacing = 10.0
        return hbox
    }

    fun buildInput() : HBox {
        var hbox : HBox = buildButtons()
        if (config.inputField == null)
            config.inputField = TextArea()

        hbox.children.add(config.inputField)

        return hbox
    }

    fun buildDropdown() : HBox {
        var hbox : HBox = buildButtons()

        if (config.dropdown == null)
            config.dropdown = ComboBox()

        config.dropdown!!.items.addAll(config.dropdownOptions)

        hbox.children.add(config.dropdown)

        return hbox
    }

    companion object {
        @JvmStatic
        fun main(someConfig : Config = Config()) {
            primaryConfig = someConfig
            launch(Notify::class.java)
        }
    }
}
