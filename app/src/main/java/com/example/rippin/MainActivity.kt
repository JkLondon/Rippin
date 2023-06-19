package com.example.rippin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import leakcanary.LeakCanary
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var requestQueue: RequestQueue? = null
    private var delay = 500
    private val timer = Timer()
    private var ssidText: WeakReference<TextView>? = null
    private var rssiText: WeakReference<TextView>? = null
    private var percText: WeakReference<TextView>? = null
    private var channelWidthText: WeakReference<TextView>? = null
    private var freqText: WeakReference<TextView>? = null
    private var timeText: WeakReference<TextView>? = null
    private var userInput: WeakReference<EditText>? = null
    private var ssid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MyLog", "onCreate")
        val tvTest = findViewById<TextView>(R.id.ssidText)
        tvTest.text = "Press the button"
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        ssidText = WeakReference(findViewById(R.id.ssidText))
        rssiText = WeakReference(findViewById(R.id.rssiText))
        percText = WeakReference(findViewById(R.id.percText))
        channelWidthText = WeakReference(findViewById(R.id.channelWidthText))
        freqText = WeakReference(findViewById(R.id.freqText))
        timeText = WeakReference(findViewById(R.id.timeText))
        userInput = WeakReference(findViewById(R.id.userInputWifiName))
    }

    fun onClickStop(view: View) {
        timer.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickTest(view: View) {
        val userInputText = userInput?.get()?.text.toString().lowercase()
        var rssi = 0
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        timer.schedule(object : TimerTask() {
            override fun run() {
                wifiManager.startScan()
                var scanResults = wifiManager.scanResults

                for (i in 0 until scanResults.size) {
                    // Check if the SSID matches your hotspot name
                    var scanResult = scanResults[i]
                    if (scanResult.SSID.lowercase() ==  userInputText) {
                        // Get the RSSI value in dBm
                        rssi = scanResult.level
                        val freq = scanResult.frequency
                        val channelWidth = scanResult.channelWidth

                        // Convert the RSSI value to a percentage using a utility method
                        val percentage = getWifiStrengthPercentage(rssi)
                        ssid = scanResult.SSID
                        Log.d("wifi", "Hotspot SSID: ${scanResult.SSID} RSSI: $rssi dBm $percentage%")
                        // Print the results
                        ssidText?.get()?.text = "Hotspot SSID: ${scanResult.SSID} "
                        rssiText?.get()?.text = "Hotspot RSSI: $rssi dBm "
                        percText?.get()?.text = "Hotspot Percentage: $percentage% "
                        channelWidthText?.get()?.text = "Hotspot ChannelWidth: $channelWidth "
                        freqText?.get()?.text = "Hotspot Frequency: $freq "

                        scanResult = null
                        break
                    }
                    scanResult = null
                }
                scanResults = null

                val currentUnixTime = System.currentTimeMillis() / 1000L
                val currentUnixTimeString = currentUnixTime.toString()
                timeText?.get()?.text = currentUnixTimeString

                val url = "http://81.163.27.74:3000/data"
                val jsonObject = JSONObject()
                jsonObject.put("rssi", rssi)
                jsonObject.put("time", currentUnixTimeString)
                jsonObject.put("ssid", ssid)

                requestQueue?.cancelAll("tag_request")
                requestQueue?.stop()
                requestQueue = Volley.newRequestQueue(applicationContext)
                val request = JsonObjectRequest(
                    Request.Method.POST, url, jsonObject,
                    { response ->
                        val responseString = response.toString()
                        Log.d("HTTP", "Response: $responseString")
                    },
                    { error ->
                        // Обработка ошибки
                        Log.e("HTTP", "Error: $error")
                    }).apply {
                    tag = "tag_request"
                }

                requestQueue?.add(request)
                System.gc()
            }
        }, 0, delay.toLong())
    }
    private fun getWifiStrengthPercentage(rssi: Int): Int {
        // The minimum and maximum RSSI values for Wi-Fi networks are -100 dBm and -30 dBm respectively
        val minRssi = -100
        val maxRssi = -30

        // Clamp the RSSI value between minRssi and maxRssi
        val clampedRssi = rssi.coerceIn(minRssi, maxRssi)

        // Calculate the percentage using a linear interpolation formula
        val percentage = ((clampedRssi - minRssi) * 100) / (maxRssi - minRssi)

        return percentage
    }


    override fun onStart() {
        super.onStart()
        Log.d("MyLog", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyLog", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MyLog", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLog", "onDestroy")
    }

    override fun onStop() {
        super.onStop()
        Log.d("MyLog", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("MyLog", "onRestart")
    }
}