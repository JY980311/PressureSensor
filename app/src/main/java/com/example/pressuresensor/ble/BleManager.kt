package com.example.pressuresensor.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.pressuresensor.data.BleInterface
import com.example.pressuresensor.data.DeviceInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    private val context: Context
) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var scanList: SnapshotStateList<DeviceInfo>? = null
    private var connectedStateObserver: BleInterface? = null
    var bleGatt: BluetoothGatt? = null

    private val scanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d("onScanResult", result.toString())
            if (result.device.name != null) {
                var uuid = "null"

                if (result.scanRecord?.serviceUuids != null) {
                    uuid = result.scanRecord!!.serviceUuids.toString()
                }

                val scanItem = DeviceInfo(
                    result.device.name ?: "null",
                    uuid,
                    result.device.address ?: "null"
                )

                if (!scanList!!.contains(scanItem)) {
                    scanList!!.add(scanItem)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            println("onScanFailed $errorCode")
        }
    }

    private val gettCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState) // 원래 부모 메소드를 그대로 사용

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BleManager", "연결 성공")
                gatt?.discoverServices()
                connectedStateObserver?.onConnectedStateObserve(
                    true,
                    "onConnectionStateChange: STATE_CONNECTED"
                            + "\n"
                            + "---"
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BleManager", "연결 해제")
                connectedStateObserver?.onConnectedStateObserve(
                    false,
                    "onConnectionStateChange: STATE_DISCONNECTED"
                            + "\n"
                            + "---"
                )
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                MainScope().launch {
                    bleGatt = gatt
                    Toast.makeText(context, " ${gatt?.device?.name} 연결 성공", Toast.LENGTH_SHORT)
                        .show()
                    var sendText =
                        "onServicesDiscovered:  GATT_SUCCESS" + "\n" + "                         ↓" + "\n"

                    for (service in gatt?.services!!) {
                        sendText += "- " + service.uuid.toString() + "\n"
                        for (characteristics in service.characteristics) {
                            sendText += "       " + characteristics.uuid.toString() + "\n"
                        }
                    }
                    sendText += "---"
                    connectedStateObserver?.onConnectedStateObserve(
                        true,
                        sendText
                    )
                }.cancel()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startBleScan() {
        scanList?.clear()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun startBleConnectGatt(deviceInfo: DeviceInfo) {
        bluetoothAdapter
            .getRemoteDevice(deviceInfo.address)
            .connectGatt(context, false, gettCallback)
    }

    fun setScanList(pScanList: SnapshotStateList<DeviceInfo>) {
        scanList = pScanList
    }

    fun onConnectedStateObserve(pConnectedStateObserver: BleInterface) {
        connectedStateObserver = pConnectedStateObserver
    }
}