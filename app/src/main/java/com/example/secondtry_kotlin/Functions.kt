package com.example.secondtry_kotlin


import android.content.Context
import android.os.Build
import android.os.Environment
import android.telephony.SmsManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class Functions : AppCompatActivity() {

    fun ReadFile(file: String): String? {
        var result: String? = null
        try {
            //val file = "saved.txt"//filename.text.toString()

            var fileInputStream: FileInputStream? = null
            fileInputStream = openFileInput(file)

            var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine();text }() != null) {
                stringBuilder.append(text + "\n")
            }
            result = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            showToast("No data saved")
        }
        if(result!="")
            return result
        else
            return "Not a message"
    }
    fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun ReadExternalFile(): String? {
        var result: String? = ""
        var sdcard = Environment.getExternalStorageDirectory().absolutePath
        var dir = File(sdcard + "/licenta")

        if (dir.exists()) {
            val file = dir.absolutePath+"/"+"licenta.txt"
            var os: FileOutputStream? = null
            var sb: StringBuilder = StringBuilder()
            try {
                val bufferedReader: BufferedReader = BufferedReader(FileReader(file))

                var text: String? = null
                while ({ text = bufferedReader.readLine();text }() != null) {
                    sb.append(text + "\n")
                    result += sb.toString()
                }
                bufferedReader.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                showToast("File not found at ${dir.absolutePath.toString()}")
            }
        }
        //showToast("$result")
        showToast("File path is ${dir.absolutePath.toString()}")
        return result

    }
    fun WriteFile(file: String,smth: TextView)
    {
        val data =smth.text.toString();

        val fileOutputStream: FileOutputStream


        try {

            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()

            //showToast("Details saved successfully")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun WriteFile(file: String,smth: EditText)
    {
        val data =smth.text.toString();

        val fileOutputStream: FileOutputStream


        try {

            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()

            //showToast("Details saved successfully")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun WriteFile(file:String, phonenb:String, firstName: TextView, lastName: TextView, age: TextView, phoneNumber: EditText)
    {
        val data =
            "First name: " + firstName.text.toString() + "\n" + "Last name: " + lastName.text.toString() + "\n" + "Age:" + age.text.toString()


        val fileOutputStream: FileOutputStream
        val fileOutputStreamPhone :FileOutputStream

        try {
            if( phoneNumber.text.toString()!="") {
                val phonedata = phoneNumber.text.toString()
                fileOutputStreamPhone=openFileOutput(phonenb,Context.MODE_PRIVATE)
                fileOutputStreamPhone.write(phonedata.toByteArray())
                fileOutputStreamPhone.close()
            }
            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.close()

            showToast("Details saved successfully")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun SendSMS() {
        try {
            val phoneNo = ReadFile("phone.txt")
            if (phoneNo == "") {
                showToast("Phone number has not been set up!")
            } else {
                try {
                    val SMS = ReadFile("phone.txt")
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
        } catch (e: FileNotFoundException ) {
            e.printStackTrace()
            showToast("Saved.txt not found")
        }
    }
    fun CreateFile(filename: String): Boolean {
        if(!File(filename).exists())
            return false
        else
            return true
    }
}