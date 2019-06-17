package com.example.sensorlogger

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.reflect.full.staticProperties

var TAG = "SensorLogger"

class MainActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private val sensor_names = listOf(
        "ACCELEROMETER", "GYROSCOPE", "GYROSCOPE_UNCALIBRATED", "LINEAR_ACCELERATION",
        "MAGNETIC_FIELD", "MAGNETIC_FIELD_UNCALIBRATED", "MOTION_DETECT", "ORIENTATION", "POSE_6DOF", "ROTATION_VECTOR",
        "SIGNIFICANT_MOTION", "STEP_COUNTER", "STEP_DETECTOR"
    )
    private val sensors = Sensor::class.staticProperties
        .filter { it.name in sensor_names.map { "TYPE_" + it } }
        .map { it.name.substring(5) to it.get() as Int }.toMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        checkSensors()

        sensors.forEach { Log.d(TAG, "sensor value: $it") }
        logActionButton.setOnClickListener {
            Log.v(TAG, "onclick")
        }

        checkMultiPermissions()
    }

    override fun onPostResume() {
        super.onPostResume()
    }

    private fun checkSensors() {
        var strbuf = StringBuffer("Sensor List:\n")
        sensor_names.forEach { name ->
            var id = sensors.get(name)
            if (id != null) {
                var sensor = sensorManager.getDefaultSensor(id)
                var ok_ng = if (sensor != null) "OK" else "NG"
                strbuf.append(ok_ng + ": " + name + "\n")
            }
        }

        sensorInfo.text = strbuf

        sensorManager.getSensorList(Sensor.TYPE_ALL).forEach { sensor ->
            Log.v(TAG, "Sensor: ${sensor.name}")
        }
    }

    private fun startSensorService() {
        setContentView(R.layout.activity_main)

    }

    private fun checkMultiPermissions() {
        var reqs = mutableListOf<String>()

        val permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionStorage == PackageManager.PERMISSION_GRANTED) {
            // pass
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE: granted")
        } else {
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE: not permitted")
            reqs.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!reqs.isEmpty()) {
            ActivityCompat.requestPermissions(this, reqs.toTypedArray(), 101 /* request multi permission */)
        } else {
            // TODO
            Log.d(TAG, "All permissions granted!!!")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == 101) {
            for (i in 0..permissions.size - 1) {
                if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // pass
                    } else {
                        val toast = Toast.makeText(this, "外部メディアへの書き込み許可がないため実行できません", Toast.LENGTH_SHORT)
                        toast.show()
                        finish()
                    }
                }
            }
        } else {
            Log.d(TAG, "unexpected request code: $requestCode")
            finish()
        }
    }
}
