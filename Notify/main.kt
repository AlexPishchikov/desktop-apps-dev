package sample

import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.*


class someWindow : Application() {
    var button = Button()
    var popup = Stage()
    override fun start (primaryStage : Stage?) {
        button.text = "Вызвать уведомление"
        button.setOnAction {
            Notify.main(primaryStage, initConfig())
        }

        popup.scene = Scene(button, 300.0, 100.0)
        popup.initStyle(StageStyle.TRANSPARENT)
        popup.initOwner(primaryStage)
        popup.show()
//        val animTimer = Timer()
//        animTimer.scheduleAtFixedRate(object : TimerTask() {
//            var i = 0
//            override fun run() {
//                if (i < 1000) {
//                    popup.x += 10
//                    popup.x %= 1820
//                } else {
//                    this.cancel()
//                }
//                i++
//            }
//        }, 2000, 25)
    }

    fun initConfig() : Config {
        var someConfig = Config()
        someConfig.title = "Lorem ipsum dolor"
        someConfig.msg = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi"
        someConfig.appName = "Duis aute irure."
        someConfig.textColor = "#000000"
        someConfig.bgColor = "#FFFFFF"
        someConfig.pos = Notify.Position.RIGHT_BOTTOM
        someConfig.mode = Notify.Modes.DROPDOWN

        someConfig.waitTime = 3000
        someConfig.pulse = false

        someConfig.onClickOk = {
            println("asadasdass")
        }

        someConfig.bgOpacity = 0.9

        someConfig.dropdownOptions = arrayOf("111asd", "222asd")
        return someConfig
    }

}

fun main() {
    launch(someWindow::class.java)
}