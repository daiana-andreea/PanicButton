package com.example.secondtry_kotlin

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.File
import java.nio.channels.Channel
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchKey

class BluetoothListener : Service() {

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        startForeground()
       // Thread { Looper.prepare()
         //   FileObserverFunction(MainActivity.EXIST) }.start()
           // Looper.loop()


        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )
        var Channel = createNotificationChannel("Panic_Button", "Bluetooth Listener")
        startForeground(
            NOTIF_ID,
            NotificationCompat.Builder(
                this,
                Channel
            ) // don't forget create a notification channel first
                .setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Service is running background")
                .setContentIntent(pendingIntent)
                .build()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
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

    @SuppressLint("MissingPermission", "ServiceCast")
    @RequiresApi(Build.VERSION_CODES.O)
    fun FileObserverFunction(exist: Boolean) {
        if (!exist) {
            try {
                MainActivity.EXIST = true


                val currentDirectory =
                    File(Environment.getExternalStorageDirectory().absolutePath + "/licenta")
                MainActivity.watchService = FileSystems.getDefault().newWatchService()
                val pathToWatch = currentDirectory.toPath()
                var flag = false

                Log.println(Log.INFO, "fisier", "fisierul monitorizat e la adresa: $pathToWatch")

                val pathKey = pathToWatch.register(
                    MainActivity.watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE
                )

                while (true) {
                    flag = false
                    try {
                        val watchKey = MainActivity.watchService.take()
                        if (watchKey.pollEvents().isNotEmpty())
                            flag = true
                        if (flag)
                            if (MainActivity.location != null) {

                                MainActivity().SendSMS(MainActivity.location.latitude.toString(), MainActivity.location.longitude.toString())
                                Log.i("FileObserver", "Message Was Sent")
                                flag = false

                                }


                             else {
                                Log.i("File Observer", "No location discovered")
                            }
                        if (!watchKey.reset()) {
                            watchKey.cancel()
                            MainActivity.watchService.close()
                            break
                        }

                    } catch (
                        @SuppressLint(
                            "NewApi",
                            "LocalSuppress"
                        ) e: ClosedWatchServiceException
                    ) {
                        e.printStackTrace()
                        Log.i("WatchService", "Closed Service")

                    }
                }


                // Log.i("FileObserver","Multe mesaje")
                pathKey.cancel()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }

    }

    companion object {
        private const val NOTIF_ID = 1
        private const val NOTIF_CHANNEL_ID = "Channel_Id"
    }
}