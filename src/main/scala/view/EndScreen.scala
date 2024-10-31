package view

import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text
import main.MyApp

object EndScreen {
  private var completionTimeText: Text = _
  private var exitLabel: Label = _
  var endScreenLayout: VBox = _

  def setup(scene: Scene, gameTime: Long): Unit = {
    val gameTimeSeconds = gameTime / 1e9
    completionTimeText = new Text(s"Game Completed in: ${gameTimeSeconds.formatted("%.2f")} seconds") {
      fill = scalafx.scene.paint.Color.White
      font = scalafx.scene.text.Font("Arial", 24)
    }

    exitLabel = new Label("Exit to Main Menu") {
      onMouseClicked = _ => MyApp.switchToMainMenu()  // Interactive, but no box around it
      style = "-fx-font-size: 16px; -fx-text-fill: white;"  // Only text styling, no background
    }

    endScreenLayout = new VBox(20) {
      alignment = scalafx.geometry.Pos.Center
      children = Seq(completionTimeText, exitLabel)
      style = "-fx-background-color: rgba(0, 0, 0, 0.8);"  // Background for the whole screen
    }

    val overlayPane = new scalafx.scene.layout.StackPane {
      alignment = scalafx.geometry.Pos.Center
      children = endScreenLayout
    }

    overlayPane.prefWidth <== scene.width
    overlayPane.prefHeight <== scene.height

    // Set the scene root to display the end screen
    scene.root = overlayPane
  }
}
