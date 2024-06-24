package com.example.calculatorapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Serializable
data class ApiResponse(
    val success: Boolean,
    val timestamp: Long,
    val base: String,
    val rates: Map<String, Double>
)

class ExchangeCurrencyActivity : ComponentActivity() {
    private val convertButton by lazy { findViewById<ImageButton>(R.id.convertButton) }
    private val amountEditText by lazy {findViewById<EditText>(R.id.convertedamount)}
    private val fromSpinner by lazy { findViewById<Spinner>(R.id.fromSpinner) }
    private val toSpinner by lazy { findViewById<Spinner>(R.id.toSpinner) }
    private val resultTextView by lazy { findViewById<TextView>(R.id.resultTextView) }

    private var canAddOperation = false
    private var canAddDecimal = true


    private val json = Json { ignoreUnknownKeys = true }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_currency)

        val context: Context = this
        if (isInternetAvailable(context)) {
            convertButton.setOnClickListener {
                val fromCurrency = fromSpinner.selectedItem.toString()
                val toCurrency = toSpinner.selectedItem.toString()
                val amount = amountEditText.text.toString().toDouble()

                CoroutineScope(Dispatchers.IO).launch {
                    val apiResponse = URL("https://api.fxratesapi.com/latest?api_key=fxr_live_6df6d1d1bfdea0c2b318c32954fae291acea&base=$fromCurrency").readText()
                    val response = json.decodeFromString<ApiResponse>(apiResponse)
                    val rate = response.rates[toCurrency] ?: error("Rate not found")
                    val result = amount * rate

                    withContext(Dispatchers.Main) {
                        resultTextView.text = result.toString()
                    }
                }
            }
        } else {
            showNoInternetAlert()
        }
    }

    fun numberAction(view: View) {
        if(view is Button) {
            if (view.text == ".") {
                if (canAddDecimal) {
                    amountEditText.append(view.text)
                    canAddDecimal = false
                }
            } else {
                amountEditText.append(view.text)
                canAddOperation = true
            }
        }
    }

    fun operationAction(view: View) {
        if (view is Button && canAddOperation) {
            amountEditText.append(view.text)
            canAddOperation = false
            canAddDecimal = true
        }
    }

    fun clearAction(view: View) {
        amountEditText.setText("")
    }

    fun backspaceAction(view: View) {
        val length = amountEditText.text.length
        if (length > 0) {
            amountEditText.setText(amountEditText.text.subSequence(0, length - 1))
        }
    }

    private fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for (character in amountEditText.text) {
            if (character.isDigit() || character == '.') {
                currentDigit += character
            } else {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(character)
            }
        }
        if (currentDigit.isNotEmpty()) {
            list.add(currentDigit.toFloat())
        }
        return list
    }

    private fun showNoInternetAlert() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.alert_dialog_for_internet)
        dialog.window?.setBackgroundDrawable(ColorDrawable(TRANSPARENT))
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            finish()
        }, 3000) // Dismiss after 3 seconds
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    private fun returnAction(){
        val intent = Intent(this@ExchangeCurrencyActivity, MainActivity::class.java)
        ContextCompat.startActivity(this@ExchangeCurrencyActivity, intent, null)
    }
}
