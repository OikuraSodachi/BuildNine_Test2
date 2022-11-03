package com.todokanai.buildnine.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.todokanai.buildnine.R
import com.todokanai.buildnine.databinding.ActivitySettingsBinding
import com.todokanai.buildnine.room.RoomHelper
import com.todokanai.buildnine.room.RoomPath
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.playListInfo
import com.todokanai.buildnine.tool.TrackTool
import com.todokanai.buildnine.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    lateinit var helper: RoomHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_settings)
        viewModel = ViewModelProvider(this@SettingsActivity)[SettingsViewModel::class.java]

        val backBtn = findViewById<ImageButton>(R.id.Backbtn)
        val pathBtn = findViewById<Button>(R.id.scanFolderButton)
        val scanBtn = findViewById<Button>(R.id.Scanbtn)

        val intentmain = Intent(this,MainActivity::class.java)
        backBtn.setOnClickListener {startActivity(intentmain);Log.d("tested111","back")} //Backbtn에 대한 동작
        scanBtn.setOnClickListener{scan()}          // scan버튼 난타하면 리스트 사이즈 증가 issue. 작업 완료시까지 버튼 disable로 해결?
        pathBtn.setOnClickListener { setPath(binding.mPathInput.text) }
    }

    fun scan() {
        TrackTool(this).reset()      // 혹시 몰라서 정지명령 내려둠
        val scannedList = TrackTool(applicationContext).scanTrackList()
        lifecycleScope.launch(Dispatchers.IO) {
            helper = RoomHelper.getInstance(applicationContext)!!

            val size = scannedList.size
            helper.roomTrackDao().deleteAll()                       // 목록비우기
            for (a in 1..size) {
                helper.roomTrackDao().insert(scannedList[a - 1])
            }                               // 스캔된 목록
            playListInfo = TrackTool(applicationContext).playList
            Log.d("tested111", "Scan")
        }
    }           // 음원파일 저장 함수
    
    fun setPath(path:Editable?){
        lifecycleScope.launch(Dispatchers.IO) {
            helper = RoomHelper.getInstance(applicationContext)!!

            helper.roomPathDao().insert(RoomPath(path.toString()))
        }
    }
}