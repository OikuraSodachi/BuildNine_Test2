package com.todokanai.buildnine.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.todokanai.buildnine.R
import com.todokanai.buildnine.adapter.FragmentAdapter
import com.todokanai.buildnine.fragment.PlayingFragment
import com.todokanai.buildnine.fragment.TrackFragment
import com.todokanai.buildnine.service.ForegroundPlayService
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    lateinit var activityResult: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val trackPager = findViewById<ViewPager2>(R.id.track_pager)
        val exitBtn = findViewById<ImageButton>(R.id.Exitbtn)
        val settingsBtn = findViewById<ImageButton>(R.id.Settingsbtn)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        var rThread = Thread(WorkerRunnable())
        rThread.start()
        Thread {}.start()
        thread(start=true) {}

        trackPager.isUserInputEnabled = false
        activityResult =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startProcess()
                } else {
                    finish()
                }
            }
        activityResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        //---------Manifest에 정의된 권한 실행?

        val intentService = Intent(application, ForegroundPlayService::class.java)
        ContextCompat.startForegroundService(this,intentService)    //----- 서비스 개시

        fun exit(){
            finishAffinity()
            stopService(intentService)      // 서비스 종료
            System.runFinalization() // 현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
            exitProcess(0)     // 현재 액티비티를 종료시킨다.
        }           // 앱 종료
        exitBtn.setOnClickListener { exit()}      //----Exitbtn에 대한 동작

        val intentSetting = Intent(this, SettingsActivity::class.java)
        settingsBtn.setOnClickListener { startActivity(intentSetting)}     //Settingsbtn에 대한 동작

        val fragmentList = listOf(TrackFragment(), PlayingFragment())
        val adapter = FragmentAdapter(this)
        adapter.fragmentList = fragmentList
        trackPager.adapter = adapter
        val tabTitles = listOf("Music", "Playing")
        TabLayoutMediator(tabLayout, trackPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()    //---------탭 넘기기 관련 코드

    }


}
fun startProcess() {}
class WorkerRunnable : Runnable {
    override fun run() {}
}