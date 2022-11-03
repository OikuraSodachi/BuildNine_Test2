package com.todokanai.buildnine.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RoomTrack::class,RoomPlayer::class,RoomPath::class], version = 1, exportSchema = false)    // arrayOf(테이블(행렬)의 갯수), 버전
abstract class RoomHelper : RoomDatabase(){     // 각 테이블마다 Dao를 부르는 중간과정역할

    abstract fun roomTrackDao():RoomTrackDao

    abstract fun roomPlayerDao():RoomPlayerDao

    abstract fun roomPathDao():RoomPathDao

    companion object {
        private var instance: RoomHelper? = null

        @Synchronized
        fun getInstance(context: Context): RoomHelper? {
            if (instance == null) {
                synchronized(RoomHelper::class){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        RoomHelper::class.java,
                        "room_db",
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}