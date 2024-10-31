package main

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import view.{Game, MainMenu}
import util.SoundEffect

object MyApp extends JFXApp {
  // Initialize the main menu and set it as the starting scene
  stage = new PrimaryStage {
    title = "Samurai Vs Robots"
    scene = MainMenu.setup()
    resizable = true
    onCloseRequest = _ => {
      sys.exit(0)
    }
  }
  SoundEffect.playThemeSong()

  // Switch to the game scene by delegating to the Game object
  def startGame(): Unit = {
    stage.scene = Game.setup()
  }

  // Switch back to the main menu
  def switchToMainMenu(): Unit = {
    stage.scene = MainMenu.setup()
  }
}
