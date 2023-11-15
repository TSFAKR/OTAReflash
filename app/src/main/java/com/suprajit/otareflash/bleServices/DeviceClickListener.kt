package com.suprajit.otareflash.bleServices

import android.bluetooth.BluetoothDevice

interface DeviceClickListener {
    fun onDeviceClick(bleDevice: BluetoothDevice)
}