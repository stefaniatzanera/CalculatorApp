package com.example.calculatorapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.calculatorapp.ui.theme.CalculatorAppTheme

class MainActivity : ComponentActivity() {
    private val result by lazy { findViewById<TextView>(R.id.result_txt)}
    private val calculation by lazy { findViewById<TextView>(R.id.calculation_txt)}

    private var canAddOperation = false
    private var canAddDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //function for each time a NUMBER button is pressed
    fun numberAction(view: View) {
        if(view is Button) {
            if (view.text == ".") {
                if (canAddDecimal) {
                    calculation.append(view.text)
                    canAddDecimal = false
                }
            } else {
                calculation.append(view.text)
                canAddOperation = true
            }
        }
    }

    //function for operator buttons
    fun operationAction(view: View) {
        if (view is Button && canAddOperation) {
            calculation.append(view.text)
            canAddOperation = false
            canAddDecimal = true
        }
    }

    //function to clear the text
    fun clearAction(view: View) {
        result.text = ""
        calculation.text = ""
    }

    //function to delete a number
    fun backspaceAction(view: View) {
        val length = calculation.text.length
        if (length > 0) {
            calculation.text = calculation.text.subSequence(0, length - 1)
        }
    }

    //function for equal button
    fun equalAction(view: View) {
        result.text = calculateResult()
    }

    //function to calculate the result
    private fun calculateResult(): String{
        val digitsOperators = digitsOperators()
        if(digitsOperators.isEmpty()) return ""
        val timesDivision = timesDivisionCalculation(digitsOperators)
        val result = addSubtractCalculate(timesDivision)
        return result.toString()
    }

    //function for add-Subtract operations calculation
    private fun addSubtractCalculate(passedList: MutableList<Any>): Float {
        var result = passedList[0] as Float

        for(i in passedList.indices){
            if(passedList[i] is Char && i != passedList.lastIndex){
                val operator = passedList[i] as Char
                val nextDigit = passedList[i + 1] as Float
                if(operator == '+'){
                    result += nextDigit
                }
                if(operator == '-'){
                    result -= nextDigit
                }
            }
        }
        return result
    }

    //function for times-Division operations calculation
    private fun timesDivisionCalculation(passedList: MutableList<Any>): MutableList<Any> {
        var list = passedList
        while(list.contains('x') || list.contains('/')){
            list = calcTimesDiv(list)
        }
        return list
    }

    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any> {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for(i in  passedList.indices) {
            if (passedList[i] is Char && i != passedList.lastIndex && i < restartIndex) {
                val operator = passedList[i]
                val prevDigit = passedList[i - 1] as Float
                val nextDigit = passedList[i + 1] as Float
                when (operator) {
                    'x' -> {
                        newList.add(prevDigit * nextDigit)
                        restartIndex = i + 1
                    }

                    '/' -> {
                        newList.add(prevDigit / nextDigit)
                        restartIndex = i + 1
                    }

                    else -> {
                        newList.add(prevDigit)
                        newList.add(operator)
                    }
                }
            }
            if (i > restartIndex) {
                newList.add(passedList[i])
            }
        }
        return newList
    }

    //function to convert the number charachers to float and operators to char
    private fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for (character in calculation.text) {
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

    fun exchangecurrencyAction(view: View) {
        val intent = Intent(this, ExchangeCurrencyActivity::class.java)
        ContextCompat.startActivity(this, intent, null)
    }
}