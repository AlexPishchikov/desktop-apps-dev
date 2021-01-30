package sample

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.web.WebView
import javafx.stage.Stage
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import java.io.IOException
import java.net.URISyntaxException

class mainController {
    @FXML
    var searchField: TextField? = null

    @FXML
    var searchButton: Button? = null

    @FXML
    var filmList: ListView<String?>? = null

    @FXML
    fun initialize() {
        searchButton!!.setOnAction {
            var jo: JSONObject = Parser.ParseByKeyWord(searchField!!.text)
            for (film in (jo["films"] as JSONArray?)!!) {
                filmList!!.items.addAll((film as JSONObject)["nameRu"] as String?)
            }
        }
        val filmSelectionModel = filmList!!.selectionModel
        filmSelectionModel?.selectedItemProperty()?.addListener(object : ChangeListener<String?> {
            override fun changed(changed: ObservableValue<out String?>, oldValue: String?, newValue: String?) {
                val root: Parent
                Controller.prevFilm = null
                Controller.keyWord = newValue
                val loader = FXMLLoader(javaClass.getResource("film.fxml"))
                root = loader.load()
                val stage = Stage()
                stage.title = "Kinopoisk"
                stage.scene = Scene(root, 1600.0, 1000.0)
                stage.isResizable = false
                stage.show()
                searchButton!!.scene.window.hide()
            }
        })
    }
}
