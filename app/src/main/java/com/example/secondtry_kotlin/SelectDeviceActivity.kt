package com.example.secondtry_kotlin

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import org.jetbrains.anko.toast
import java.util.ArrayList

class SelectDeviceActivity : AppCompatActivity() {

   private var m_BluetoothAdapter: BluetoothAdapter? =null
    private lateinit var m_pairedDevices:Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH=1

    companion object{
        val EXTRA_ADDRESS:String="Device_address"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        val homeButtonSelect=findViewById<Button>(R.id.HomeButtonSelect)

        homeButtonSelect.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        m_BluetoothAdapter= BluetoothAdapter.getDefaultAdapter()
        if(m_BluetoothAdapter==null) {
            showToast("This device doesn't support bluetooth!")
            return
        }
        if(!m_BluetoothAdapter!!.isEnabled)
        {
            val enableBluetoothIntent=Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent,REQUEST_ENABLE_BLUETOOTH)
        }


        val refreshButton=findViewById<Button>(R.id.RefreshButton)

        refreshButton.setOnClickListener {
            pairedDeviceList()
        }
    }

    private fun pairedDeviceList()
    {
          m_pairedDevices=m_BluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> =ArrayList()
        val device_names:ArrayList<String> = ArrayList()
        if(m_pairedDevices.isNotEmpty())
        {
            for(device: BluetoothDevice in m_pairedDevices)
            {
                device_names.add(device.name)
                list.add(device)
                Log.i("device",""+device+ " "+device.name)
            }
        }
        else
            showToast("No paired bluetooth devices found.")
        val adapter=ArrayAdapter(this, android.R.layout.simple_list_item_1,device_names)
        var selectDeviceList=findViewById<ListView>(R.id.SelectDeviceList)
        selectDeviceList.adapter=adapter
        selectDeviceList.onItemClickListener=AdapterView.OnItemClickListener{_,_,position,_->
        val device:BluetoothDevice=list[position]
        val address:String=device.address

            val intent=Intent(this,ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS,address)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_ENABLE_BLUETOOTH)
        {
            if(resultCode==Activity.RESULT_OK)
            {
                if(m_BluetoothAdapter!!.isEnabled)
                {
                    showToast("Bluetooth has been enabled.")
                }
                else
                    showToast("Bluetooth has been disabled.")
            }
            else if (resultCode==Activity.RESULT_CANCELED){
                showToast("Bluetooth enabling has been canceled.")
            }
        }
    }
    fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()

    }
}