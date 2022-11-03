package com.todokanai.buildnine.tool

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.room.Room
import com.todokanai.buildnine.R
import com.todokanai.buildnine.room.RoomHelper
import com.todokanai.buildnine.room.RoomPlayer
import com.todokanai.buildnine.room.RoomTrack
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isLoopingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isPlayingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isShuffled
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.mCurrent
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.playListInfo

class TrackTool (context: Context?){

    var helper = Room.databaseBuilder(context!!, RoomHelper::class.java, "room_db")
            .allowMainThreadQueries()
            .build()

    val mPath =  helper.roomPathDao().getAll()   // 지정할 경로들.  경로 "들"!! 여러개 경로
    val mPathSize = mPath.size
    var context: Context? = null
    val myContext = context
    val playlist = helper.roomTrackDao().getAll()        // 전체목록 playList 확인완료
    var playList = playlist
    val intentTrigger: Intent
        get() {
            return Intent(myContext,ForegroundPlayService::class.java)
        }
// MediaStore.MediaColumns.DATA  파일 실제경로???
    var mediaPlayer = ForegroundPlayService.mediaPlayer

    fun scanTrackList(): List<RoomTrack> {
        // 1. 음원 정보 주소
        val listUrl = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI // URI 값을 주면 나머지 데이터 모아옴
        // 2. 음원 정보 자료형 정의
        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.MediaColumns.DATA
        )
        // 3. 컨텐트리졸버의 쿼리에 주소와 컬럼을 입력하면 커서형태로 반환받는다
        val cursor = myContext?.contentResolver?.query(listUrl,proj, null,
            null,null)
        val trackList = mutableListOf<RoomTrack>()
        while (cursor?.moveToNext() == true) {
            val id = cursor.getString(0)
            val title = cursor.getString(1)
            val artist = cursor.getString(2)
            val albumId = cursor.getString(3)
            val duration = cursor.getLong(4)
            val fileDir = cursor.getString(5)

            val track = RoomTrack(id, title, artist, albumId, duration, fileDir)
            if(mPath.isEmpty()){
                trackList.add(track)
            }else{
                for(a in 1..mPathSize){
                    if(fileDir.startsWith(mPath[a-1])){
                        Log.d("success", fileDir)
                        trackList.add(track)
                    }
                }
            }
        }

        cursor?.close()
        trackList.sortBy { it.title }       // 제목기준으로 정렬
        return trackList    // track 전체 반환
    }

    fun setPausePlayImage(): Int {
        if (isPlayingNow.value == true) {
            return R.drawable.ic_baseline_pause_24
        } else {
            return R.drawable.ic_baseline_play_arrow_24 }
    }

    fun setLoopingImage(): Int {
        if (isLoopingNow.value == true) {
            return R.drawable.ic_baseline_repeat_one_24
        } else {
            return R.drawable.ic_baseline_repeat_24
        }
    }


    fun replay() {
        if(isLoopingNow.value == true){
            isLoopingNow.value = false
            mediaPlayer.isLooping = false
        }else{
            isLoopingNow.value =true
            mediaPlayer.isLooping = true
        }
        myContext?.startForegroundService(intentTrigger)
    }       // 반복재생

    fun prev(){
        if(playListInfo.isNotEmpty()) {
            mPrev()
            setTrack()
            start()
        }
    }       // 이전곡
    fun pauseplay(){
        if(playListInfo.isNotEmpty()) {
            if (isPlayingNow.value!!) {
                mediaPlayer.pause()
                myContext?.startForegroundService(intentTrigger)
                isPlayingNow.value = mediaPlayer.isPlaying
            } else {
                start()
            }
        }
    }       // 일시정지,재생
    fun next(){
        if(playListInfo.isNotEmpty()) {
            mNext()
            setTrack()
            start()
        }
    }       // 다음곡
    fun close() {
        Toast.makeText(myContext,"미완성 기능", LENGTH_SHORT).show()

    }       // 종료
    fun shuffle(){
        val focusedTrack = playListInfo[mCurrent.value!!].getTrackUri()
        if(playListInfo.isNotEmpty()){
            if(isShuffled.value==true) {
                playListInfo = playListInfo.sortedBy { it.title }
                isShuffled.value = false
            } else{
                playListInfo = playListInfo.shuffled()
                isShuffled.value = true
            }
        }
        for(a in 1..playListInfo.size) {
            if(playListInfo[a-1].getTrackUri() == focusedTrack){
                mCurrent.value = a-1
                break
            }
        }               // mCurrent 위치 보정
    }
    fun start() {
        mediaPlayer.setOnCompletionListener{if(!mediaPlayer.isLooping){next()}}
        mediaPlayer.start()
        isPlayingNow.value = mediaPlayer.isPlaying
        myContext?.startForegroundService(intentTrigger)
        Log.d("prototype","no: ${playListInfo[mCurrent.value!!].no}")
        Log.d("prototype","mCurrent: ${mCurrent.value}")
   }              // 재생개시
    fun reset(){
        mediaPlayer.reset()
    }               // mediaPlayer 비워두기
    fun setTrack(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(myContext!!, playListInfo[mCurrent.value!!].getTrackUri())
        helper.roomPlayerDao().deleteAll()
        helper.roomPlayerDao().insert(RoomPlayer(mCurrent.value!!, isLoopingNow.value, isShuffled.value))
        mediaPlayer.prepare()
    }            // 현재 위치의 곡 담기
    fun mPrev(){
        if(mCurrent.value == 0){
            mCurrent.value = playlist.size-1
        } else{
            mCurrent.value = mCurrent.value!! - 1
        }
    }               // 이전곡 위치로 이동
    fun mNext(){
        if(mCurrent.value == playlist.size-1){
            mCurrent.value = 0
        } else {
            mCurrent.value = mCurrent.value!! + 1
        }
    }               // 다음곡 위치로 이동

}