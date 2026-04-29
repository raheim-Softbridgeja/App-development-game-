package com.islandmatch.yardvibes.ui

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

class MusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var toneLoopTrack: AudioTrack? = null
    private var activeTrackName: String? = null
    private var muted = false

    fun playMenuLoop() {
        playLoop("menu_theme")
    }

    fun playGameplayLoop() {
        playLoop("gameplay_theme")
    }

    fun isMuted(): Boolean = muted

    fun toggleMuted(): Boolean {
        muted = !muted
        if (muted) {
            mediaPlayer?.pause()
            toneLoopTrack?.pause()
        } else {
            activeTrackName?.let { playLoop(it) }
        }
        return muted
    }

    fun release() {
        stop()
    }

    private fun playLoop(trackName: String) {
        if (activeTrackName == trackName && (mediaPlayer?.isPlaying == true || toneLoopTrack?.playState == AudioTrack.PLAYSTATE_PLAYING)) {
            return
        }

        activeTrackName = trackName
        if (muted) {
            return
        }

        val rawId = context.resources.getIdentifier(trackName, "raw", context.packageName)
        stop()
        if (rawId != 0) {
            mediaPlayer = MediaPlayer.create(context, rawId)?.apply {
                isLooping = true
                setVolume(0.35f, 0.35f)
                start()
            }
            return
        }

        toneLoopTrack = runCatching { createProceduralLoop(trackName) }.getOrNull()?.apply {
            play()
        }
    }

    private fun stop() {
        mediaPlayer?.run {
            runCatching { stop() }
            release()
        }
        mediaPlayer = null

        toneLoopTrack?.run {
            runCatching { pause() }
            runCatching { flush() }
            release()
        }
        toneLoopTrack = null
    }

    private fun createProceduralLoop(trackName: String): AudioTrack {
        val sampleRate = 22_050
        val secondsPerChord = if (trackName == "menu_theme") 1.3 else 0.95
        val progression = if (trackName == "menu_theme") {
            listOf(
                doubleArrayOf(261.63, 329.63, 392.0),
                doubleArrayOf(293.66, 369.99, 440.0),
                doubleArrayOf(220.0, 277.18, 329.63),
                doubleArrayOf(246.94, 311.13, 392.0),
            )
        } else {
            listOf(
                doubleArrayOf(174.61, 220.0, 261.63),
                doubleArrayOf(196.0, 246.94, 293.66),
                doubleArrayOf(220.0, 277.18, 329.63),
                doubleArrayOf(196.0, 246.94, 329.63),
            )
        }

        val samplesPerChord = (sampleRate * secondsPerChord).toInt()
        val totalSamples = samplesPerChord * progression.size
        val pcm = ShortArray(totalSamples)

        for ((chordIndex, chord) in progression.withIndex()) {
            val baseIndex = chordIndex * samplesPerChord
            for (sampleIndex in 0 until samplesPerChord) {
                val time = sampleIndex / sampleRate.toDouble()
                val normalized = sampleIndex.toDouble() / samplesPerChord.toDouble()
                val attack = if (normalized < 0.08) normalized / 0.08 else 1.0
                val release = if (normalized > 0.86) (1.0 - normalized) / 0.14 else 1.0
                val envelope = attack.coerceAtMost(release).coerceIn(0.0, 1.0)
                val sway = 1.0 + 0.06 * sin((2.0 * PI * 2.2 * time) + chordIndex)

                var mixed = 0.0
                for ((noteIndex, note) in chord.withIndex()) {
                    mixed += sin(2.0 * PI * note * time + noteIndex) * (0.14 - noteIndex * 0.025)
                }

                mixed += sin(2.0 * PI * (chord.first() / 2.0) * time) * 0.16
                mixed += sin(2.0 * PI * (chord.last() * 2.0) * time) * 0.05

                val sample = (mixed * envelope * sway * 0.85).coerceIn(-1.0, 1.0)
                pcm[baseIndex + sampleIndex] = (sample * Short.MAX_VALUE).toInt().toShort()
            }
        }

        val requiredBytes = pcm.size * Short.SIZE_BYTES
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )

        return AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            max(requiredBytes, minBufferSize),
            AudioTrack.MODE_STATIC,
        ).apply {
            write(pcm, 0, pcm.size)
            setLoopPoints(0, pcm.size, -1)
            setStereoVolume(0.28f, 0.28f)
        }
    }
}
