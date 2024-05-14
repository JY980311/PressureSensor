package com.example.pressuresensor.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class DeviceInfo(
    val name: String,
    val uuid: String,
    val address: String,
): Parcelable