package view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.text.Text
import main.MyApp

object DeathScreen {
  private var deathMessage: Text = _
  private var exitLabel: Label = _
  private var pane: VBox = _

  def setup(scene: Scene): Unit = {
    deathMessage = new Text("You have died") {
      style = "-fx-font-size: 36px; -fx-font-weight: bold;"
    }

    exitLabel = new Label("Exit to Main Menu") {
      onMouseClicked = _ => MyApp.switchToMainMenu()  // Still interactive on click
      style = "-fx-font-size: 16px; -fx-text-fill: white;"  // Removed background and padding
    }

    pane = new VBox {
      alignment = Pos.Center
      spacing = 20
      padding = Insets(20)
      children = Seq(deathMessage, exitLabel)
      style = "-fx-background-color: rgba(0, 0, 0, 0.8);"  // Background for the whole pane, not individual labels
    }

    pane.prefWidth <== scene.width
    pane.prefHeight <== scene.height
  }

  def getPane: VBox = pane
}
