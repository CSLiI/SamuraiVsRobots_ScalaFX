package characters

import scalafx.Includes._
import scalafx.animation.{AnimationTimer, PauseTransition}
import scalafx.geometry.Bounds
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration

trait BeamShooter { self: NormalEnemy =>

  private val beamThickness = 10.0
  private val beamColor = Color.Blue
  private val beamDuration = Duration(1000) // 1 second
  private val shootInterval = Duration(3000) // 3 seconds
  private var lastShootTime = 0L
  private var shootingLeft = true
  private var beamTimer: Option[AnimationTimer] = None
  private var activeBeam: Option[Rectangle] = None
  private val beamPane = new Pane()

  private def ensureBeamPaneAdded(): Unit = {
    if (self.scene() != null && !self.scene().content.contains(beamPane)) {
      self.scene().content.add(beamPane)
      println("Beam pane added to the scene.")
    }
  }

  private def teleportBeam(): Unit = {
    ensureBeamPaneAdded()

    if (self.scene() == null) {
      println("Error: Scene is not initialized yet.")
      return
    }

    // Remove any existing beam
    activeBeam.foreach(beam => beamPane.children.remove(beam))

    val beam = new Rectangle {
      height = beamThickness
      fill = beamColor
    }

    beam.layoutY = self.getCenterY - beamThickness / 2
    if (shootingLeft) {
      beam.layoutX = 0
      beam.width = self.getCenterX
    } else {
      beam.layoutX = self.getCenterX
      beam.width = self.scene().width.value - self.getCenterX
    }

    beamPane.children.add(beam)
    activeBeam = Some(beam)
    println(s"Beam teleported to (${beam.layoutX.value}, ${beam.layoutY.value}) with width ${beam.width.value}")
    println(s"characters.Shooting ${if (shootingLeft) "left" else "right"}")

    // Schedule removal of the beam using PauseTransition
    new PauseTransition(beamDuration) {
      onFinished = _ => {
        beamPane.children.remove(beam)
        activeBeam = None
      }
    }.play()

    // Toggle shooting direction for next shot
    shootingLeft = !shootingLeft
  }

  private def checkBeamCollision(player: Player): Unit = {
    activeBeam.foreach { beam =>
      val beamBounds: Bounds = beam.getBoundsInParent
      if (beamBounds.intersects(player.getHitboxBounds)) {
        player.takeDamage(self.attackPower)
        println("characters.Player hit by beam!")
      }
    }
  }
  //https://claude.ai/new
  def startBeamTeleporting(player: Player): Unit = {
    beamTimer = Some(AnimationTimer { now =>
      if (now - lastShootTime > shootInterval.toMillis * 1000000) {
        teleportBeam()
        lastShootTime = now
      }
      checkBeamCollision(player)
    })
    beamTimer.foreach(_.start())
  }

  def stopBeamTeleporting(): Unit = {
    beamTimer.foreach(_.stop())
    beamTimer = None
    activeBeam.foreach(beam => beamPane.children.remove(beam))
    activeBeam = None
  }
}