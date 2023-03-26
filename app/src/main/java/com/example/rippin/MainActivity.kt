package com.example.rippin

import android.content.Context
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
import com.example.rippin.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.PointsGraphSeries
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var handler: Handler = Handler()
    var runnable: Runnable? = null
    private var delay = 1000
    private val dbmPoints = mutableListOf<DataPoint>()
    var t = 0
    lateinit var graphView: GraphView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MyLog", "onCreate")
        val tvTest = findViewById<TextView>(R.id.ssidText)
        tvTest.text = "Нажми на кнопку"
    }

    fun onClickStop(view: View) {
        handler.removeCallbacks(runnable!!)
        graphView = findViewById(R.id.idGraphView)
        graphView.title = "зависимость силы сигнала от времени"
        // on below line we are creating a new data
        // point series for our point graph series.
        // we are calling get data point method to add
        // data point to our point graph series
        val series: PointsGraphSeries<DataPoint> = PointsGraphSeries<DataPoint>(getDataPoint())
        Log.d("wifi", dbmPoints.toString())
        dbmPoints.clear()
        t = 0
        graphView.removeAllSeries()
        // on below line we are adding series to graph view
        graphView.addSeries(series)

        // on below line we are setting scrollable
        // for point graph view
        graphView.viewport.isScrollable = true

        // on below line we are setting scalable.
        graphView.viewport.isScalable = true

        // on below line we are setting scalable y
        graphView.viewport.setScalableY(true)

        // on below line we are setting scrollable y
        graphView.viewport.setScrollableY(true)

        // on below line we are setting shape for series.
        series.shape = PointsGraphSeries.Shape.POINT

        // on below line we are setting size for series.
        series.size = 5f

        // on below line we are setting color for series.
        series.color = R.color.purple_200

    }

    private fun getDataPoint(): Array<DataPoint> {
        return dbmPoints.toTypedArray()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onClickTest(view: View) {
        val ssidText = findViewById<TextView>(R.id.ssidText)
        val rssiText = findViewById<TextView>(R.id.rssiText)
        val percText = findViewById<TextView>(R.id.percText)
        val channelWidthText = findViewById<TextView>(R.id.channelWidthText)
        val freqText = findViewById<TextView>(R.id.freqText)
        val timeText = findViewById<TextView>(R.id.timeText)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val userInput = findViewById<EditText>(R.id.userInputWifiName)

        var ssidTextRaw = ""
        var rssiTextRaw = ""
        var percTextRaw = ""
        var channelWidthTextRaw = ""
        var freqTextRaw = ""
        var rssi = 0
        t = 0
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            wifiManager.startScan()
            val scanResults = wifiManager.scanResults
            for (scanResult in scanResults) {
                // Check if the SSID matches your hotspot name
                if (scanResult.SSID.lowercase() ==  userInput.text.toString().lowercase()) {
                    // Get the RSSI value in dBm
                    rssi = scanResult.level
                    val freq = scanResult.frequency
                    val channelWidth = scanResult.channelWidth

                    // Convert the RSSI value to a percentage using a utility method
                    val percentage = getWifiStrengthPercentage(rssi)
                    Log.d("wifi", "Hotspot SSID: ${scanResult.SSID} RSSI: $rssi dBm $percentage%")
                    // Print the results
                    ssidTextRaw = "Hotspot SSID: ${scanResult.SSID} "
                    rssiTextRaw = "Hotspot RSSI: $rssi dBm "
                    percTextRaw = "Hotspot Percentage: $percentage% "
                    channelWidthTextRaw = "Hotspot ChannelWidth: $channelWidth "
                    freqTextRaw = "Hotspot Frequency: $freq "
                }
            }
            ssidText.text = ssidTextRaw
            rssiText.text = rssiTextRaw
            percText.text = percTextRaw
            channelWidthText.text = channelWidthTextRaw
            freqText.text = freqTextRaw

            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("HH:mm:ss")
            val current = formatter.format(time)
            timeText.text = current
            dbmPoints.add(DataPoint(t.toDouble(), rssi.toDouble()))
            t += 1
        }.also { runnable = it }, delay.toLong())

        // A utility method that converts RSSI value to percentage

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