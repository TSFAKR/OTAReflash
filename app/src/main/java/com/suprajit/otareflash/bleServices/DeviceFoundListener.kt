package com.suprajit.otareflash.bleServices

import android.bluetooth.le.ScanResult

interface DeviceFoundListener {
    fun onDeviceFound(result: ScanResult)
}