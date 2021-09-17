package com.sablab.speedometer_view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.sablab.speedometerview.SpeedometerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    private var job: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val seekBar = findViewById<MaterialButton>(R.id.button)

        seekBar.setOnClickListener {
            startProgress()
        }
        startProgress()
    }

    private fun startProgress() {
        val speedometerView = findViewById<SpeedometerView>(R.id.speedometerView)

        job?.cancel()
        job = lifecycleScope.launchWhenCreated {
            for (i in 0..360) {
                delay(1)
                speedometerView.speed = i.toFloat()
            }

            for (i in 360 downTo 0) {
                delay(1)
                speedometerView.speed = i.toFloat()
            }
        }
    }
}