package view

import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.Label
import scalafx.scene.layout.{Pane, StackPane, VBox}
import scalafx.geometry.{Insets, Pos}
import scalafx.Includes._
import main.MyApp

object MainMenu {
  private var backgroundImageView: ImageView = _
  private var startLabel: Label = _
  private var exitLabel: Label = _
  var root: StackPane = _

  def setup(): Scene = {
    backgroundImageView = new ImageView(new Image(getClass.getResource("/Images/MainBackground.png").toExternalForm)) {
      preserveRatio = false
    }
    //https://stackoverflow.com/questions/24127557/scala-how-to-create-a-label-button-that-shows-more-options-when-clicked
    startLabel = new Label("Start") {
      onMouseClicked = handle { MyApp.startGame() }  // Correctly call the startGame method
      style = "-fx-font-size: 24px; -fx-text-fill: white;"  // Simplified style
    }

    exitLabel = new Label("Exit") {
      onMouseClicked = handle { sys.exit(0) }  // Exit the application
      style = "-fx-font-size: 24px; -fx-text-fill: white;"  // Simplified style
    }

    val labelLayout = new VBox(20) {
      alignment = Pos.BottomCenter  // Align the labels at the bottom
      padding = Insets(50)
      children = Seq(startLabel, exitLabel)
    }

    root = new StackPane {
      children = Seq(backgroundImageView, labelLayout)
    }
    StackPane.setAlignment(labelLayout, Pos.BottomCenter)  // Position VBox at the bottom center

    backgroundImageView.fitWidth <== root.width
    backgroundImageView.fitHeight <== root.height

    new Scene(root, 1280, 700)
  }
}
