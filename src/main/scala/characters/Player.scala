package characters

import main.MyApp
import scalafx.Includes._
import scalafx.animation.AnimationTimer
import scalafx.geometry.Bounds
import scalafx.scene.Scene
import scalafx.scene.image.Image
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration
import view.{DeathScreen, GameHUD}
import util.SoundEffect


class Player(scene: Scene, platforms: List[Platform], enemies: List[NormalEnemy],  boss: Boss, xInitial: Double, yInitial: Double, widthFactor: Double, heightFactor: Double, imagePath: String , _health: Int, _attack: Int)
  extends characters.Character(scene, imagePath, xInitial, yInitial, widthFactor, heightFactor, _health, _attack ) {

  private val playerSpeed = 10.0
  private val jumpHeight = 25.0
  private val gravity = 1.5
  private var velocityY = 0.0
  private var movingLeft = false
  private var movingRight = false
  private var isOnGround = true
  private var isOnPlatform = false
  private var jumpCount = 0
  private val maxJumps = 2
  private val groundHeightFactor = 0.9
  private var dropThroughPlatform = false
  private val attackRange = 200.0
  private var dead: Boolean = false
  private var currentBoss: Boss = boss
  protected var onDeath: () => Unit = () => {}
  private val maxHealth: Int = _health
  private var gameEnded: Boolean = false // Local gameEnded flag
  def getMaxHealth: Int = maxHealth
  def setOnDeath(handler: () => Unit): Unit = {
    onDeath = handler
  }

  private val hitbox = new Rectangle {
    width = getImageView.fitWidth() * 0.8
    height = getImageView.fitHeight() * 0.8
    stroke = Color.Red
    fill = Color.Transparent
  }


  // Ensure the hitbox follows the player's position
  hitbox.layoutX <== layoutX + (getImageView.fitWidth() * 0.3)
  hitbox.layoutY <== layoutY + (getImageView.fitHeight() * 0.3)
  // Add the hitbox to the scene (assuming the root is a Pane)
  def getHitboxBounds: Bounds = hitbox.boundsInParent()


  private val walkFrames = List(
    loadFrame("/Sprites/R1.png"),
    loadFrame("/Sprites/R2.png"),
    loadFrame("/Sprites/R3.png"),
    loadFrame("/Sprites/R4.png"),
    loadFrame("/Sprites/R5.png"),
    loadFrame("/Sprites/R6.png")
  ).flatten
  private val attackFrames = List(
    loadFrame("/Sprites/A1.png"),
    loadFrame("/Sprites/A2.png"),
    loadFrame("/Sprites/A3.png"),
    loadFrame("/Sprites/A4.png"),
    loadFrame("/Sprites/A5.png"),
    loadFrame("/Sprites/A6.png")
  ).flatten
  private val idleFrames = List(
    loadFrame("/Sprites/I1.png"),
    loadFrame("/Sprites/I2.png"),
    loadFrame("/Sprites/I3.png"),
    loadFrame("/Sprites/I4.png"),
    loadFrame("/Sprites/I5.png"),
    loadFrame("/Sprites/I56.png")
  ).flatten


  private def loadFrame(path: String): Option[Image] = {
    val resource = getClass.getResource(path)
    if (resource == null) {
      println(s"Error: Image resource not found at $path")
      None
    } else {
      Some(new Image(resource.toExternalForm))
    }
  }
  // Initialize the animation timers
  private var walkAnimationTimer: Option[AnimationTimer] = None
  private var attackAnimationTimer: Option[AnimationTimer] = None
  private var idleAnimationTimer: Option[AnimationTimer] = None


  private def startWalkAnimation(): Unit = {
    stopIdleAnimation()
    var currentFrame = 0
    val frameDuration = Duration(100) // 100ms per frame for walking animation
    var lastFrameChange = 0L

    walkAnimationTimer = Some(AnimationTimer { now =>
      if (movingLeft || movingRight) {  // Ensure animation only plays when moving
        if (now - lastFrameChange >= frameDuration.toMillis * 1000000 && walkFrames.nonEmpty) {
          currentFrame = (currentFrame + 1) % walkFrames.length
          getImageView.image = walkFrames(currentFrame)
          lastFrameChange = now
        }
      }
    })

    walkAnimationTimer.foreach(_.start())
  }
  private def stopWalkAnimation(): Unit = {
    walkAnimationTimer.foreach(_.stop())
    walkAnimationTimer = None
  }

  private def startAttackAnimation(): Unit = {
    stopIdleAnimation()
    stopWalkAnimation()
    var attackFrameIndex = 0
    val attackFrameDuration = Duration(65) // 50ms per frame for attack animation
    val attackStartTime = System.nanoTime()

    attackAnimationTimer = Some(AnimationTimer { _ =>
      val elapsedTime = (System.nanoTime() - attackStartTime) / 1000000 // Convert to milliseconds

      if (elapsedTime > attackFrameIndex * attackFrameDuration.toMillis && attackFrameIndex < attackFrames.length) {
        getImageView.image = attackFrames(attackFrameIndex)

        attackFrameIndex += 1
      } else if (attackFrameIndex >= attackFrames.length) {
        // Reset to idle frame after attack
        if (movingLeft || movingRight) {
          startWalkAnimation()
        } else {
          startIdleAnimation()
        }
        attackAnimationTimer.foreach(_.stop()) // Stop the attack animation timer
        attackAnimationTimer = None
      }
    })


    attackAnimationTimer.foreach(_.start())
  }

  private def startIdleAnimation(): Unit = {
    if (idleAnimationTimer.isDefined || movingLeft || movingRight || attackAnimationTimer.isDefined) return

    var currentFrame = 0
    val frameDuration = Duration(150) // 150ms per frame for idle animation
    var lastFrameChange = 0L

    idleAnimationTimer = Some(AnimationTimer { now =>
      if (now - lastFrameChange >= frameDuration.toMillis * 1000000 && idleFrames.nonEmpty) {
        currentFrame = (currentFrame + 1) % idleFrames.length
        getImageView.image = idleFrames(currentFrame)
        lastFrameChange = now
      }
    })

    idleAnimationTimer.foreach(_.start())
  }

  private def stopIdleAnimation(): Unit = {
    idleAnimationTimer.foreach(_.stop())
    idleAnimationTimer = None
  }
  override def die(): Unit = {
    if (!dead && !gameEnded) { //
      println("characters.Player is dead. Showing death screen...")
      stopAllPlayerActions()
      dead = true
      SoundEffect.playDieSound()
      showDeathScreen() // Show the player death screen
    }
  }

  private def stopAllPlayerActions(): Unit = {

    walkAnimationTimer.foreach(_.stop())
    attackAnimationTimer.foreach(_.stop())
    idleAnimationTimer.foreach(_.stop())
    animationTimer.stop()

    // Stop any ongoing input handling (keyboard and mouse events)
    scene.onKeyPressed = null
    scene.onKeyReleased = null
    scene.onMouseMoved = null
    scene.onMouseClicked = null
    enemies.foreach(_.stopAllAnimations())
    // Add any additional cleanup here, like resetting variables
    movingLeft = false
    movingRight = false
    jumpCount = 0
    velocityY = 0.0
  }
  private def showDeathScreen(): Unit = {
    stopAllPlayerActions()
    boss.stopAllActions()
    DeathScreen.setup(scene)  // Initialize the death screen
    scene.root = DeathScreen.getPane  // Set the scene root to the death screen pane
  }

  private def goToMainMenu(): Unit = {
    boss.stopAllActions()
    stopAllPlayerActions()
    MyApp.switchToMainMenu()
  }
  // Implement the abstract move method
  def move(): Unit = {
    if (movingLeft) {
      moveLeft()
    }
    if (movingRight) {
      moveRight()
    }

    if (movingLeft || movingRight) {
      if (walkAnimationTimer.isEmpty) startWalkAnimation() // Start walking animation if not already running
    } else if (attackAnimationTimer.isEmpty) {
      stopWalkAnimation() // Stop walking animation if not moving
      startIdleAnimation() // Start idle animation when not moving
    }
    applyGravity()
  }

  // Method to handle player movement
  private def moveLeft(): Unit = {
    val newX = layoutX.value - playerSpeed
    if (newX >= 0) {
      layoutX.value = newX
      getImageView.scaleX = -1 // Flip horizontally
    }
  }

  private def moveRight(): Unit = {
    val newX = layoutX.value + playerSpeed
    if (newX + getImageView.fitWidth.value <= scene.width.value) {
      layoutX.value = newX
      getImageView.scaleX = 1 // Normal orientation
    }
  }

  private def jump(): Unit = {
    if (jumpCount < maxJumps) {
      velocityY = -jumpHeight
      isOnGround = false
      jumpCount += 1
    }
  }

  private def drop(): Unit = {
    if (isOnPlatform) {
      dropThroughPlatform = true
      velocityY = gravity // Set a downward velocity to simulate dropping
      isOnPlatform = false
      println("Drop initiated") // Debugging line
    }
  }

  // Method to apply gravity and check for landing on platforms
  private def applyGravity(): Unit = {
    layoutY.value += velocityY
    velocityY += gravity

    // Ensure the player does not jump out of the top boundary
    if (layoutY.value < 0) {
      layoutY.value = 0
      velocityY = 0.0
    }

    // Check for collision with platforms
    val groundY = scene.height.value * groundHeightFactor
    val playerBottom = layoutY.value + getImageView.fitHeight.value

    if (playerBottom >= groundY) {
      layoutY.set(groundY - getImageView.fitHeight.value)
      velocityY = 0.0
      isOnGround = true
      isOnPlatform = false
      jumpCount = 0
      dropThroughPlatform = false // Reset the drop flag when landing on ground
    } else {
      val landedPlatform = platforms.find(platform =>
        layoutX.value + getImageView.fitWidth.value > platform.layoutX.value &&
          layoutX.value < platform.layoutX.value + platform.width.value &&
          playerBottom >= platform.layoutY.value &&
          playerBottom <= platform.layoutY.value + velocityY
      )

      landedPlatform match {
        case Some(platform) if velocityY >= 0 && !dropThroughPlatform =>
          layoutY.set(platform.layoutY.value - getImageView.fitHeight.value)
          velocityY = 0.0
          isOnGround = false
          isOnPlatform = true
          jumpCount = 0
        case _ =>
          isOnGround = false
          isOnPlatform = false
      }
    }
  }

  // Set up key event handlers
  scene.onKeyPressed = (event: KeyEvent) => {
    event.code match {
      case KeyCode.A => movingLeft = true
      case KeyCode.D => movingRight = true
      case KeyCode.Space => jump()
      case KeyCode.S => drop()
      case _ =>
    }
  }

  scene.onKeyReleased = (event: KeyEvent) => {
    event.code match {
      case KeyCode.A => movingLeft = false
      case KeyCode.D => movingRight = false
      case _ =>
    }
  }

  // Handle mouse movement and clicking
  scene.onMouseMoved = (event: MouseEvent) => {
    val mouseX = event.sceneX
    if (mouseX < layoutX.value + getImageView.fitWidth.value / 2) {
      getImageView.scaleX = -1 // Face left
    } else {
      getImageView.scaleX = 1  // Face right
    }
  }
  override def attack(target: characters.Character): Unit = {
    if (!target.isDead) {
      println(s"Attacking target: ${target.getClass.getSimpleName}")
      SoundEffect.playSlashEffectSound()
      target.takeDamage(_attack)
      if (target.isInstanceOf[Boss]) {
        println(s"characters.Boss health: ${target.getHealth}, Is boss dead? ${target.isDead}")
      }
    }
  }
  override def takeDamage(damage: Int): Unit = {
    if (!gameEnded && !dead) { // Check local gameEnded flag
      health -= damage
      if (health <= 0) {
        die()
      }
    }
  }

  // Handle mouse movement and clicking
  scene.onMouseClicked = (event: MouseEvent) => {
    if (attackAnimationTimer.isEmpty) {
      startAttackAnimation()
    }

    val playerCenterX = getCenterX
    val playerCenterY = getCenterY
    var attacked = false

    if (currentBoss != null && currentBoss.isVisible && !currentBoss.isDead) {
      val distanceToBoss = math.hypot(playerCenterX - currentBoss.getCenterX, playerCenterY - currentBoss.getCenterY)
      if (distanceToBoss <= attackRange) {
        println(s"Attacking characters.Boss at distance $distanceToBoss")
        attack(currentBoss)
        attacked = true
      }
    } else {
      println("characters.Boss is out of range.")
    }
    println(s"characters.Boss health: ${boss.health}, Is boss dead? ${boss.isDead}")
    // Proceed with enemy attack logic only if the boss wasn't attacked
    if (!attacked) {
      val closestEnemyOption = enemies.filterNot(_.isDead).reduceOption { (enemy1, enemy2) =>
        val distance1 = math.hypot(playerCenterX - enemy1.getCenterX, playerCenterY - enemy1.getCenterY)
        val distance2 = math.hypot(playerCenterX - enemy2.getCenterX, playerCenterY - enemy2.getCenterY)
        if (distance1 < distance2) enemy1 else enemy2
      }

      closestEnemyOption match {
        case Some(closestEnemy) =>
          val distanceToEnemy = math.hypot(playerCenterX - closestEnemy.getCenterX, playerCenterY - closestEnemy.getCenterY)
          if (distanceToEnemy <= attackRange) {
            println(s"Attacking ${closestEnemy.getClass.getSimpleName} at distance $distanceToEnemy")
            attack(closestEnemy)
          } else {
            println("No enemies within range to attack.")
          }
        case None =>
          println("No enemies available to attack.")
      }
    }
  }

  // Animation timer for handling movement and gravity
  private val animationTimer: AnimationTimer = AnimationTimer { _ =>
    move() // Call move method
  }
  // Start the animation timer
  animationTimer.start()
}
