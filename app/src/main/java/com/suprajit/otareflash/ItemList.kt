package com.suprajit.otareflash

import android.bluetooth.BluetoothDevice

interface ItemListClickListener {
    fun onItemClickListener(deviceName: String, device: BluetoothDevice)
}

