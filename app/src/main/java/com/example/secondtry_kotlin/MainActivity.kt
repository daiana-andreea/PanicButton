package com.example.secondtry_kotlin

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    lateinit var m_address: String
    companion object{
        lateinit var m_address: String
        lateinit var locationManager: LocationManager
        lateinit var location: Location
        var Sendsms=false
        var IS_CONNECTED=false
        var EXIST=false

        lateinit var watchService:WatchService
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            m_address = intent.getStringExtra(ControlActivity.m_address)!!
        }catch (e: KotlinNullPointerException)
        {
            Log.i("Test Device", "No device connected")
            //startActivity(Intent(this,MainActivity::class.java ))
            //finish()
        }

        //permissions
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        if (checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.SEND_SMS), 1)
        if(checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)



            val currentDirectory =
                File(Environment.getExternalStorageDirectory().absolutePath + "/licenta")
            if(!currentDirectory.exists())
                currentDirectory.mkdir()
        //location
        try {
            locationManager =
                applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)!!
        }catch(e:SecurityException)
        {
            e.printStackTrace()
        }catch (e:KotlinNullPointerException)
        {
            e.printStackTrace()
        }
      //Thread(Runnable { startService(Intent(this, MyService::class.java))}).start()

        // conexiune home_layout-cod
        val SendSmsButton = findViewById<Button>(R.id.SendSmsButton)
        val resultTextView = findViewById<TextView>(R.id.ResultTextView)
        val setDetailsButton = findViewById<Button>(R.id.SetDetailsButton)
        val selectDeviceButton=findViewById<Button>(R.id.SelectDeviceButton)
        val testDeviceButton=findViewById<Button>(R.id.TestDeviceButton)
        val stopServiceButton=findViewById<Button>(R.id.StopServiceButton)
        val startServiceButton=findViewById<Button>(R.id.StartService)





        //schimba atentia spre set_details_layout

    Thread(Runnable {
           /* try{
              m_address = intent.getStringExtra(ControlActivity.m_address)!!                      //bucata pentru Test_Device
          }catch(e: KotlinNullPointerException)
          {
              e.printStackTrace()
              Log.i("main", "Nu a luat data din Control Activity")
          }
          try{
              m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)!!
          }catch(e: KotlinNullPointerException)
          {
              e.printStackTrace()
              Log.i("main", "Nu a luat data din SelectDeviceActivity")
          }*/



           setDetailsButton.setOnClickListener {
               startActivity(Intent(this, SetDetailsActivity::class.java))
           }
           selectDeviceButton.setOnClickListener {
               startActivity(Intent(this, SelectDeviceActivity::class.java))
           }
           stopServiceButton.setOnClickListener {
               if(isMyServiceRunning(BluetoothListener::class.java)) {
                   stopService(Intent(this, BluetoothListener::class.java))
                   //watchService.close()
                   exitProcess(1)

               }
               else
                   showToast("No service is running")
               finishAndRemoveTask()
           }

           startServiceButton.setOnClickListener {
               if(!isMyServiceRunning(BluetoothListener::class.java))                               //test if the Service was created before
               {
                   startService(Intent(this, BluetoothListener::class.java))
                   Thread(Runnable {
                       Looper.prepare()
                       FileObserverFunction(location!!)                        //E posibil sa fie pentru update location, but was not tested
                   }).start()
               }
                else
                    showToast("Service already running")
           }

           testDeviceButton.setOnClickListener{
               if(IS_CONNECTED) {
                   val intent=Intent(this,MainActivity::class.java)
                   intent.putExtra(SelectDeviceActivity.EXTRA_ADDRESS, ControlActivity.m_address)
                   startActivity(intent)
               }
              else
               {
                   Log.i("Test Device", "No device connected")
                   showToast("No device connected");
               }

           }

           SendSmsButton.setOnClickListener {
               var available = false
               var readable = false
               val PERMISSION_REQUEST_STORAGE = 1

               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                   if (location != null) {
                       SendSMS(
                           location.latitude.toString(),
                           location.longitude.toString()
                       )
                   }
               }
           }



       }).start()
   }

   fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
       Toast.makeText(this, text, duration).show()

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
   @RequiresApi(Build.VERSION_CODES.KITKAT)
   fun SendSMS(latitude: String, longitude: String) {
       try {
           val phoneNo = ReadFile("phone.txt")
           if (phoneNo == "") {
               showToast("Phone number has not been set up!")
           } else {
               try {
                   val SMS = ReadFile("messageContent.txt")+ "Latitude: " + latitude + "\n" + "Longitude: " + longitude +"\n"+ReadFile("saved.txt")
                   val smsManager = SmsManager.getDefault()
                   smsManager.sendTextMessage(phoneNo, null, SMS, null, null)
                   showToast("Message is sent")
               } catch (e: java.lang.Exception) {
                   e.printStackTrace()
                   showToast("Failed to send sms")
               } catch (e: FileNotFoundException) {
                   e.printStackTrace()
                   showToast("File not found")
               }
           }
       } catch (e: FileNotFoundException) {
           e.printStackTrace()
           showToast("Saved.txt not found")
       }

   }

   fun ReadFile(file: String): String? {
       var result: String? = null
       try {
           //val file = "saved.txt"//filename.text.toString()

           val fileInputStream: FileInputStream? = openFileInput(file)

           val inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
           val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)

           val stringBuilder: StringBuilder = StringBuilder()
           var text: String? = null
           while (run {
                   text = bufferedReader.readLine()
                   text
               } != null) {
               stringBuilder.append(text + "\n")
           }
           result = stringBuilder.toString()
       } catch (e: FileNotFoundException) {
           showToast("No data saved")
       }
       if (result != "")
           return result
       else
           return "Not a message"
   }

   @RequiresApi(Build.VERSION_CODES.O)
   fun FileObserverFunction(location: Location) {

       try {
           val currentDirectory = File(Environment.getExternalStorageDirectory().absolutePath + "/licenta")
           val watchService = FileSystems.getDefault().newWatchService()
           val pathToWatch = currentDirectory.toPath()
           var flag= false

           Log.println(Log.INFO, "fisier", "fisierul monitorizat e la adresa: $pathToWatch")

           val pathKey = pathToWatch.register(
               watchService, StandardWatchEventKinds.ENTRY_CREATE,
               StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE
           )

           while (true) {
               flag=false
               val watchKey = watchService.take()
               if (watchKey.pollEvents().isNotEmpty())
                  flag=true

               if(flag)
               {
                   SendSMS(location.latitude.toString(), location.longitude.toString())
                    Log.i("FileObserver","Was sent")
                   flag=false

               }
               if (!watchKey.reset()) {
                   watchKey.cancel()
                   watchService.close()
                   break
               }

           }

           pathKey.cancel()
       }catch (e: NullPointerException)
       {
           e.printStackTrace()
       }
   }


}
/*
class MyService : Service() {

   @RequiresApi(Build.VERSION_CODES.O)
   override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


       val channelId =
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           createNotificationChannel("my_service", "My Background Service")
       } else {
           // If earlier version channel ID is not used
           ""
       }

       val notification: Notification = Notification.Builder(this, channelId)
               .setContentTitle("title")
               .setContentText("text")
               .build()
       startForeground(2001, notification)
       FileObserverFunction()
       return START_STICKY
   }


   override fun onBind(intent: Intent?): IBinder? {
       TODO("Not yet implemented")
   }

   @RequiresApi(Build.VERSION_CODES.O)
   private fun createNotificationChannel(channelId: String, channelName: String): String{
       val chan = NotificationChannel(
           channelId,
           channelName, NotificationManager.IMPORTANCE_NONE
       )
       chan.lightColor = Color.BLUE
       chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
       val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
       service.createNotificationChannel(chan)
       return channelId
   }

   @SuppressLint("MissingPermission")
   @RequiresApi(Build.VERSION_CODES.O)
   fun FileObserverFunction() {
       try {
           val locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager

           val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
           val currentDirectory = File(Environment.getExternalStorageDirectory().absolutePath + "/licenta")
           val watchService = FileSystems.getDefault().newWatchService()
           val pathToWatch = currentDirectory.toPath()
           var k= false

           Log.println(Log.INFO, "fisier", "fisierul monitorizat e la adresa: $pathToWatch")

           val pathKey = pathToWatch.register(
               watchService, StandardWatchEventKinds.ENTRY_CREATE,
               StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE
           )

           while (true) {
               k=false
               val watchKey = watchService.take()

               if (watchKey.pollEvents().isNotEmpty())
                   k=true
               if(k)
                   if (location != null) {
                       Log.i("FileObserver","Multe mesaje")
                       /*MainActivity().SendSMS(
                           location.latitude.toString(),
                           location.longitude.toString()
                       )*/
                   }
               if (!watchKey.reset()) {
                   watchKey.cancel()
                   watchService.close()
                   break
               }
           }
           Log.i("FileObserver","Multe mesaje")
           pathKey.cancel()
       }catch (e: NullPointerException)
       {
           e.printStackTrace()
       }
   }

}
*/

