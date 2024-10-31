package view

import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane
import characters._
import util.SoundEffect
import main.MyApp
import scalafx.animation.AnimationTimer

object Game {
  private var projectiles = scala.collection.mutable.ArrayBuffer[ImageView]()
  private var gamePane: Pane = _
  private var gameScene: Scene = _
  private var backgroundImageView: ImageView = _
  private var bossSpawnTimer: scalafx.animation.AnimationTimer = _
  private var gameLoop: scalafx.animation.AnimationTimer = _
  private var gameStartTime: Long = _

  def setup(): Scene = {
    gamePane = new Pane()
    gameScene = new Scene(gamePane, 1280, 700)
    SoundEffect.playEnterGameSound()

    gameStartTime = System.nanoTime()  // Store the start time of the game

    // Initialize background image
    val backgroundImage = new Image(getClass.getResource("/Images/lobby.jpg").toExternalForm)
    backgroundImageView = new ImageView(backgroundImage) {
      //https://stackoverflow.com/questions/17440878/binding-a-function-with-conditional-logic-in-scala
      fitWidth <== gameScene.width
      fitHeight <== gameScene.height
    }
    gamePane.children.add(backgroundImageView)  // Add background image first

    // Initialize platforms
    val platforms = List(
      new Platform(gameScene, 50.0 / 1280.0, 300.0 / 700.0, 300.0 / 1280.0, 50.0 / 700.0, "/Images/Platform.jpg"),
      new Platform(gameScene, 300.0 / 1280.0, 450.0 / 700.0, 300.0 / 1280.0, 50.0 / 700.0, "/Images/Platform.jpg"),
      new Platform(gameScene, 500.0 / 1280.0, 330.0 / 700.0, 300.0 / 1280.0, 50.0 / 700.0, "/Images/Platform.jpg"),
      new Platform(gameScene, 800.0 / 1280.0, 200.0 / 700.0, 300.0 / 1280.0, 50.0 / 700.0, "/Images/Platform.jpg"),
      new Platform(gameScene, 900.0 / 1280.0, 500.0 / 700.0, 300.0 / 1280.0, 50.0 / 700.0, "/Images/Platform.jpg")
    )

    // Add platforms after the background
    platforms.foreach(platform => gamePane.children.add(platform))

    // Initialize enemies
    val enemies = List(
      new NormalEnemy(gameScene, platforms(0), 0.25, 2.0, 200.0 / 1280.0, 200.0 / 700.0, "/Images/NormalEnemy.gif", 100, 10) with Laser,
      new NormalEnemy(gameScene, platforms(1), 0.25, 2.0, 200.0 / 1280.0, 200.0 / 700.0, "/Images/NormalEnemy.gif", 100, 10) with BeamShooter,
      new NormalEnemy(gameScene, platforms(2), 0.25, 2.0, 200.0 / 1280.0, 200.0 / 700.0, "/Images/NormalEnemy.gif", 100, 10) with Laser,
      new NormalEnemy(gameScene, platforms(3), 0.25, 2.0, 200.0 / 1280.0, 200.0 / 700.0, "/Images/NormalEnemy.gif", 100, 10) with BeamShooter,
      new NormalEnemy(gameScene, platforms(4), 0.25, 2.0, 200.0 / 1280.0, 200.0 / 700.0, "/Images/NormalEnemy.gif", 100, 10) with Laser
    )
    enemies.foreach(enemy => gamePane.children.add(enemy))

    // Initialize boss
    val boss = new Boss(gameScene, gamePane, platforms, 0.15, 0.25, "/Images/Boss.gif", 1000, 5)
    boss.hide()
    gamePane.children.add(boss)

    // Initialize player
    val player = new Player(gameScene, platforms, enemies, boss, 100.0, 500.0, 0.05, 0.15, "/Sprites/w1.png", 2000, 200)
    gamePane.children.add(player)

    // Setup HUD
    GameHUD.setup(gameScene, player, boss)
    gamePane.children.add(GameHUD.hudPane)

    // Start game logic
    startGameLoop(player, enemies, boss)

    gameScene
  }

  private def startGameLoop(player: Player, enemies: List[NormalEnemy], boss: Boss): Unit = {
    // Start shooting and beam teleporting for enemies with those traits
    enemies.foreach {
      case shooter: Laser => shooter.startShooting(player)
      case beamShooter: BeamShooter => beamShooter.startBeamTeleporting(player)
      case _ => // Do nothing for non-shooting enemies
    }
    // https://stackoverflow.com/questions/71471546/how-to-make-a-smooth-time-based-animation-with-javafx-animationtimer
    bossSpawnTimer = AnimationTimer { _ =>
      if (enemies.forall(_.isDead) && !boss.isVisible) {
        boss.spawnOnPlatform()
        boss.startBossBehavior(player)
      }

      if (boss.isDead) {
        bossSpawnTimer.stop()
        gamePane.children.remove(boss)
        showEndScreen()
      }
    }
    bossSpawnTimer.start()

    gameLoop = AnimationTimer { _ =>
      GameHUD.update()
    }
    gameLoop.start()
  }
  // Refactored showEndScreen method to recreate the main menu scene instead of reusing the existing root node
  private def showEndScreen(): Unit = {
    val gameEndTime = System.nanoTime()
    val totalTime = gameEndTime - gameStartTime  // Calculate total game time
    bossSpawnTimer.stop()
    gameLoop.stop()
    EndScreen.setup(gameScene, totalTime)  // Recreate the main menu scene
  }
}