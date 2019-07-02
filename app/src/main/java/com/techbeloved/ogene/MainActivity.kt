package com.techbeloved.ogene

import android.content.ComponentName
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.techbeloved.ogene.databinding.ActivityMainBinding
import timber.log.Timber

private const val STATE_PLAYING = 1
private const val STATE_PAUSED = 0
class MainActivity : AppCompatActivity() {
    private var currentState: Int = STATE_PAUSED
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat

    private lateinit var binding: ActivityMainBinding

    private val mediaControllerCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                state ?: return

                when (state.state) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        currentState = STATE_PLAYING
                        binding.buttonPlayPause.text = "Pause"
                    }
                    else -> {
                        currentState = STATE_PAUSED
                        binding.buttonPlayPause.text = "Play"
                    }
                }
            }
        }
    private val connectionCallback: MediaBrowserCompat.ConnectionCallback =
        object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                super.onConnected()
                Log.i("MediaBrowser", "we are connected")
                try {
                    val token = mediaBrowser.sessionToken
                    mediaController = MediaControllerCompat(this@MainActivity, token)
                    mediaController.registerCallback(mediaControllerCallback)
                    MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
                    MediaControllerCompat.getMediaController(this@MainActivity).transportControls.playFromMediaId("${R.raw.you_alone}", null)
                }  catch (e : RemoteException) {
                    Log.w("MainActivity", e)
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        mediaBrowser = MediaBrowserCompat(this, ComponentName(this, MusicService::class.java),
            connectionCallback, intent.extras)
        mediaBrowser.connect()

        binding.buttonPlayPause.setOnClickListener { v ->
            val controller = MediaControllerCompat.getMediaController(this)
            if (controller != null) {
                when {
                    currentState == STATE_PAUSED -> {
                        controller.transportControls.play()
                        currentState = STATE_PLAYING
                    }
                    controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING -> {
                        controller.transportControls.pause()
                        currentState = STATE_PAUSED
                    }
                    else -> Toast.makeText(this, "Media not in playable state!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (MediaControllerCompat.getMediaController(this).playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.getMediaController(this).transportControls.pause()
        }
        mediaBrowser.disconnect()
    }
}
