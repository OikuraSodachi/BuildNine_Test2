package com.todokanai.buildnine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.tool.TrackTool

class TrackBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("tester", "Receiver Reached")

        if(intent.action == ForegroundPlayService.ACTION_REPLAY) {
            TrackTool(context).replay()

        }else if(intent.action == ForegroundPlayService.ACTION_SKIP_TO_PREVIOUS) {
            TrackTool(context).prev()

        }else if(intent.action == ForegroundPlayService.ACTION_PAUSE_PLAY) {
            TrackTool(context).pauseplay()

        }else if(intent.action == ForegroundPlayService.ACTION_SKIP_TO_NEXT) {
            TrackTool(context).next()

        }else if(intent.action == ForegroundPlayService.ACTION_CLOSE) {
            TrackTool(context).close()

        }
    }
}