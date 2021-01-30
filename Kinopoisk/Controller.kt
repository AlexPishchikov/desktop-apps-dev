package sample

import com.pixelduke.control.ParallaxListView
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.effect.BoxBlur
import javafx.scene.effect.MotionBlur
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.ParseException
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL

class Controller {
    @FXML
    var filmView: ImageView? = null

    @FXML
    var trailer: WebView? = null

    @FXML
    var filmName: Label? = null

    @FXML
    var enName: Label? = null

    @FXML
    var rating: Label? = null

    @FXML
    var countryLabel: Label? = null

    @FXML
    var createAgeLabel: Label? = null

    @FXML
    var taglinetaglineLabel: Label? = null

    @FXML
    var genresLabel: Label? = null

    @FXML
    var durationLabel: Label? = null

    @FXML
    var countVoteLabel: Label? = null

    @FXML
    var description: Label? = null

    @FXML
    var prevButton: Button? = null

    @FXML
    var searchField: TextField? = null

    @FXML
    var searchButton: Button? = null

//    @FXML
//    var factsList: ListView<String>? = null

    @FXML
    var background: ImageView? = null

    @FXML
    @Throws(IOException::class, URISyntaxException::class, ParseException::class)
    fun initialize() {
        val data = (Parser.ParseByKeyWord(keyWord)["films"] as JSONArray)[0] as JSONObject
        val film = Parser.ParseById((data["filmId"] as Long?).toString())
        val trailerUrl = Parser.ParseTrailer((data["filmId"] as Long?).toString())
        filmName!!.text = film?.get("nameRu") as String?
        enName!!.text = film?.get("nameEn") as String?
        createAgeLabel!!.text = film?.get("year") as String?
        taglinetaglineLabel!!.text = film?.get("slogan") as String?
        var countries = ""
        for (country in (film?.get("countries") as JSONArray?)!!) {
            countries += (country as JSONObject)["country"] as String
            countries += ", "
        }
        countries = countries.removeSuffix(", ")
        countryLabel!!.text = countries
        var genres = ""
        for (genre in (film?.get("genres") as JSONArray?)!!) {
            genres += (genre as JSONObject)["genre"] as String
            genres += ", "
        }
        genres = genres.removeSuffix(", ")
        genresLabel!!.text = genres
        genresLabel!!.isWrapText = true
        durationLabel!!.text = film?.get("filmLength") as String?
        rating!!.text = data["rating"] as String?
        val countRate = data["ratingVoteCount"] as Long?
        countVoteLabel!!.text = countRate.toString()
        val connection = URL(data["posterUrl"] as String?).openConnection()
        connection.connect()
        filmView!!.image = Image(connection.getInputStream())
        background!!.image = filmView!!.image
        background!!.opacity = 0.33
        background!!.effect = MotionBlur()
        background!!.x = 200.0
        filmView!!.setOnMousePressed {
            val dialog = Stage()
            dialog.initStyle(StageStyle.UNDECORATED)
            val box = VBox()
            box.alignment = Pos.CENTER
            val image = ImageView(filmView!!.image)
            box.children.add(image)
            val scene = Scene(box)
            scene.setOnMousePressed {
                dialog.close()
            }
            dialog.isAlwaysOnTop = true
            dialog.scene = scene
            dialog.show()
        }
        if (trailerUrl != null) {
            val changedTrailerUri = trailerUrl.substring(32, trailerUrl.length)
            val embancedUri = "https://www.youtube.com/embed/$changedTrailerUri"
            trailer!!.engine.load(embancedUri)
        }
        description!!.isWrapText = true
        description!!.text = film?.get("description") as String?
//        for (fact in (film?.get("facts") as JSONArray?)!!)
//            factsList!!.items.addAll(fact as String)
//        for (fact in factsList!!.childrenUnmodifiable)
//            fact.style = "-fx-background-color: transparent;"
        prevButton!!.onAction = object : EventHandler<ActionEvent> {
            override fun handle(event: ActionEvent) {
                var root: Parent? = null
                if (prevFilm == null)
                    root = FXMLLoader.load<Parent>(javaClass.getResource("main.fxml"))
                else {
                    keyWord = prevFilm
                    prevFilm = null
                    root = FXMLLoader.load<Parent>(javaClass.getResource("film.fxml"))
                }
                val stage = Stage()
                stage.title = "Kinopoisk"
                stage.scene = Scene(root, 1600.0, 1000.0)
                stage.isResizable = false
                stage.show()
                (event.source as Node).scene.window.hide()
            }
        }
        searchButton!!.onAction = object : EventHandler<ActionEvent> {
            override fun handle(event: ActionEvent) {
                val root: Parent
                try {
                    prevFilm = keyWord
                    keyWord = searchField!!.text
                    root = FXMLLoader.load(javaClass.getResource("film.fxml"))
                    val stage = Stage()
                    stage.title = "Kinopoisk"
                    stage.scene = Scene(root, 1600.0, 1000.0)
                    stage.show()
                    (event.source as Node).scene.window.hide()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        @JvmField
        var keyWord: String? = null
        @JvmField
        var prevFilm: String? = null
    }
}