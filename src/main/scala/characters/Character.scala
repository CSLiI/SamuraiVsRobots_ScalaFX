package characters

import scalafx.beans.binding.Bindings
import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane

abstract class Character(scene: Scene, imagePath: String, initialX: Double, initialY: Double, widthFactor: Double, heightFactor: Double, var health: Int, var attackPower: Int) extends Pane {
  // Load the character image
  private val imageStream = getClass.getResourceAsStream(imagePath)
  if (imageStream == null) {
    throw new RuntimeException(s"Error: Image resource not found at $imagePath")
  }
  private val image = new Image(imageStream)
  if (image.isError) {
    throw new RuntimeException(s"Error loading character image: ${image.getException}")
  }

  // Create an ImageView for the character
  private val imageView = new ImageView(image) {
    preserveRatio = false
  }

  // Add the ImageView to the Pane
  children = List(imageView)

  // Initial position
  layoutX = initialX
  layoutY = initialY

  // Bind the ImageView's size to the scene's width and height, based on the width and height factors
  if (scene != null) {
    imageView.fitWidth <== Bindings.createDoubleBinding(() => Option(scene.width).map(_.value * widthFactor).getOrElse(0.0), scene.width)
    imageView.fitHeight <== Bindings.createDoubleBinding(() => scene.height.value * heightFactor, scene.height)
  }
  // Abstract method for movement, to be implemented by subclasses
  def move(): Unit

  // Method to attack another character
  def attack(target: Character): Unit = {
    target.takeDamage(attackPower) // Reduce the target's health by the player's attack power
    println(s"Attacked ${target.getClass.getSimpleName}. Target's remaining health: ${target.getHealth}")

    if (target.isDead) {
      target.die() // Trigger death logic if the target is dead
    }
  }

  // Method to handle taking damage
  def takeDamage(damage: Int): Unit = {
    health -= damage
    if (health <= 0) {
      die()
    } else {
      println(s"${this.getClass.getSimpleName} takes $damage damage, remaining health: $health")
    }
  }

  // Method to handle character death
  def die(): Unit = {
    println(s"${this.getClass.getSimpleName} has died.")
    // Remove from scene or play death animation
    this.visible = false
  }
  def getHealth: Int = health
  def isDead: Boolean = health <= 0
  def getCenterX: Double = layoutX.value + imageView.fitWidth.value / 2
  def getCenterY: Double = layoutY.value + imageView.fitHeight.value / 2
  def getImageView: ImageView = imageView
}
