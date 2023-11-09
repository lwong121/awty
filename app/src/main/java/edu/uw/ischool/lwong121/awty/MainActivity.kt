package edu.uw.ischool.lwong121.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

const val ALARM_ACTION = "edu.uw.ischool.lwong121.ALARM"
const val TAG = "awty"
const val MIN_IN_MILLIS = 60 * 1000

class MainActivity : AppCompatActivity() {
    private var isAwtyActivated: Boolean = false

    private var receiver : BroadcastReceiver? = null

    private lateinit var messageEditText : EditText
    private lateinit var phoneEditText : EditText
    private lateinit var minsEditText : EditText
    private lateinit var startStopBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageText)
        phoneEditText = findViewById(R.id.phoneText)
        minsEditText = findViewById(R.id.minsIntervalText)
        startStopBtn = findViewById(R.id.startStopBtn)

        startStopBtn.setOnClickListener {
            if (!isAwtyActivated) {
                startAwty()
            } else {
                stopAwty()
            }
        }
    }

    private fun startAwty() {
        val activityThis = this

        val message = messageEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val intervalMins = minsEditText.text.toString()
        val intervalMillis = intervalMins.toLongOrNull()

        if (!message.isNullOrEmpty() && !phone.isNullOrEmpty() && intervalMillis != null && intervalMillis > 0) {
            startStopBtn.text = getString(R.string.stop_button)
            isAwtyActivated = true

            val formattedPhone = PhoneNumberUtils.formatNumber(phone, Locale.getDefault().country)

             // create the PendingIntent
            val intent = Intent(ALARM_ACTION)
            val pendingIntent =
                PendingIntent.getBroadcast(activityThis, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // get the AlarmManager
            val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                intervalMillis.times(MIN_IN_MILLIS),
                pendingIntent
            )

            if (receiver == null) {
                receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        Log.i(TAG, "recieved. making toast...")

                        // original version
                        // Toast.makeText(activityThis, "${formattedPhone}: $message", Toast.LENGTH_LONG).show()

                        // custom toast
                        val customToastView = layoutInflater.inflate(R.layout.custom_toast, null)

                        val toastCaptionTextView = customToastView.findViewById<TextView>(R.id.captionTextView)
                        val toastMessageTextView = customToastView.findViewById<TextView>(R.id.messageTextView)

                        toastCaptionTextView.text = "Texting $formattedPhone"
                        toastMessageTextView.text = message

                        val customToast = Toast(activityThis)
                        customToast.view = customToastView
                        customToast.duration = Toast.LENGTH_LONG
                        customToast.show()
                    }
                }
                val filter = IntentFilter(ALARM_ACTION)
                registerReceiver(receiver, filter)
            }
        } else {
            // create a toast so that we have some way to tell users about incorrect input
            Toast.makeText(activityThis, "Oh no! Cannot start AWTY because one or more values are invalid. Fix and try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopAwty() {
        unregisterReceiver(receiver)
        receiver = null

        Log.i(TAG, "stopping...")

        startStopBtn.text = getString(R.string.start_button)
        isAwtyActivated = false
    }
}