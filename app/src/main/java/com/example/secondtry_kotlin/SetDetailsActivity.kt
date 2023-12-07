package com.example.secondtry_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import java.io.*
import java.util.*


class SetDetailsActivity: AppCompatActivity() {


    @SuppressLint("WrongViewCast")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_details)



        val file_all_data = "saved.txt"//filename.text.toString()
        val phoneNumber_file="phone.txt"
        val firstName_file="firstName.txt"
        val lastName_file="lastName.txt"
        val age_file="age.txt"
        val messageContent_file="messageContent.txt"


        val messageContent=findViewById<TextView>(R.id.MessageContentEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)
        val firstName = findViewById<TextView>(R.id.firstNamePlainText)
        val lastName = findViewById<TextView>(R.id.lastNamePlainText)
        val age = findViewById<TextView>(R.id.agePlainText)
        val phoneNumber=findViewById<EditText>(R.id.phoneNumberEditText)


        try {
            if (ReadFile(firstName_file) != "") {
                try {
                    firstName.setText(ReadFile(firstName_file))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else
                showToast("Nu acceseaza fisierul firstName")
        }catch (e: FileNotFoundException)
        {
            e.printStackTrace()
        }


        try {
            if (ReadFile(lastName_file) != "") {
                try {
                    lastName.setText(ReadFile(lastName_file))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else
                showToast("Nu acceseaza fisierul firstName")
        }catch (e: FileNotFoundException)
        {
            e.printStackTrace()
        }


        try {
            if (ReadFile(age_file) != "") {
                try {
                    age.setText(ReadFile(age_file))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else
                showToast("Nu acceseaza fisierul firstName")
        }catch (e: FileNotFoundException)
        {
            e.printStackTrace()
        }


        try {
            if (ReadFile(phoneNumber_file) != "") {
                try {
                    phoneNumber.setText(ReadFile(phoneNumber_file))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else
                showToast("Nu acceseaza fisierul firstName")
        }catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            if (ReadFile(messageContent_file) != "") {
                try {
                    messageContent.setText(ReadFile(messageContent_file))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            } else
                showToast("Nu acceseaza fisierul messageContent_file.txt")
        }catch (e: FileNotFoundException) {
            e.printStackTrace()
        }



        saveButton.setOnClickListener {


            if(firstName.text.toString()==""&&lastName.text.toString()==""&&age.text.toString()=="" && phoneNumber.text.toString()=="" &&messageContent.text.toString()=="")
                startActivity(Intent(this, MainActivity::class.java))
            else {

                WriteFile(file_all_data, phoneNumber_file, firstName, lastName, age, phoneNumber)
                WriteFile(phoneNumber_file, phoneNumber)
                WriteFile(firstName_file, firstName)
                WriteFile(lastName_file, lastName)
                WriteFile(age_file, age)
                WriteFile(messageContent_file,messageContent)

                startActivity(Intent(this, MainActivity::class.java))
            }


        }

    }

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
    fun WriteFile(file: String, smth: TextView)
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
    fun WriteFile(file: String, smth: EditText)
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

    fun WriteFile(file: String, phonenb: String, firstName: TextView, lastName: TextView, age: TextView, phoneNumber: EditText)
    {
        val data =
            "First name: " + firstName.text.toString() + "\n" + "Last name: " + lastName.text.toString() + "\n" + "Age:" + age.text.toString()


        val fileOutputStream: FileOutputStream
        val fileOutputStreamPhone : FileOutputStream

        try {
            if( phoneNumber.text.toString()!="") {
                val phonedata = phoneNumber.text.toString()
                fileOutputStreamPhone=openFileOutput(phonenb, Context.MODE_PRIVATE)
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


    fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }





}
