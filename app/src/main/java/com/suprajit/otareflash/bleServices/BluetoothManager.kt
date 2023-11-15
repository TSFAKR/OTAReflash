package com.suprajit.otareflash.bleServices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.suprajit.otareflash.utils.Constants
import java.util.UUID


class BluetoothManager : BluetoothGattCallback() {
    private lateinit var context: Context
    var deviceFoundListener: DeviceFoundListener? = null
    var gattConnectionListener: GattConnectionListener? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothScanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null
    private var isScanning = false
    private var notificationCharacteristicsMap: HashMap<String, BluetoothGattCharacteristic> =
        HashMap()

    fun getContext(application: Context){
        this.context = application
    }
    fun initiateManager(context: Context) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    fun isBleOn(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun startScan() {
        if (bluetoothScanner == null)
            bluetoothScanner = bluetoothAdapter?.bluetoothLeScanner
        setCallBacks()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothScanner?.startScan(scanCallback)
            isScanning = true
        }

    }

    fun stopScan() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isScanning)
                bluetoothScanner?.stopScan(scanCallback)
        }

        isScanning = false
    }

    fun connectDevice(bleDevice: BluetoothDevice, context: Context, autoConnect: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt = bleDevice.connectGatt(context, autoConnect, this)
        }

    }

    fun disConnectDevice() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt?.disconnect()
            //bluetoothGatt?.close()
            bluetoothGatt = null
        }

    }

    //scanCallBacks
    private fun setCallBacks() {
        if (scanCallback == null)
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    if (result != null) {
                        deviceFoundListener?.onDeviceFound(result)
                    }
                }
                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    Log.e(TAG, "ScanFailed code: $errorCode")
                }
            }
    }

    //gattCallBacks
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (newState) {
                        BluetoothGatt.STATE_CONNECTED -> {
                            Handler(Looper.getMainLooper()).post {

                                gatt?.discoverServices()
                            }
                        }
                        BluetoothGatt.STATE_DISCONNECTED -> {
                            Handler(Looper.getMainLooper()).post {
                                gattConnectionListener?.gattDisconnected()
                            }
                            gatt?.close()
                        }
                        else -> {
                            Log.d(TAG, "BLE State : $newState")
                        }
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        gattConnectionListener?.gattDisconnected()
                    }
                    gatt?.close()
                    Log.e(TAG, "Gatt Status: $status")
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }

        }

    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                gatt?.requestMtu(120)
                gatt?.services?.forEach { services ->
                    Log.d(TAG, services.uuid.toString())
                    if (services?.uuid.toString() == Constants.NOTIFICATION_SERVICE_UUID) {
                        services.characteristics?.forEach { characteristics ->
                            Log.d(TAG, characteristics.uuid.toString())
                            notificationCharacteristicsMap[(characteristics.uuid ?: "").toString()] =
                                characteristics
                        }
                    }
                }
                Handler(Looper.getMainLooper()).post {
                    gattConnectionListener?.gattConnected()
                }
            }

        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            val sb = StringBuilder()
            if (characteristic?.value?.size == 1) {
                Log.d("WRITTEN DATA", characteristic.value?.get(0).toString())
            } else {
                for (byte in 0..45) {
                    val string: String = characteristic?.value?.get(byte).toString() + " "
                    sb.append(string)
                }
                Log.d("WRITTEN DATA", sb.toString())
            }
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
//        if (characteristic != null && characteristic == notificationCharacteristicsMap[Constants.RIDE_DETAILS_READ_UUID]) {
//            if (characteristic.value.size < 30) {
//                rideDetailsListener?.onReadRideDetailsData(characteristic.value, characteristic)
//                val sb = StringBuilder()
//                for (byte in characteristic.value.indices) {
//                    val string: String = characteristic.value?.get(byte).toString() + " "
//                    sb.append(string)
//                }
//                Log.d("READ DATA", sb.toString())
//            }
//        }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i("MTU SIZE", "SIZE$mtu")
        } else {
            Log.e("MTU SIZE", "REQUEST FAILED")
        }
    }

    fun setCharacteristicNotification(uuid: String, notifyOn: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (bluetoothAdapter == null || bluetoothGatt == null) {
                return
            }
            val characteristic = notificationCharacteristicsMap[uuid]
            if (characteristic != null) {

                bluetoothGatt?.setCharacteristicNotification(characteristic, notifyOn)
                val descriptor =
                    characteristic.getDescriptor(UUID.fromString(Constants.DESCRIPTOR_UUID))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
            }
        }


    }

    fun writeDataToCharacteristics(data: ByteArray, uuid: String) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return
        }
        val characteristic = notificationCharacteristicsMap[uuid]
        if (characteristic != null) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.value = data
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothGatt?.writeCharacteristic(characteristic)
        }

    }


    companion object {
        private var instance: BluetoothManager? = null

        fun getInstance(): BluetoothManager? {
            if (instance == null)
                instance = BluetoothManager()
            return instance
        }

        private val TAG: String = BluetoothManager::class.java.simpleName
    }
}