package util

import javafx.scene.media.{Media, MediaPlayer}
import java.io.File

object SoundEffect {
  private val dieSound = createMediaPlayer("src/main/resources/Sound/Die.mp3", 0.3)
  private val enterGameSound = createMediaPlayer("src/main/resources/Sound/Enter.mp3", 0.3)
  private val slashEffectSound = createMediaPlayer("src/main/resources/Sound/SlashEffect.mp3", 0.3)
  private val themeSong = createMediaPlayer("src/main/resources/Sound/Theme.mp3", 0.3)

  // Helper method to create MediaPlayer for a sound file with volume
  private def createMediaPlayer(filePath: String, volume: Double): MediaPlayer = {
    val media = new Media(new File(filePath).toURI.toString)
    val player = new MediaPlayer(media)
    player.setVolume(volume)
    player
  }

  private def playSound(player: MediaPlayer): Unit = {
    player.stop()
    player.play()
  }

  def playDieSound(): Unit = playSound(dieSound)
  def playEnterGameSound(): Unit = playSound(enterGameSound)
  def playSlashEffectSound(): Unit = playSound(slashEffectSound)
  def playThemeSong(): Unit = {
    themeSong.setCycleCount(MediaPlayer.INDEFINITE) // Loop the theme song
    playSound(themeSong)
  }
  def stopThemeSong(): Unit = themeSong.stop()
}
