package sample
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import java.io.Serializable
import java.util.*

class Point2dSerial(x: Double, y: Double) : Point2D(x, y), Serializable
var stateAddLink = DataFormat("linkAdd")
var stateAddNode = DataFormat("nodeAdd")

//lateinit var btn1: DraggableNode
//lateinit var btn2: DraggableNode

class DraggableNode(var content : ImageView? = null, var text: String = "") : AnchorPane() {
    @FXML
    var root_pane: AnchorPane? = null

    @FXML
    var left_link_handle: AnchorPane? = null

    @FXML
    var right_link_handle: AnchorPane? = null

    @FXML
    var title_bar: Label? = null

    lateinit var contextDragOver: EventHandler<DragEvent>
    lateinit var contextDragDropped: EventHandler<DragEvent>

    lateinit var linkDragDetected: EventHandler<MouseEvent>
    lateinit var linkDragDropped: EventHandler<DragEvent>
    lateinit var contextLinkDragOver: EventHandler<DragEvent>
    lateinit var contextLinkDagDropped: EventHandler<DragEvent>

    var myLink = NodeLink()
    var offset = Point2D(0.0, 0.0)

    var superParent: AnchorPane? = null

    @FXML
    private fun initialize() {
        nodeHandlers()
        linkHandlers()
        title_bar!!.style = "-fx-background-color: white;"
        if(text != "")
            title_bar!!.text = text
        if(content != null)
            title_bar!!.graphic = content
        left_link_handle!!.onDragDetected = linkDragDetected
        left_link_handle!!.onDragDropped = linkDragDropped
        right_link_handle!!.onDragDetected = linkDragDetected
        right_link_handle!!.onDragDropped = linkDragDropped

        myLink.isVisible = false
        parentProperty().addListener{ o, old, new -> superParent = parent as AnchorPane? }

    }

    fun updatePoint(p: Point2D) {
        var local = parent.sceneToLocal(p)
        relocate(
            (local.x - offset.x),
            (local.y - offset.y)
        )
    }

    fun nodeHandlers() {
        contextDragOver = EventHandler { event ->
            updatePoint(Point2D(event.sceneX, event.sceneY))
            event.consume()
        }

        contextDragDropped = EventHandler { event ->
            parent.onDragDropped = null
            parent.onDragOver = null
            event.isDropCompleted = true
            event.consume()
        }

        title_bar!!.onDragDetected = EventHandler { event->
            parent.onDragOver = contextDragOver
            parent.onDragDropped = contextDragDropped

            offset = Point2D(event.x - 10, event.y - 20)
            updatePoint(Point2D(event.sceneX, event.sceneY))

            val content = ClipboardContent()
            content[stateAddNode] = "node"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
        }
    }

    fun linkHandlers() {

        linkDragDetected = EventHandler { event ->
            parent.onDragOver = null
            parent.onDragDropped = null

            parent.onDragOver = contextLinkDragOver
            parent.onDragDropped = contextLinkDagDropped

            superParent!!.children.add(0, myLink)
            myLink.isVisible = true

            val p = Point2D(layoutX + 100/2, layoutY + 100/2)
            myLink.setStart(p)

            val content = ClipboardContent()
            content[stateAddLink] = "link"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.consume()
        }

        linkDragDropped = EventHandler { event ->
            println("link connect")
            parent.onDragOver = null
            parent.onDragDropped = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)

            //val source = event.source as _root_ide_package_.javafx.scene.layout.AnchorPane

            val link = NodeLink()
//            link.bindStartEnd(btn1, btn2)
            superParent!!.children.add(0, link)

            val content = ClipboardContent()
            content[stateAddLink] = "link"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.isDropCompleted = true
            event.consume()
        }


        contextLinkDragOver = EventHandler { event ->
            event.acceptTransferModes(*TransferMode.ANY)
            if (!myLink.isVisible) myLink.isVisible = true
            myLink.setEnd(Point2D(event.x, event.y))

            event.consume()
        }

        contextLinkDagDropped = EventHandler { event ->
            println("link dropped")
            parent.onDragDropped = null
            parent.onDragOver = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)

            event.isDropCompleted = true
            event.consume()
        }
    }

    init {
        val fxmlLoader = FXMLLoader(
            javaClass.getResource("resources/DraggableNode.fxml")
        )
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
        id = UUID.randomUUID().toString()
    }
}

//
//class DnD2: Application() {
//
//    override fun start(primaryStage: Stage) {
//        btn1 = DraggableNode()
//        btn2 = DraggableNode()
//
//        var root = _root_ide_package_.javafx.scene.layout.AnchorPane()
//        root.children.add(btn1)
//        root.children.add(btn2)
//
//        var scene = Scene(root, 640.0, 480.0)
//        primaryStage.scene = scene
//        primaryStage.show()
//    }
//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            launch(DnD2::class.java)
//        }
//    }
//}