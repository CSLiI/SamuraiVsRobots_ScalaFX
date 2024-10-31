package characters

import scalafx.animation.{KeyFrame, Timeline}
import scalafx.scene.Scene
import scalafx.util.Duration

import scala.util.Random

class NormalEnemy(scene: Scene, platform: Platform, xFactor: Double, yFactor: Double, widthFactor: Double, heightFactor: Double, imagePath: String, _health: Int, _attack: Int)
  extends Character(scene, imagePath, 0, 0, widthFactor, heightFactor, _health, _attack) {

  private val random = new Random()
  private var timeline: Timeline = _
  private var dead = false
  private var movingRight = random.nextBoolean() // Random initial direction
  private var moveSpeed = random.nextDouble() * 2 + 0.5 // Random speed between 0.5 and 2.5 pixels per frame
  private val edgeBuffer = 5.0 // Buffer to prevent immediate direction change at edges

  // Add listeners to dynamically update the position and size based on the scene and platform
  List(scene.width, scene.height, platform.layoutX, platform.layoutY, platform.width, platform.height).foreach(_.onChange {
    updateLayout()
  })

  private def updateLayout(): Unit = {
    val enemyWidth = scene.width.value * widthFactor
    val enemyHeight = scene.height.value * heightFactor

    getImageView.fitWidth.unbind()
    getImageView.fitHeight.unbind()

    getImageView.fitWidth = enemyWidth
    getImageView.fitHeight = enemyHeight

    layoutX = platform.layoutX.value + (platform.width.value * xFactor) - (enemyWidth / 2)
    layoutY = platform.layoutY.value - enemyHeight
  }

  override def die(): Unit = {
    if (!dead) {
      stopAllAnimations()
      dead = true
      super.die()
    }
  }

  def stopAllAnimations(): Unit = {
    if (timeline != null) timeline.stop()
    stopShootingIfExists()
  }

  private def stopShootingIfExists(): Unit = {
    this match {
      case shooter: Laser => shooter.stopShooting()
      case beamShooter: BeamShooter => beamShooter.stopBeamTeleporting()
      case _ => // No shooting or beam shooting behavior to stop
    }
  }

  def move(): Unit = {
    if (!dead) {
      if (timeline != null) {
        timeline.stop()
      }

      timeline = new Timeline {
        cycleCount = Timeline.Indefinite
        autoReverse = false
        keyFrames = Seq(
          KeyFrame(Duration(33), onFinished = _ => moveAcrossPlatform()) // ~30 fps
        )
      }

      timeline.play()
    }
  }

  private def moveAcrossPlatform(): Unit = {
    val platformWidth = platform.width.value
    val enemyWidth = getImageView.fitWidth.value
    val currentX = layoutX.value - platform.layoutX.value // Relative to platform

    if (movingRight) {
      layoutX = platform.layoutX.value + currentX + moveSpeed
      if (layoutX.value >= platform.layoutX.value + platformWidth - enemyWidth - edgeBuffer) {
        changeDirection()
      }
    } else {
      layoutX = platform.layoutX.value + currentX - moveSpeed
      if (layoutX.value <= platform.layoutX.value + edgeBuffer) {
        changeDirection()
      }
    }
  }

  private def changeDirection(): Unit = {
    movingRight = !movingRight
    getImageView.scaleX = if (movingRight) 1 else -1
    // Randomize speed again on direction change
    moveSpeed = random.nextDouble() * 2 + 0.5 // Update `moveSpeed`
  }

  // Initialize the movement
  move()

  // Initial layout setup
  updateLayout()
}
