package characters

import scalafx.scene.Scene
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Pane

class Platform(scene: Scene, xFactor: Double, yFactor: Double, widthFactor: Double, heightFactor: Double, imagePath: String) extends Pane {
  // Load the image for the platform
  private val image = new Image(getClass.getResource(imagePath).toExternalForm)

  // Create an ImageView for the image
  private val imageView = new ImageView(image) {
    preserveRatio = false
  }
  // Add the ImageView to the Pane
  children = List(imageView)

  // Set initial size and position
  updateLayout()

  // Update size and position on window resize
  scene.width.onChange((_, _, _) => updateLayout())
  scene.height.onChange((_, _, _) => updateLayout())

  def updateLayout(): Unit = {
    // Check that scene dimensions are valid before updating
    if (scene.width.value > 0 && scene.height.value > 0) {
      imageView.fitWidth = scene.width.value * widthFactor
      imageView.fitHeight = scene.height.value * heightFactor
      layoutX = scene.width.value * xFactor
      layoutY = scene.height.value * yFactor
    }
  }
}
