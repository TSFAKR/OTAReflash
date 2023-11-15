package com.suprajit.otareflash.bleServices

interface GattConnectionListener {
    fun gattConnected()
    fun gattDisconnected()
}