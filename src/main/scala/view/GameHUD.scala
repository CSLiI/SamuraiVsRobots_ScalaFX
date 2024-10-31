package view

import scalafx.scene.Scene
import scalafx.scene.control.{Label, ProgressBar}
import scalafx.scene.layout.Pane
import characters.{Boss, Player}
import scalafx.Includes._
object GameHUD {
  var hudPane: Pane = _

  private var playerHealthBar: ProgressBar = _
  private var bossHealthBar: ProgressBar = _
  private var timerLabel: Label = _
  private var gameStartTime: Long = _

  // Add private variables to hold player and boss instances
  private var player: Player = _
  private var boss: Boss = _

  def setup(scene: Scene, player: Player, boss: Boss): Unit = {
    this.player = player
    this.boss = boss

    hudPane = new Pane()
    //https://github.com/a8m/pb-scala/tree/master/src
    // Initialize health bars with default sizes (we will adjust them in listeners)
    playerHealthBar = new ProgressBar {
      style = "-fx-accent: green;"
    }
    hudPane.children.add(playerHealthBar)

    bossHealthBar = new ProgressBar {
      style = "-fx-accent: red;"
      visible = false
    }
    hudPane.children.add(bossHealthBar)

    timerLabel = new Label {
      textFill = scalafx.scene.paint.Color.White
    }
    hudPane.children.add(timerLabel)

    // Add listeners to adjust the sizes and positions dynamically
    scene.widthProperty.onChange { (_, _, newWidth) =>
      adjustLayout(scene)
    }
    scene.heightProperty.onChange { (_, _, newHeight) =>
      adjustLayout(scene)
    }

    gameStartTime = System.nanoTime()
    val timer = scalafx.animation.AnimationTimer { _ =>
      val elapsedTime = (System.nanoTime() - gameStartTime) / 1e9.toLong
      timerLabel.text = f"Time: $elapsedTime%d seconds"
    }
    timer.start()

    adjustLayout(scene)  // Initial adjustment based on the current scene size
  }

  private def adjustLayout(scene: Scene): Unit = {
    // Adjust the width of the health bars
    playerHealthBar.prefWidth = scene.width.value * 0.2
    bossHealthBar.prefWidth = scene.width.value * 0.25

    // Adjust the positions
    playerHealthBar.layoutX = 10
    playerHealthBar.layoutY = 10

    bossHealthBar.layoutX = scene.width.value - bossHealthBar.prefWidth.value - 10
    bossHealthBar.layoutY = 10

    // Adjust the font size of the timer label based on the scene width
    timerLabel.font = new scalafx.scene.text.Font("Arial", scene.width.value * 0.02)

    // Position the timer label dynamically at the bottom left
    timerLabel.layoutX = 10
    timerLabel.layoutY = scene.height.value - 80
  }

  def update(): Unit = {
    // Use the player and boss instances instead of static references
    playerHealthBar.progress = math.max(0, player.health.toDouble / player.getMaxHealth)
    bossHealthBar.progress = math.max(0, boss.health.toDouble / boss.getMaxHealth)

    if (!bossHealthBar.visible.value && boss.isVisible) {
      bossHealthBar.visible = true
    }
  }
}