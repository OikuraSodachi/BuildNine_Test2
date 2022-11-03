package com.todokanai.buildnine.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_player")
data class RoomPlayer (
    @ColumnInfo val mCurrent: Int?,
    @ColumnInfo val isLooping: Boolean?,
    @ColumnInfo val isShuffled: Boolean?
    ) {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo
    var no: Long? = null
    override fun toString(): String {
        return "RoomPlayer(mCurrent=$mCurrent, isLooping=$isLooping, isShuffled=$isShuffled, no=$no)"
    }


}

