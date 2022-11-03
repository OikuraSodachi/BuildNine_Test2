package com.todokanai.buildnine.fragment

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.todokanai.buildnine.R
import com.todokanai.buildnine.adapter.TrackRecyclerAdapter
import com.todokanai.buildnine.databinding.FragmentTrackBinding
import com.todokanai.buildnine.room.RoomHelper
import com.todokanai.buildnine.service.ForegroundPlayService
import com.todokanai.buildnine.tool.TrackTool
import com.todokanai.buildnine.viewmodel.TrackViewModel

class TrackFragment : Fragment() {

    companion object {
        fun newInstance() = TrackFragment()
    }
    lateinit var helper : RoomHelper

    lateinit var binding: FragmentTrackBinding
    private lateinit var viewModel: TrackViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("trackfragment","onCreateView")
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track, container, false)
        viewModel = ViewModelProvider(this)[TrackViewModel::class.java]



        val adapter = TrackRecyclerAdapter()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track, container, false)
        binding.trackRecyclerView.adapter = adapter
        binding.trackRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.swipe.setOnRefreshListener {
            ForegroundPlayService.playListInfo = TrackTool(activity).playList
            adapter.notifyDataSetChanged()
            binding.swipe.isRefreshing = false          //------swipe 해서 목록 새로고침
        }

        val ct : Context = requireContext()
        helper = Room.databaseBuilder(ct, RoomHelper::class.java,"room_db")
            .allowMainThreadQueries()
            .build()

        adapter.trackList.addAll(helper.roomTrackDao().getAll())
        Log.d("tested","loaded")


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("trackfragment","onViewCreated")

        viewModel = ViewModelProvider(this, TrackViewModel.Factory(Application()))[TrackViewModel::class.java]

    }

}

//   variableId????
// TrackFragment는 Observe 적용 안하는게 맞는듯?