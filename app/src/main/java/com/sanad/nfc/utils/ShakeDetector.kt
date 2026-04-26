package com.sanad.nfc.utils
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(context: Context, private val onShake: () -> Unit) : SensorEventListener {
    private var sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var lastShakeTime = 0L
    fun start() { accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) } }
    fun stop() { sensorManager.unregisterListener(this) }
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0]; val y = it.values[1]; val z = it.values[2]
            val magnitude = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            if (magnitude > 15f && System.currentTimeMillis() - lastShakeTime > 1000) {
                lastShakeTime = System.currentTimeMillis(); onShake()
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
