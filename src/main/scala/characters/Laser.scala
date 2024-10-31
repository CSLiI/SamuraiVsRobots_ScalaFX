package characters

import scalafx.Includes.jfxScene2sfx
import scalafx.animation.AnimationTimer
import scalafx.scene.Group
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane

trait Laser {
  self: NormalEnemy =>
  private val bulletSpeed = 2.0
  private val shootingInterval = 2000
  private var lastShootTime = 0L


  private var bullets = List[(ImageView, Double, Double)]() // List to keep track of all bullets
  private var shootingTimer: Option[AnimationTimer] = None
  private val bulletContainer = new Group()

  private def ensureBulletContainerInScene(): Unit = {
    if (self.scene() != null && !self.scene().content.contains(bulletContainer)) {
      self.scene().content.add(bulletContainer)
    }
  }


  // Method to create a bullet with a fixed direction
  private def createBullet(player: Player): Option[(ImageView, Double, Double)] = {
    if (self.scene() == null || player == null) return None
    val angle = math.atan2(player.getCenterY - self.getCenterY, player.getCenterX - self.getCenterX)
    val dx = math.cos(angle) * bulletSpeed
    val dy = math.sin(angle) * bulletSpeed

    val bulletImage = new ImageView(new Image(getClass.getResource("/Images/laser.png").toExternalForm)) {
      fitWidth = 40
      fitHeight = 40
      preserveRatio = true
      layoutX = self.getCenterX
      layoutY = self.getCenterY
      rotate = math.toDegrees(angle)
    }
    Some((bulletImage, dx, dy))
  }

  // Helper method to remove a bullet from the scene
  private def removeBulletFromScene(bullet: ImageView): Unit = {
    self.scene().root.value match {
      case pane: Pane =>
        pane.children.remove(bullet) // Remove the bullet from the scene
      case group: Group =>
        group.children.remove(bullet) // Remove the bullet from the scene
      case _ =>
        println("Scene root does not support removing children, cannot remove bullet")
    }
  }


  // Start shooting method
  def startShooting(player: Player): Unit = {
    shootingTimer = Some(AnimationTimer { now =>
      ensureBulletContainerInScene() // Ensure the bullet container is in the scene

      if (self.scene() != null && player != null && !player.isDead) {
        if (now - lastShootTime > shootingInterval * 1000000) {
          createBullet(player).foreach { case (newBullet, dx, dy) =>
            bulletContainer.children.add(newBullet) // Add the bullet to the bullet container
            bullets ::= (newBullet, dx, dy) // Add the bullet to the list
          }
          lastShootTime = now
        }

        // Update the position of all bullets
        bullets = bullets.filter { case (bullet, dx, dy) =>
          bullet.layoutX = bullet.layoutX() + dx
          bullet.layoutY = bullet.layoutY() + dy

          // Check if the bullet is out of bounds or hits the player
          if (!isInScene(bullet)) {
            bulletContainer.children.remove(bullet)
            false
          } else if (bullet.getBoundsInParent.intersects(player.getHitboxBounds)) {
            player.takeDamage(self.attackPower)
            bulletContainer.children.remove(bullet)
            false
          } else {
            true
          }
        }
      } else {
        stopShooting()
      }
    })

    shootingTimer.foreach(_.start()) // Start the animation timer
  }


  // Stop shooting method
  def stopShooting(): Unit = {
    // Stop the shooting animation timer
    shootingTimer.foreach(_.stop())
    shootingTimer = None

    // Remove all bullets from the scene
    bullets.foreach { case (bullet, _, _) =>
      bulletContainer.children.remove(bullet)
    }
    bullets = List() // Clear the list of bullets
  }

  // Method to check if the bullet is within the scene
  private def isInScene(bullet: ImageView): Boolean = {
    val scene = self.scene()
    scene != null && bullet.layoutX.value >= 0 &&
      bullet.layoutX.value <= scene.width.value &&
      bullet.layoutY.value >= 0 &&
      bullet.layoutY.value <= scene.height.value
  }
}
