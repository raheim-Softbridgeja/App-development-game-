package com.islandmatch.yardvibes.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

class SoundManager(private val context: Context) {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    fun playClick() {
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    fun playMatch() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    fun playSwap() {
        toneGenerator.startTone(ToneGenerator.TONE_SUP_PIP, 80)
    }

    fun playError() {
        toneGenerator.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
    }

    fun release() {
        toneGenerator.release()
    }
}
