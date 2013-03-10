package com.silverkeytech.android_rivers

import android.app.Service
import android.app.Service.START_STICKY
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnErrorListener
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import android.net.Uri
import android.util
import android.util.Log
import android.os.PowerManager
import android.widget.RemoteViews
import android.app.Notification
import android.app.PendingIntent
import android.support.v4.app.NotificationCompat
import java.util.Random
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS

public open class PodcastPlayerService(): Service(), MediaPlayer.OnErrorListener, OnAudioFocusChangeListener {
    class object{
        public val TAG: String = javaClass<PodcastPlayerService>().getSimpleName()
    }

    private val binder: IBinder = ServiceBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var length: Int = 0

    inner class ServiceBinder(): Binder() {
        fun getService(): PodcastPlayerService? {
            return this@PodcastPlayerService
        }
    }

    public override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    fun prepareNotification(): Notification {
        val notificationIntent = Intent(Intent.ACTION_MAIN)
        notificationIntent.setClass(getApplicationContext(), javaClass<MainWithFragmentsActivity>())

        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(this)
                .setTicker("Start playing Podcast")
        ?.setWhen(System.currentTimeMillis())
        ?.setContentIntent(contentIntent)
        ?.build()

        notification!!.icon = android.R.drawable.star_big_on

        notification.contentView = RemoteViews(getApplicationContext()!!.getPackageName(), R.layout.notification_podcast_player).with {
            this.setImageViewResource(R.id.notification_podcast_player_status_icon, android.R.drawable.btn_star)
            this.setTextViewText(R.id.notification_podcast_player_status_text, getString(R.string.download_starts))
        }

        return notification
    }

    private val notificationId = Random().nextLong().toInt()
    private var notification : Notification? = null
    private var notificationManager : NotificationManager? = null
    private var podcastTitle : String? = null
    private var podcastPath : String? = null

    fun updateText(msg: String) {
        notification!!.contentView!!.setTextViewText(R.id.notification_podcast_player_status_text, msg)
        notificationManager!!.notify(notificationId, notification)
    }

    var audioManager : AudioManager? = null

    //http://developer.android.com/training/managing-audio/audio-focus.html
    public override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        podcastTitle = intent!!.getStringExtra(Params.PODCAST_TITLE)
        podcastPath = intent.getStringExtra(Params.PODCAST_PATH)
        notification = prepareNotification()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            updateText("Sorry, I cannot play $podcastTitle at this moment")
        }
        else{
            updateText("Playing $podcastTitle")

            mediaPlayer = MediaPlayer.create(this, Uri.parse(podcastPath))
            mediaPlayer?.setOnErrorListener(this)
            mediaPlayer?.setLooping(false)
            mediaPlayer?.setVolume(100.0, 100.0)
            mediaPlayer?.start()

            mediaPlayer!!.setOnCompletionListener(object: MediaPlayer.OnCompletionListener{
                public override fun onCompletion(p0: MediaPlayer?) {
                    updateText("Podcast completed")
                }
            })
        }

        return super<Service>.onStartCommand(intent, flags, startId)
    }

    public override fun onAudioFocusChange(p0: Int) {
        if (p0 == AUDIOFOCUS_LOSS_TRANSIENT){
            this.pauseMusic()
        }
        else if (p0 == AudioManager.AUDIOFOCUS_GAIN) {
            this.resumeMusic()
        } else if (p0 == AudioManager.AUDIOFOCUS_LOSS) {
            audioManager?.abandonAudioFocus(this)
            this.stopMusic()
        }
    }

    public fun isPlaying() : Boolean{
        if (mediaPlayer != null)
            return mediaPlayer!!.isPlaying()
        else
            return false
    }

    public fun pauseMusic(): Unit {
        if (mediaPlayer!!.isPlaying()){
            mediaPlayer!!.pause()
            length = mediaPlayer!!.getCurrentPosition()
        }
    }

    public fun resumeMusic(): Unit {
        if (!mediaPlayer!!.isPlaying())
        {
            mediaPlayer?.seekTo(length)
            mediaPlayer?.start()
        }
    }

    public fun stopMusic(): Unit {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    public override fun onDestroy(): Unit {
        super<Service>.onDestroy()
        if (mediaPlayer != null){
            try {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
            }
            finally {
                mediaPlayer = null
            }
        }
    }

    public override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Toast.makeText(this, "Music player failed", Toast.LENGTH_SHORT)?.show()
        if (mediaPlayer != null){
            try {
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
            }
            finally {
                mediaPlayer = null
            }
        }
        return false
    }
}