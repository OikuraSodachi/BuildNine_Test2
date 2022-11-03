package com.todokanai.buildnine.fragment

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.todokanai.buildnine.R
import com.todokanai.buildnine.databinding.FragmentPlayingBinding
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isLoopingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isPlayingNow
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.isShuffled
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.mCurrent
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.mediaPlayer
import com.todokanai.buildnine.service.ForegroundPlayService.Companion.playListInfo
import com.todokanai.buildnine.tool.TrackTool
import com.todokanai.buildnine.viewmodel.PlayingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayingFragment : Fragment() {

    lateinit var binding: FragmentPlayingBinding
    private lateinit var viewModel: PlayingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("playingfragment","onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playing, container, false)
        viewModel = ViewModelProvider(this)[PlayingViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("playingfragment", "onViewCreated")

        viewModel = ViewModelProvider(this, PlayingViewModel.Factory(Application()))[PlayingViewModel::class.java]

        fun seekBarSet() {
            if(playListInfo.isNotEmpty()) {
                lifecycleScope.launch {
                    while (mediaPlayer.isPlaying) {
                        binding.seekBar.progress = viewModel.getCurrentPosition()
                        binding.songCurrentProgress.text =
                            SimpleDateFormat("mm:ss").format(binding.seekBar.progress)
                        delay(1)
                    }
                }

                binding.seekBar.max = viewModel.getSeekbarMax()
                val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            //sets the playing file progress to the same seekbar progressive, in relative scale
                            mediaPlayer.seekTo(progress)

                            //Also updates the textView because the coroutine only runs every 1 second
                            binding.songCurrentProgress.text = viewModel.getCurrentProgress()
                        } else {

                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                }
                binding.seekBar.setOnSeekBarChangeListener(seekBarListener)

                binding.songTotalTime.text = viewModel.getTotalTime()
            }
        }
        seekBarSet()

        binding.playPauseButton.setImageResource(TrackTool(activity).setPausePlayImage())
        binding.repeatButton.setOnClickListener { TrackTool(activity).replay();  Log.d("playingfragment","isLooping:${isLoopingNow.value}");         Log.d("playingfragment","isreallyLooping:${mediaPlayer.isLooping}")}
        binding.previousButton.setOnClickListener{ TrackTool(activity).prev() }
        binding.playPauseButton.setOnClickListener{ TrackTool(activity).pauseplay()}
        binding.nextButton.setOnClickListener { TrackTool(activity).next() }
        binding.shuffleButton.setOnClickListener { TrackTool(activity).shuffle() }

        isPlayingNow.observe(viewLifecycleOwner){
            Log.d("seekBar","isPlayingNow: $it")
            seekBarSet()
            binding.playPauseButton.setImageResource(viewModel.setPausePlayImage())
        }

        isLoopingNow.observe(viewLifecycleOwner){
            binding.repeatButton.setImageResource(viewModel.setLoopingImage())
        }

        isShuffled.observe(viewLifecycleOwner){
            binding.shuffleButton.setImageResource(viewModel.setShuffleImage())
        }

        mCurrent.observe(viewLifecycleOwner){
            binding.title.text = viewModel.getTitle()
            binding.title.isSelected=true
            binding.artist.text = viewModel.getArtistName()
            binding.playerImage.setImageURI(viewModel.getAlbumArt())
            seekBarSet()
        }
    }
    override fun onResume() {
        super.onResume()
        Log.d("playingfragment", "onResume")
        if (playListInfo.isNotEmpty()) {
            lifecycleScope.launch {
                while (mediaPlayer.isPlaying) {
                    binding.seekBar.progress = viewModel.getCurrentPosition()
                    binding.songCurrentProgress.text =
                        SimpleDateFormat("mm:ss").format(binding.seekBar.progress)
                    delay(1)
                }
            }

            binding.seekBar.max = viewModel.getSeekbarMax()
            val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        //sets the playing file progress to the same seekbar progressive, in relative scale
                        mediaPlayer.seekTo(progress)

                        //Also updates the textView because the coroutine only runs every 1 second
                        binding.songCurrentProgress.text = viewModel.getCurrentProgress()
                    } else {

                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }
            binding.seekBar.setOnSeekBarChangeListener(seekBarListener)

            binding.songTotalTime.text = viewModel.getTotalTime()   }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("playingfragment","onDetach")
    }

    override fun onPause() {
        super.onPause()
        Log.d("playingfragment","onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("playingfragment","onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("playingfragment","onDestroyView")
    }

    override fun onStart() {
        super.onStart()
        Log.d("playingfragment","onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("playingfragment","onStop")
    }
}