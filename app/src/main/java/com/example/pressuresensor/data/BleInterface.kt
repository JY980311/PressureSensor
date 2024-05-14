package com.example.pressuresensor.data

import java.sql.Connection

interface BleInterface {
    fun onConnectedStateObserve(isConnected: Boolean, data: String)
}