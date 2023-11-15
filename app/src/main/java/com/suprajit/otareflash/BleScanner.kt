package com.suprajit.otareflash

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BleScanner(val activity: Activity, val handler: Handler) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanCallback = object : ScanCallback() {

        @RequiresApi(Build.VERSION_CODES.S)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Se encontró un nuevo dispositivo BLE
            val device: BluetoothDevice = result.device
            val rssi: Int = result.rssi

            // Dispositivo HM-10 encontrado
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
              ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_PERMISSION_CODE)
            }
           else{
               if (device.name != null){
                   Log.d(TAG, "${device.name}: ${device.address}")
               }


            }

        }
    }

    fun startScan() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_CODE)
            return
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        // Escanear dispositivos BLE por 10 segundos
        handler.postDelayed({
            stopScan()
        }, 5000)

        bluetoothAdapter?.bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
        Log.d(TAG, "starting device scan BLE...")
    }

    fun stopScan() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_CODE)
            return
        }
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
    }


    companion object {
        const val REQUEST_PERMISSION_CODE = 123
        const val TAG = "BleScanner"
    }
}