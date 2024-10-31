package characters

import scalafx.animation.AnimationTimer
import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane

import scala.util.Random

class Boss(scene: Scene, gamePane: Pane, platforms: List[Platform], widthFactor: Double, heightFactor: Double, imagePath: String, _health: Int, _attack: Int)
  extends Character(scene, imagePath, 0, 0, widthFactor, heightFactor, _health, _attack) {

  private var shootingTimer: Option[AnimationTimer] = None
  private var ultimateTimer: Option[AnimationTimer] = None
  private var teleportTimer: Option[AnimationTimer] = None
  private val shootingInterval = 2000 // 2 seconds interval between shots
  private var lastShotTime = 0L // Track the last time the boss shot a particle
  private var gameEnded: Boolean = false

  // Load GIF for the boss and set it as the ImageView
  private val bossGif = new Image(getClass.getResourceAsStream(imagePath))
  getImageView.image = bossGif
  private val maxHealth: Int = _health // Store the maximum health

  def getMaxHealth: Int = maxHealth

  def startBossBehavior(player: Player): Unit = {
    startFiring(player)
    startUltimateAttack(player)
    startTeleporting()
    player.setOnDeath(() => stopAllActions())
    this.show()
  }

  def stopAllActions(): Unit = {
    stopFiring()
    stopTeleporting()
    ultimateTimer.foreach(_.stop())
  }

  def hide(): Unit = {
    this.setVisible(false)
  }

  private def show(): Unit = {
    this.setVisible(true)
  }

  // Method to make the boss face the player
  private def facePlayer(player: Player): Unit = {
    if (player.getCenterX < getCenterX) {
      getImageView.scaleX = -1 // Face left
    } else {
      getImageView.scaleX = 1  // Face right
    }
  }

  def spawnOnPlatform(): Unit = {
    teleportToRandomPlatform()
  }

  // Method to start firing particles at the player
  private def startFiring(player: Player): Unit = {
    shootingTimer = Some(AnimationTimer { now =>
      if (now - lastShotTime > shootingInterval * 1e6) { // Convert interval to nanoseconds
        fireParticleAtPlayer(player)
        lastShotTime = now
        facePlayer(player) // Make the boss face the player when firing
      }
    })
    shootingTimer.foreach(_.start())
  }

  private def stopFiring(): Unit = {
    shootingTimer.foreach(_.stop())
    shootingTimer = None
  }
  //https://chatgpt.com
  // Method to fire a particle towards the player and deal damage according to the boss's attack power
  private def fireParticleAtPlayer(player: Player): Unit = {
    val rocketImage = new Image(getClass.getResource("/Images/rocket.png").toExternalForm)
    val rocket = new ImageView(rocketImage) {
      fitWidth = 60 // Adjust the size
      fitHeight = 60
      layoutX = getCenterX
      layoutY = getCenterY
    }

    // Calculate the angle to the player
    val targetX = player.getCenterX
    val targetY = player.getCenterY
    val angle = math.atan2(targetY - getCenterY, targetX - getCenterX)

    // Rotate the rocket to face the player
    rocket.rotate = math.toDegrees(angle) + 90

    gamePane.children.add(rocket)

    val particleMovement = AnimationTimer { _ =>
      rocket.layoutX = rocket.layoutX.value + math.cos(angle) * 2
      rocket.layoutY = rocket.layoutY.value + math.sin(angle) * 2

      // Check if the rocket collides with the player's hitbox
      if (!isDead && rocket.getBoundsInParent.intersects(player.getHitboxBounds)) {
        player.takeDamage(_attack)
        gamePane.children.remove(rocket)
      }

      // Remove the rocket if it goes out of bounds
      if (particleOutOfBounds(rocket)) {
        gamePane.children.remove(rocket)
      }
    }
    particleMovement.start()
  }

  private def particleOutOfBounds(particle: ImageView): Boolean = {
    particle.layoutX.value < 0 || particle.layoutX.value > scene.width.value ||
      particle.layoutY.value < 0 || particle.layoutY.value > scene.height.value
  }

  // Ultimate attack that shoots 4 particles in all directions and is not blocked by platforms
  private def startUltimateAttack(player: Player): Unit = {
    ultimateTimer = Some(AnimationTimer { now =>
      if (now % (10 * 1e9) < 1e9) {
        fireUltimate(player)
      }
    })
    ultimateTimer.foreach(_.start())
  }

  private def fireUltimate(player: Player): Unit = {
    val numParticles = 4
    for (i <- 0 until numParticles) {
      val angle = 2 * math.Pi * i / numParticles  // Calculate the angle for each projectile
      fireUltimateParticleInDirection(angle, player)  // Fire the projectile in that direction
    }
  }

  // Ultimate particles are not blocked by platforms
  private def fireUltimateParticleInDirection(angle: Double, player: Player): Unit = {
    val particleImage = new Image(getClass.getResource("/Images/rocket.png").toExternalForm)
    val particle = new ImageView(particleImage) {
      fitWidth = 60
      fitHeight = 60
      layoutX = getCenterX
      layoutY = getCenterY
      rotate = math.toDegrees(angle) + 90
    }

    // Add the particle to the game pane
    gamePane.children.add(particle)

    // Move the particle in the given direction
    val particleMovement = AnimationTimer { _ =>
      particle.layoutX = particle.layoutX.value + math.cos(angle) * 2
      particle.layoutY = particle.layoutY.value + math.sin(angle) * 2

      // Check if the particle hits the player or is out of bounds
      if (!isDead && particle.getBoundsInParent.intersects(player.getHitboxBounds)) {
        println("Rocket hit the player at (" + particle.layoutX.value + ", " + particle.layoutY.value + ")")
        player.takeDamage(_attack) // Use the boss's attack power
        gamePane.children.remove(particle) // Remove the particle from the game pane
      }

      // Remove the particle if it goes out of bounds
      if (particleOutOfBounds(particle)) {
        gamePane.children.remove(particle)
      }
    }
    particleMovement.start()
  }
  //https://chatgpt.com
  // Method to teleport the boss to a random platform
  private def startTeleporting(): Unit = {
    teleportTimer = Some(AnimationTimer { now =>
      if (now % (5 * 1e9) < 1e9) {
        teleportToRandomPlatform()
      }
    })
    teleportTimer.foreach(_.start())
  }

  private def stopTeleporting(): Unit = {
    teleportTimer.foreach(_.stop())
    teleportTimer = None
  }

  private def teleportToRandomPlatform(): Unit = {
    val randomPlatform = platforms(Random.nextInt(platforms.length))

    // Position the boss on the random platform
    layoutX = randomPlatform.layoutX.value + randomPlatform.width.value / 2 - getImageView.fitWidth.value / 2
    layoutY = randomPlatform.layoutY.value - getImageView.fitHeight.value // Set boss on top of the platform

    println(s"characters.Boss has teleported to platform at X: ${randomPlatform.layoutX.value}, Y: ${randomPlatform.layoutY.value}")
  }

  // Override the die method for boss-specific behavior
  override def die(): Unit = {
    if (!gameEnded) { // Check local gameEnded flag
      stopAllActions()
      println("The boss has been defeated!")
      gameEnded = true // Set gameEnded when the boss dies
    }
  }

  override def move(): Unit = {
    // characters.Boss does not move traditionally, it only teleports between platforms
  }

  override def takeDamage(damage: Int): Unit = {
    if (!gameEnded) { // Check local gameEnded flag
      health -= damage
      println(s"characters.Boss took $damage damage. Remaining health: $health")
      if (health <= 0) {
        die()
      }
    }
  }
}
