package com.example.weatherapp


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {


    var cityzip = "1001"
    val API: String = "f2763f64328617a339894577e9107052"

    lateinit var Loader: ProgressBar
    lateinit var mainContainer: RelativeLayout
    lateinit var llErrorContainer: LinearLayout

    lateinit var addressContainer: LinearLayout
    private lateinit var rlzip: RelativeLayout
    private lateinit var etzip: EditText
    private lateinit var bzip: Button
    private lateinit var bError: Button

    lateinit var temp: TextView
    lateinit var tempf: TextView
    var c = 00.00
    var f = 00.00

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //weatherTask()

        Loader = findViewById(R.id.Loader)
        mainContainer = findViewById(R.id.mainContainer)
        llErrorContainer = findViewById(R.id.llErrorContainer)

        addressContainer = findViewById(R.id.addressContainer)
        rlzip = findViewById(R.id.rlzip)
        etzip = findViewById(R.id.etzip)
        bzip = findViewById(R.id.bzip)


        addressContainer.setOnClickListener {
            rlzip.isVisible = true
        }

        bzip.setOnClickListener {
            cityZip()
        }

        bError = findViewById(R.id.bError)
        bError.setOnClickListener {
            rlzip.isVisible = true
        }

        temp = findViewById(R.id.temp)
        tempf = findViewById(R.id.tempf)

            temp.setOnClickListener {
             temp.isVisible = false
                tempfshow()
            }

    }

    fun cityZip() {
        cityzip = etzip.text.toString()
        weatherTask()
        etzip.text.clear()
        // Hide Keyboard
        val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)

        rlzip.isVisible = false
    }


    private fun weatherTask() {

        CoroutineScope(IO).launch {
            updateStatus(-1)

            val data = async {
                fetchWeatherData()
            }.await()

            if (data.isNotEmpty()) {
                updateStatus(0)
                updateWeatherData(data)
            } else {
                updateStatus(1)
            }
        }
    }

    private fun fetchWeatherData(): String {

        var response = ""
        try {
            response =
                URL("https://api.openweathermap.org/data/2.5/weather?zip=$cityzip&units=metric&appid=$API")
                    .readText(Charsets.UTF_8)
        } catch (e: Exception) {
            println("Error: $e")
        }
        return response
    }

    private suspend fun updateWeatherData(result: String) {
        withContext(Main) {

            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val updatedAt: Long = jsonObj.getLong("dt")
            val updatedAtText = "Updated at: " + SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.ENGLISH
            ).format(Date(updatedAt * 1000))

            val temp = main.getString("temp") + "°C"
            val temp_min = "Min Temp: " + main.getString("temp_min") + "°C"
            val temp_max = "Max Temp: " + main.getString("temp_max") + "°C"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherDescription = weather.getString("description")
            val address = jsonObj.getString("name") + ", " + sys.getString("country")

            findViewById<TextView>(R.id.address).text = address
            findViewById<TextView>(R.id.updated_at).text = updatedAtText
            findViewById<TextView>(R.id.status).text = weatherDescription.uppercase()



            findViewById<TextView>(R.id.temp_min).text = temp_min
            findViewById<TextView>(R.id.temp_max).text = temp_max

            findViewById<TextView>(R.id.sunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            findViewById<TextView>(R.id.sunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))

            findViewById<TextView>(R.id.wind).text = windSpeed
            findViewById<TextView>(R.id.pressure).text = pressure
            findViewById<TextView>(R.id.humidity).text = humidity
            findViewById<LinearLayout>(R.id.llRefresh).setOnClickListener { weatherTask() }

        }

    }

    private suspend fun updateStatus(state: Int) {
//        states: -1 = loading, 0 = loaded, 1 = error
        withContext(Main) {
            when {
                state < 0 -> {
                    findViewById<ProgressBar>(R.id.Loader).visibility = View.VISIBLE
                    findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.llErrorContainer).visibility = View.GONE
                }
                state == 0 -> {
                    findViewById<ProgressBar>(R.id.Loader).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
                }
                state > 0 -> {
                    findViewById<ProgressBar>(R.id.Loader).visibility = View.GONE
                    findViewById<LinearLayout>(R.id.llErrorContainer).visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun tempfshow(){
        val tempnum = temp.text.substring(0,temp.length()-2)

        c = tempnum.toDouble()
        f = 9*c /5 + 32

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        val shorttempf = df.format(f).toDouble()

        tempf.text = shorttempf.toString()+"°F"

        tempf.isVisible = true

        tempf.setOnClickListener {
            temp.isVisible = true
            tempf.isVisible = false

        }
    }
}