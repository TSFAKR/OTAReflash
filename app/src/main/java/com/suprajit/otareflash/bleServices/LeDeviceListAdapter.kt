package com.suprajit.otareflash.bleServices

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.suprajit.otareflash.BleDeviceDataClass
import com.suprajit.otareflash.ItemListClickListener
import com.suprajit.otareflash.R
import com.suprajit.otareflash.activity.DeviceListActivity

class LeDeviceListAdapter(
    private val result: MutableList<MutableList<BleDeviceDataClass>>,
    private val device: BluetoothDevice,
    private val activity: DeviceListActivity,
    private val context: Context,
    private val oncItemListClickListener: ItemListClickListener
) : RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return result.size

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activity.requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val list = result[position]
            holder.tvDeviceName.text = list[0].deviceName
                holder.tvDeviceMacAddress.text = list[0].deviceAddress
            holder.clDeviceList.setOnClickListener {
                oncItemListClickListener.onItemClickListener(list[0].deviceName, device)
            }



        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val tvDeviceMacAddress: TextView = itemView.findViewById(R.id.tvDeviceMacAddress)
        val clDeviceList: ConstraintLayout = itemView.findViewById(R.id.clDeviceList)
    }


}
