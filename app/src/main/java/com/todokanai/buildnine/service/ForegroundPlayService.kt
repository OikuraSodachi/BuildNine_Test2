package com.todokanai.buildnine.service

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.lifecycle.MutableLiveData
import androidx.media.app.NotificationCompat.MediaStyle
import com.todokanai.buildnine.R
import com.todokanai.buildnine.activity.MainActivity
import com.todokanai.buildnine.receiver.TrackBroadcastReceiver
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.tool.TrackTool

class ForegroundPlayService : Service() {

    override fun onBind(p0: Intent?): IBinder {
        return Binder()
    }

    val CHANNEL_ID = "ForegroundPlayServiceChannel"

    private var isServiceOn: Boolean = false        // 여러개의 service instance 방지용 변수

    companion object {
        val ACTION_SKIP_TO_PREVIOUS = "prev"
        val ACTION_SKIP_TO_NEXT = "next"
        val ACTION_PAUSE_PLAY = "pauseplay"
        val ACTION_CLOSE = "close"
        val ACTION_REPLAY = "replay"
        val mCurrent: MutableLiveData<Int> = MutableLiveData()        // 현재 곡 인덱스                              // 현재 곡의 인덱스
        val isPlayingNow: MutableLiveData<Boolean> = MutableLiveData(false)     // 재생중 여부
        val isLoopingNow: MutableLiveData<Boolean> = MutableLiveData()                // 반복재생 여부
        val isShuffled: MutableLiveData<Boolean> = MutableLiveData()   // Shuffle 여부
        var mediaPlayer: MediaPlayer = MediaPlayer()
        lateinit var playListInfo: List<RoomTrack>
    }               // ACTION 종류 선언

    override fun onCreate() {
        super.onCreate()
    }

    fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_NONE             //  알림의 중요도
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(serviceChannel)
    }                  // 서비스 채널 생성

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("service","onStartCommand")
        createNotificationChannel()

        Log.d("tested", "isServiceOn: $isServiceOn")
        if (!isServiceOn) {
            playListInfo = TrackTool(this).playList     // 파일 목록에 변동 있을경우 다시 실행해야함
            mCurrent.value = TrackTool(this).helper.roomPlayerDao().mCurrent()
            isLoopingNow.value = TrackTool(this).helper.roomPlayerDao().isLooping()
            isShuffled.value = TrackTool(this).helper.roomPlayerDao().isShuffled()
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_REPLAY))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_SKIP_TO_PREVIOUS))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_PAUSE_PLAY))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_SKIP_TO_NEXT))
            registerReceiver(TrackBroadcastReceiver(), IntentFilter(ACTION_CLOSE))
            isServiceOn = true
        }
        //------------------------------------------

        val mainOpenIntent = Intent(this,MainActivity::class.java)
        val mainIntent = PendingIntent.getActivity(this,0,Intent(mainOpenIntent),PendingIntent.FLAG_IMMUTABLE)

        val repeatIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_REPLAY), PendingIntent.FLAG_IMMUTABLE)
        val prevIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_SKIP_TO_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
        val pauseplayIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_PAUSE_PLAY), PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_SKIP_TO_NEXT), PendingIntent.FLAG_IMMUTABLE)
        val closeIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_CLOSE), PendingIntent.FLAG_IMMUTABLE)

        val notiTitle =
            if(playListInfo.isEmpty()) {
                "null"
            }else{
                playListInfo[mCurrent.value!!].title
            }

        val notiArtist =
            if(playListInfo.isEmpty()) {
                "null"
            }else{
                playListInfo[mCurrent.value!!].artist
            }

        val notiAlbumArt =
            if(playListInfo.isEmpty()) {
                null
            }else{
                playListInfo[mCurrent.value!!].getAlbumUri()
            }

        val mediaSession = MediaSessionCompat(this, "MediaNotification")
        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "$notiTitle")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "$notiArtist")
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, notiAlbumArt.toString())
                .build()
        )
       // mediaSession.release()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)       // 알림바에 띄울 알림을 만듬
            .setContentTitle(if(playListInfo.isEmpty()){"null"}else{"${playListInfo[mCurrent.value!!].title}"}) // 알림의 제목
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setVisibility(VISIBILITY_PUBLIC)
            .addAction(NotificationCompat.Action(TrackTool(this).setLoopingImage(), "REPEAT", repeatIntent))
            .addAction(NotificationCompat.Action(R.drawable.ic_baseline_skip_previous_24,"PREV",prevIntent))
            .addAction(NotificationCompat.Action(TrackTool(this).setPausePlayImage(), "pauseplay", pauseplayIntent))
            .addAction(NotificationCompat.Action(R.drawable.ic_baseline_skip_next_24,"NEXT",nextIntent))
            .addAction(NotificationCompat.Action(R.drawable.ic_baseline_close_24, "CLOSE", closeIntent))
            .setContentIntent(mainIntent)
            .setStyle(
                MediaStyle()
                    .setShowActionsInCompactView(1, 2, 3)     // 확장하지 않은상태 알림에서 쓸 기능의 배열번호
                    .setMediaSession(mediaSession.sessionToken)
            )
            .build()

        startForeground(1, notification)              // 지정된 알림을 실행

        return super.onStartCommand(intent, flags, startId)
    }  // 서비스 활동개시

    override fun onDestroy() {
        super.onDestroy()
        Log.d("service", "onDestroy")
    }
}
// fun close() 실행으로 종료후 계속 mediastyle 알림이 계속 살아남