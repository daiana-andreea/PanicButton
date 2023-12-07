package com.example.secondtry_kotlin

import android.app.ActivityManager
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.secondtry_kotlin.SelectDeviceActivity.Companion.EXTRA_ADDRESS
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class ControlActivity : AppCompatActivity() {

    var RED_ON = byteArrayOf(
        0xe6.toByte(),0x90.toByte(), 0x07.toByte(), 0x52, 0xef.toByte(), 0xed.toByte(), 0x97.toByte(), 0xf7.toByte()    )
    var RED_OFF = byteArrayOf(
        0x13, 0x23, 0x8c.toByte(), 0x71, 0x02, 0x72, 0xa5.toByte(),
        0xd8.toByte()
    );
    val BLUE_OFF= byteArrayOf(
        0x89.toByte(),0x85.toByte(), 0xaf.toByte(), 0x78, 0x60.toByte(), 0xb0.toByte(), 0xb6.toByte(), 0xb7.toByte()    )
    val BLUE_ON=byteArrayOf(
        0x82.toByte(),0x00.toByte(), 0x31.toByte(), 0xeb.toByte(), 0x48.toByte(), 0xa9.toByte(), 0xda.toByte(), 0x53.toByte()    )
    var Message_Received = false

    companion object {
        lateinit var m_myUUID: UUID
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        var m_address: String? = null
        var Message_Received: Boolean = false


        var staticPlainText = ByteArray(8)
        var staticKey = byteArrayOf(
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte(),
            0xFF.toByte()
        )
        var testPlain = byteArrayOf(
            0x13, 0x23, 0x8c.toByte(), 0x71, 0x02, 0x72, 0xa5.toByte(),
            0xd8.toByte()
        );
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        //try {
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)!!
        //}catch (e: KotlinNullPointerException)
        // {
        //Log.i("Test Device", "No device connected")
        //startActivity(Intent(this,MainActivity::class.java ))
        //finish()
        // }

        val turnLedOnButton = findViewById<Button>(R.id.TurnLedOnButton)
        val turnLedOffButton = findViewById<Button>(R.id.TurnLedOffButton)
        val turnBlueLedOnButton = findViewById<Button>(R.id.TurnBlueLedOnButton)
        val turnBlueLedOffButton = findViewById<Button>(R.id.TurnBlueLedOffButton)
        val disconnectButton = findViewById<Button>(R.id.DisconnectButton)
        val homeButton = findViewById<Button>(R.id.HomeButton)

        val csdButton = findViewById<Button>(R.id.CsdButton)
        val csdTextView = findViewById<TextView>(R.id.CsdTextView)


        staticPlainText = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)


        Thread(Runnable {
            Looper.prepare()
            try {
                ConnectToDevice(this).execute()
            } catch (e: IllegalStateException) {
                e.printStackTrace()

            }
            //if(!m_isConnected&& ConnectToDevice(this).status.equals(finish()))
            //  startActivity(Intent(this,SelectDeviceActivity::class.java))
            Looper.loop()
        }).start()


        Thread(Runnable
        {
            Looper.prepare()

            try {
                readBlueToothData(m_bluetoothSocket!!)

            } catch (e: KotlinNullPointerException) {
                Log.i("Received", "Couldn't receive")
            }

        }).start()
        Thread(Runnable {
/*
            try {
                readBlueToothData(m_bluetoothSocket!!)
            }catch (e: KotlinNullPointerException)
            {
                Log.i("Received", "Couldn't receive")
            }
*/
            turnLedOnButton.setOnClickListener {
                Log.i("butoane BT", "ON $RED_ON")
                sendCommandHexa(RED_ON)
            }



            turnLedOffButton.setOnClickListener {
                Log.i("butoane BT", "OFF $RED_OFF")
                sendCommandHexa(RED_OFF)
            }

            turnBlueLedOnButton.setOnClickListener {
                Log.i("butoane BT", "Blue_ON $BLUE_ON")
                sendCommandHexa(BLUE_ON)
            }
            turnBlueLedOffButton.setOnClickListener {
                Log.i("butoane BT", "Blue_OFF $BLUE_OFF")
                sendCommandHexa(BLUE_OFF)
            }

            disconnectButton.setOnClickListener {
                disconnect()
            }

            homeButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(EXTRA_ADDRESS, m_address)
                startActivity(intent)
            }

            csdButton.setOnClickListener {
                //Log.i("Encrypt1", csdTextView.text.toString())
                // Log.i("Encrypt3",csdTextView.text.toString().toByteArray().toString())
                //staticPlainText=csdTextView.text.toString().toByteArray()
                //Log.i("Encrypt2",staticPlainText.toString())
                try {
                    presentEncrypt.Encrypt(staticPlainText, staticKey)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()

                }
                Log.i("Encrypt4", staticPlainText.toString())

                sendCommandHexa(staticPlainText);
            }


        }).start()
        //if(!m_isConnected&& ConnectToDevice(this).status.equals(finish()))
        if (!m_isConnected)
            startActivity(Intent(this, SelectDeviceActivity::class.java))
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun WriteExternalFile() {
        var sdcard = Environment.getExternalStorageDirectory().absolutePath
        var dir = File(sdcard + "/licenta")
        if (dir.exists()) {
            val file = File(dir.absolutePath + "/" + "licenta.txt")
            val data = "1 "
            val fileOutputStream: FileOutputStream
            try {

                fileOutputStream = FileOutputStream(file, true)
                fileOutputStream.write(data.toByteArray())
                fileOutputStream.close()

                Log.i("WriteExternalFile", "Written to file licenta.txt")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()

    }

    private fun readBlueToothData(bluetoothSocket: BluetoothSocket) {
        val bluetoothSocketInputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int
        //Loop to listen for received bluetooth messages
        while (true) {
            Message_Received = false
            try {
                bytes = bluetoothSocketInputStream.read(buffer)
                val readMessage = String(buffer, 0, bytes)
                Log.i("Received", readMessage)
                if (readMessage == "1") {
                    Message_Received = true

                    Log.i("Received", "S-a primit mesajul")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.i("Received", "Inside function null")
                break
            }
            if (Message_Received)
                WriteExternalFile()
        }
    }

    private fun sendCommandHexa(staticPlainText: ByteArray) {
        if (m_bluetoothSocket != null)
            try {
                m_bluetoothSocket!!.outputStream.write(staticPlainText)
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null)
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
                MainActivity.IS_CONNECTED = false

            } catch (e: IOException) {
                e.printStackTrace()

            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {

        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_myUUID = device.uuids[0].uuid

                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                    MainActivity.IS_CONNECTED = true
                }
            } catch (e: IOException) {
                MainActivity.IS_CONNECTED = false
                connectSuccess = false
                e.printStackTrace()

            }
            return null
        }


        override fun onPreExecute() {
            super.onPreExecute()

            m_progress = ProgressDialog.show(context, "Connecting...", "Please wait")
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {

                Log.i("data", "Couldn't connect")

            } else {
                Log.i("data", "connected successfully")
                m_isConnected = true

            }
            if (m_progress.isShowing && m_progress != null) {
                m_progress.dismiss()
                //m_progress=null
            }

        }

    }
}
