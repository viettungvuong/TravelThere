package com.tung.travelthere.objects

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.type.DateTime
import java.sql.Date
import java.sql.Time
import java.util.LinkedList

class Checkpoint(private val placeLocation: PlaceLocation) {
    fun distanceTo(other: Checkpoint): Float {
        return placeLocation.distanceTo(other.placeLocation)
    }

    fun getLocation(): PlaceLocation {
        return placeLocation
    }

    override fun toString(): String {
        return placeLocation.getPos().toString()
    }
}

class Schedule() {
    private val checkpointList = mutableStateListOf<Checkpoint?>()
    val distances = mutableStateListOf<Float>()

    constructor(other: Schedule): this(){
        this.checkpointList.addAll(other.checkpointList)
        this.distances.addAll(distances)
    } //copy constructor

    fun getList(): SnapshotStateList<Checkpoint?> {
        return checkpointList
    }

    fun addNullCheckpoint(){
        checkpointList.add(null)
        distances.add(0f)
    }

    //set thì không null
    fun setCheckpoint(checkpoint: Checkpoint, index: Int) {
        checkpointList[index]=checkpoint
        calculateDistance(checkpointList.lastIndex-1)
    }

    fun removeCheckpointAt(index: Int) {
        checkpointList.removeAt(index)
        distances.removeAt(index)
    }

    fun clear(){
        checkpointList.clear()
        distances.clear()
    }

    //tính khoảng cách giữa các checkpoint
    fun calculateDistance(index: Int) {
        if (index >= 0 && index < checkpointList.lastIndex) {
            if (checkpointList[index] != null && checkpointList[index + 1] != null) {
                distances[index] = checkpointList[index]!!.distanceTo(checkpointList[index + 1]!!)
            }
            else {
                distances[index] = 0f
            }
        } else if (index == checkpointList.lastIndex) {
            distances[index] = 0f
        } else {
            return
        }
        distances[index]=distances[index]/1000
    }
}