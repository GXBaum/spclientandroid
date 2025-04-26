package de.rafaelbeckmann.hvkclient.playback

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}