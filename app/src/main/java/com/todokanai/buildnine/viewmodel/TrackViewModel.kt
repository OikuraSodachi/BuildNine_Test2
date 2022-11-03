package com.todokanai.buildnine.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TrackViewModel(application: Application) : AndroidViewModel(application)  {

    class Factory(val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackViewModel(application) as T
        }
    }
}