package com.todokanai.buildnine.viewmodel

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todokanai.buildnine.R
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isLoopingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isPlayingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isShuffled
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.mCurrent
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.playListInfo

class PlayingViewModel(application: Application) : AndroidViewModel(application) {

    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayingViewModel(application) as T
        }
    }

    var mediaPlayer = ForegroundPlayService.mediaPlayer

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

    fun setShuffleImage(): Int {
        if(isShuffled.value == true) {
            return R.drawable.ic_baseline_shuffle_24
        } else {
            return R.drawable.ic_baseline_arrow_right_alt_24
        }
    }

    fun getTotalTime():String{
        if(playListInfo.isEmpty()){
            return "0"
        }else {
            return SimpleDateFormat("mm:ss").format(playListInfo[mCurrent.value!!].duration)
        }
    }
    fun getSeekbarMax():Int{
        return mediaPlayer.duration
    }

    fun getCurrentProgress():String{
        return SimpleDateFormat("mm:ss").format(ForegroundPlayService.mediaPlayer.currentPosition)
    }
    fun getCurrentPosition():Int{
        return mediaPlayer.currentPosition
    }

    fun getTitle():String?{
        if(playListInfo.isEmpty()){
            return "Nothing Found"
        }else {
            return playListInfo[mCurrent.value!!].title
        }
    }
    fun getAlbumArt(): Uri? {
        if(playListInfo.isEmpty()){
            return null
        }else {
            return playListInfo[mCurrent.value!!].getAlbumUri()
        }
    }
    fun getArtistName():String?{
        if(playListInfo.isEmpty()){
            return "0"
        }else {
            return playListInfo[mCurrent.value!!].artist
        }
    }
}