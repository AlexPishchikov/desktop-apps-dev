package sample

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import nu.pattern.OpenCV
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc.*

class DragAndDropMainWindow : Application() {
    private var root : AnchorPane = AnchorPane()

    private var menubar : MenuBar = MenuBar()

    private var imageMenu : Menu = Menu("Add image")
    private var effectsMenu : Menu = Menu("Add effect")

    private val selectImageMenuItem : MenuItem = MenuItem("Select image")
    private val addEffectMenuItems : Array<MenuItem> = arrayOf(MenuItem("Rotate"), MenuItem("Black-White"), MenuItem("Mirror"))

    private val fileChooser : FileChooser = FileChooser()

    private val resultImagePath : String = "images/result_image.jpg"

    private var effects: MutableList<Label> = mutableListOf()

    private lateinit var mainImage : File
    private lateinit var resultImage : File

    init {
        fileChooser.title = "Select image"
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Images", "*.jpg"))

        imageMenu.items.add(selectImageMenuItem)
        effectsMenu.items.addAll(addEffectMenuItems)

        menubar.menus.add(imageMenu)
        menubar.menus.add(effectsMenu)

        root.children.add(menubar)
    }

    override fun start(primaryStage: Stage) {
        selectImageMenuItem.setOnAction {
            mainImage = fileChooser.showOpenDialog(primaryStage)
            val source : Mat = Imgcodecs.imread(mainImage.absolutePath)
            Imgcodecs.imwrite(resultImagePath, source)
            resultImage = File(resultImagePath)
            root.children.removeAll()

            val inputNode = createImageNode(mainImage.toURI().toString())
            val resultNode = createImageNode(resultImage.toURI().toString())

            if (root.children.count() == 1) {
                root.children.add(inputNode)
                root.children.add(resultNode)
            }

            root.children[1] = inputNode
            root.children[1].layoutX = 350.0
            root.children[1].layoutY = 150.0
            root.children[2] = resultNode
            root.children[2].layoutX = 350.0
            root.children[2].layoutY = 350.0

            for (effect in effects)
                addEffect(effect.text)
        }

        for (effect in addEffectMenuItems) {
            effect.setOnAction {
                val newNode = DraggableNode(text = effect.text)
                newNode.setOnMousePressed {
                    if(it.button == MouseButton.SECONDARY) {
                        effects.removeAt(findEffect(newNode.title_bar!!.text))
                        root.children.remove(newNode)
                        val source: Mat = Imgcodecs.imread(mainImage.absolutePath)
                        Imgcodecs.imwrite(resultImagePath, source)
                        resultImage = File(resultImagePath)
                        for (eff in effects)
                            addEffect(eff.text)

                        val resultNode = createImageNode(resultImage.toURI().toString())
                        val oldCoords : Pair<Double, Double> = root.children[2].layoutX to root.children[2].layoutY

                        root.children[2] = resultNode
                        root.children[2].layoutX = oldCoords.first
                        root.children[2].layoutY = oldCoords.second
                    }
                }
                root.children.add(newNode)
                root.children.last().layoutX = 600.0 - 70 * effects.size
                root.children.last().layoutY = 600.0
                effects.add(Label(effect.text))
                addEffect(effect.text)
            }
        }
        primaryStage.scene = Scene(root, 800.0, 800.0)
        primaryStage.show()
    }

    private fun findEffect(effectName : String) : Int {
        for (i in 0..effects.size) {
            if (effects[i].text == effectName)
                return i
        }
        return -1
    }

    private fun addEffect(effectName: String) {
        if(effectName == "Rotate") {
            val source : Mat = Imgcodecs.imread(resultImage.absolutePath)
            val destination = Mat(source.rows(), source.cols(), source.type())
            val center = Point(destination.cols() / 2.0, destination.rows() / 2.0)
            val rotMat : Mat = getRotationMatrix2D(center, 180.0, 1.0)
            warpAffine(source, destination, rotMat, destination.size())
            Imgcodecs.imwrite(resultImagePath, destination)
        }

        if(effectName == "Black-White") {
            val source : Mat = Imgcodecs.imread(resultImage.absolutePath)
            val bwImage = Mat()
            cvtColor(source, bwImage, COLOR_RGB2GRAY)
            Imgcodecs.imwrite(resultImagePath, bwImage)
        }

        if(effectName == "Mirror") {
            val source : Mat = Imgcodecs.imread(resultImage.absolutePath)
            Core.flip(source, source, 1)
            Imgcodecs.imwrite(resultImagePath, source)
        }

        val oldCoords : Pair<Double, Double> = root.children[2].layoutX to root.children[2].layoutY

        root.children[2] = createImageNode(resultImage.toURI().toString())
        root.children[2].layoutX = oldCoords.first
        root.children[2].layoutY = oldCoords.second
    }

    private fun createImageNode(path : String) : DraggableNode {
        val resultImage = ImageView(path)
        resultImage.fitHeight = 100.0
        resultImage.fitWidth = 100.0
        resultImage.isPreserveRatio = true
        resultImage.isSmooth = true
        resultImage.isCache = true

        return DraggableNode(content = resultImage)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            OpenCV.loadLocally()
            launch(DragAndDropMainWindow::class.java)
        }
    }
}
