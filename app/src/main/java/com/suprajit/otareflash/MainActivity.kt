package com.suprajit.otareflash


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.suprajit.otareflash.activity.DeviceListActivity
import com.suprajit.otareflash.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var granted: Boolean = false
    private lateinit var mBluetoothAdapter: BluetoothAdapter


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ActivityCompat.checkSelfPermission(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).toString()
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            granted = true
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter.isEnabled) {
            binding.btnConnect.text = "Turned On"
        } else {
            binding.btnConnect.text = "Turned Off"
        }
        binding.btnConnect.setOnClickListener {

            if (granted) {
                if (mBluetoothAdapter.isEnabled) {
                    mBluetoothAdapter.disable()
                    binding.btnConnect.text = "Turned On"

                    val intent = Intent(this, DeviceListActivity::class.java)
                    startActivity(intent)
                } else {
                    binding.btnConnect.text = "Turned Off"
                    bleTurnOffOn.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    Toast.makeText(applicationContext, "Bluetooth Turned ON", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.btnInterface.setOnClickListener {
            Toast.makeText(this, "Interfacing", Toast.LENGTH_SHORT).show()
        }
        binding.btnJumpToBootLoader.setOnClickListener {
            Toast.makeText(this, "Bootloader enabled", Toast.LENGTH_SHORT).show()
        }
        binding.btnSecurity.setOnClickListener {
            Toast.makeText(this, "Interfacing", Toast.LENGTH_SHORT).show()
        }
        binding.btnLoadConfigFile.setOnClickListener {
            Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show()
        }
        binding.btnLoadCHexFile.setOnClickListener {
            Toast.makeText(this, "File Uploaded", Toast.LENGTH_SHORT).show()
        }
        binding.btnEraseProgramVerify.setOnClickListener {
            Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show()
        }
        binding.btnECUReset.setOnClickListener {
            Toast.makeText(this, "Reset Completed", Toast.LENGTH_SHORT).show()
        }


    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                granted = true
                Log.d("TSF_APPS", "${it.key} = ${it.value}")
            }
        }

    private val bleTurnOffOn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                binding.btnConnect.text = "Turned On"
            }

        }

}



