package com.example.pressuresensor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pressuresensor.Screen.ConnectScreen
import com.example.pressuresensor.Screen.ScanScreen
import com.example.pressuresensor.ble.BleManager
import com.example.pressuresensor.ui.theme.PressureSensorTheme

class MainActivity : ComponentActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PressureSensorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val bleManager = BleManager(applicationContext)
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "ScanScreen"){
                        composable(route = "ScanScreen") {ScanScreen(navController = navController, bleManager = bleManager)}
                        composable(route = "ConnectScreen") {ConnectScreen(navController = navController, bleManager = bleManager)}
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 31) {
            if (permissionArray.all {
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "권한 확인", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissionLauncher.launch(permissionArray)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("DEBUG", "${it.key} = ${it.value}")
        }
    }
}

