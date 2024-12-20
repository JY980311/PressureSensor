package com.example.pressuresensor.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.pressuresensor.R
import com.example.pressuresensor.ble.BleManager
import com.example.pressuresensor.data.DeviceInfo

@Composable
fun ScanScreen(navController: NavHostController, bleManager: BleManager) {
    val scanList = remember { mutableStateListOf<DeviceInfo>() }
    val isScanning = remember { mutableStateOf(false) }
    val context = LocalContext.current
    bleManager.setScanList(scanList)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScanButton(context,bleManager, isScanning)
        ScanList(navController, bleManager, scanList)
    }
}

@Composable
@SuppressLint("MissingPermission")
fun ScanButton(
    context: Context,
    bleManager: BleManager,
    isScanning: MutableState<Boolean>
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // todo : 권한 결과 처리
    }

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 5.dp),
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFF1D8821)),
        onClick = {
            if(!isScanning.value) {
                if (checkPermission(context)) {
                    bleManager.startBleScan()
                } else {
                    launcher.launch(permissionArray)
                }
            } else {
                bleManager.stopBleScan()
            }
            isScanning.value =!isScanning.value
        }
    ) {
        Text(
            text = if (!isScanning.value) {
                "scan"
            } else {
                "stop"
            }
        )
    }
}

@Composable
fun ScanList(
    navController: NavHostController,
    bleManager: BleManager,
    scanList: SnapshotStateList<DeviceInfo>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        items(scanList) { topic->
            ScanItem(navController, bleManager, topic)
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanItem(
    navController: NavHostController,
    bleManager: BleManager,
    deviceData: DeviceInfo,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF569097)
        ),
        modifier = Modifier.padding(vertical = 4.dp),
        onClick = {
            bleManager.stopBleScan()
            navController.currentBackStackEntry?.savedStateHandle?.set(key = "deviceData", value = deviceData)
            navController.navigate("ConnectScreen")

        }
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
            .padding(start = 2.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = deviceData.name)

                if(expanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "UUID\n>> ${deviceData.uuid}")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Address\n>> ${deviceData.address}")
                }
            }

            IconButton(onClick = { expanded=!expanded }) {
                Icon(
                    imageVector = if(expanded) Icons.Filled.ArrowDropDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = if(expanded){
                        "show_less"
                    } else {
                        "show_more"
                    })
            }
        }
    }
}

fun checkPermission(context: Context): Boolean {
    val permissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    if(Build.VERSION.SDK_INT >= 31){
        // 블루투스와 카메라 권한이 허용되었는지 체크
        return permissionArray.all{ ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED}
    }
    return true
}

private val permissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}