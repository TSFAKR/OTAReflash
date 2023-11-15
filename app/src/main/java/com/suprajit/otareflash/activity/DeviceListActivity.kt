package com.suprajit.otareflash.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.suprajit.otareflash.BleDeviceDataClass
import com.suprajit.otareflash.ItemListClickListener
import com.suprajit.otareflash.bleServices.GattConnectionListener
import com.suprajit.otareflash.bleServices.LeDeviceListAdapter
import com.suprajit.otareflash.databinding.ActivityDerviceListBinding
import com.suprajit.otareflash.utils.Constants
import com.suprajit.otareflash.utils.Constants.TAG
import java.util.UUID


class DeviceListActivity : AppCompatActivity() {
    lateinit var binding: ActivityDerviceListBinding
    val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
    val bleDeviceDataClass = mutableListOf<MutableList<BleDeviceDataClass>>()
    private var _socket: BluetoothSocket? = null
    private var mGatt: BluetoothGatt? = null
    private var notificationCharacteristicsMap: HashMap<String, BluetoothGattCharacteristic> =
        HashMap()
    var gattConnectionListener: GattConnectionListener? = null


    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 5000

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDerviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scanLeDevice()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bluetoothLeScanner.startScan(leScanCallback)

        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }


    }

    val onItemListClickListener = object : ItemListClickListener {
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onItemClickListener(deviceName: String, device: BluetoothDevice) {
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceListActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                connectToDevice(device)


            } else {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            }
        }

    }


    /***After receive the Location Permission, the Application need to initialize the
     * BLE Module and BLE Service*/

    @RequiresApi(Build.VERSION_CODES.S)
    private fun scanLeDevice() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            if (!scanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false

                    bluetoothLeScanner.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }
        }

    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)


            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN))
            } else {
                if (result.device.name != null) {
                    bleDeviceDataClass.add(
                        mutableListOf(
                            BleDeviceDataClass(
                                result.device.name,
                                result.device.address
                            )
                        )
                    )
                    val leDeviceListAdapter =
                        LeDeviceListAdapter(
                            bleDeviceDataClass,
                            result.device,
                            this@DeviceListActivity,
                            applicationContext,
                            onItemListClickListener
                        )
                    binding.recyclerview.apply {
                        layoutManager = LinearLayoutManager(this@DeviceListActivity)
                        adapter = leDeviceListAdapter
                    }
                    runOnUiThread {
                        leDeviceListAdapter.notifyDataSetChanged()
                    }
                }

            }


        }

        @SuppressLint("NotifyDataSetChanged")
        @RequiresApi(Build.VERSION_CODES.S)
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.d(TAG, "results_batch_scan:")
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "results_batch_scan: ${results?.get(0)?.device?.name.toString()}")

            } else {

                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )

            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d(TAG, "Scan Failed: $errorCode ")
        }
    }
    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
        }

    val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i(TAG, "onConnectionStateChange Status: $status")
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceListActivity,
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
            val services = gatt!!.services
            Log.i(TAG, "onServicesDiscovered  $services")
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceListActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    gatt.requestMtu(120)
                    gatt.services?.forEach { services ->
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
               // gatt.readCharacteristic(services[1].characteristics[0])
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.i(TAG, "onCharacteristicRead $characteristic")
            if (ActivityCompat.checkSelfPermission(
                    this@DeviceListActivity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                gatt.disconnect()
            }
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
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("MTU SIZE", "SIZE$mtu")
            } else {
                Log.e("MTU SIZE", "REQUEST FAILED")
            }
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (mGatt == null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this@DeviceListActivity, "Connecting...", Toast.LENGTH_LONG).show()
                Log.w(TAG, "Connecting.....")
                mGatt = device.connectGatt(this, false, gattCallback)
                scanning = false // will stop after first device detection
            }

        }
    }
    fun disConnectDevice() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mGatt?.disconnect()
            //bluetoothGatt?.close()
            mGatt = null
        }

    }
    fun setCharacteristicNotification(uuid: String, notifyOn: Boolean) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (mBluetoothAdapter == null || mGatt == null) {
                return
            }
            val characteristic = notificationCharacteristicsMap[uuid]
            if (characteristic != null) {

                mGatt?.setCharacteristicNotification(characteristic, notifyOn)
                val descriptor =
                    characteristic.getDescriptor(UUID.fromString(Constants.DESCRIPTOR_UUID))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                mGatt?.writeDescriptor(descriptor)
            }
        }


    }

    fun writeDataToCharacteristics(data: ByteArray, uuid: String) {
        if (mBluetoothAdapter == null || mGatt == null) {
            return
        }
        val characteristic = notificationCharacteristicsMap[uuid]
        if (characteristic != null) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            characteristic.value = data
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mGatt?.writeCharacteristic(characteristic)
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        if (mGatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(
                this@DeviceListActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mGatt?.close()
            mGatt = null
        }
    }
/*
    fun writeDataToCharacteristic(
        ctx: Context,
        navigationByte: Int
    ) {
        val bleState = BLE_STATE_CONNECTED
        val batteryStatus = getBatteryState(ctx)
        var callerData = ""
        var senderData = ""

        val dataArray = ByteArray(46)
        dataArray[0] = bleState.toByte()
        dataArray[1] = batteryStatus.toByte()
        dataArray[2] = receptionLevel.toByte()
        dataArray[3] = callType.toByte()
        if (callerData.isNotEmpty()) {
            if (callerData.length <= 18) {
                for (byte in 4..callerData.length + 3) {
                    dataArray[byte] = callerData[byte - 4].code.toByte()
                }
            } else {
                for (byte in 4..21) {
                    dataArray[byte] = callerData[byte - 4].code.toByte()
                }
            }
        }
        dataArray[22] = missedCallCount.toByte()
        dataArray[23] = msgType.toByte()
        if (senderData.isNotEmpty()) {
            if (senderData.length <= 18) {
                for (byte in 24..senderData.length + 23) {
                    dataArray[byte] = senderData[byte - 24].code.toByte()
                }
            } else {
                for (byte in 24..41) {
                    dataArray[byte] = senderData[byte - 24].code.toByte()
                }
            }
        }
        dataArray[42] = unreadMsgCount.toByte()
        dataArray[43] = getCurrentHour().toInt().toByte()
        dataArray[44] = getCurrentMinute().toInt().toByte()
        dataArray[45] = navigationByte.toByte()
        bluetoothManager?.writeDataToCharacteristics(dataArray, Constants.NOTIFICATION_WRITE_UUID)
        val sb = StringBuilder()
        for (byte in 0..45) {
            val string: String = dataArray[byte].toString() + " "
            sb.append(string)
        }
        Log.d("DATA ARRAY", sb.toString())
    }
*/
}





