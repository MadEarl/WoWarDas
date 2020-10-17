package de.mederle.wowardas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity(), LocationListener {

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private lateinit var tvGpsLocation: TextView
    private lateinit var myLocation: Location
    private var gotLocation: (Boolean) = false
    private var timesover: (Boolean) = false

    // If button is tapped within running time, alternate view ist triggered instead of write and exit
    private val taptimer = object : CountDownTimer(7000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            timesover = true
        }
    }

    // Show GPS coordinates for this time, then exit if button not tapped
    private val endtimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            this@MainActivity.finish()
            exitProcess(0)
        }
    }

    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.INTERNET
    )

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain =
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                            permissions[i]
                        )
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Go to settings and enable the permission",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            if (allSuccess)
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        taptimer.start()
        tvGpsLocation = findViewById(R.id.txt_location)!!
        tvGpsLocation.text = "GPS Location:\n"

        if (checkPermission(permissions)) {
            if (!gotLocation)
                getLocation()
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            Log.d("WoWarDas", "hasGps")
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onLocationChanged(location: Location) {
        // tvGpsLocation = findViewById(R.id.txt_location)
        if (!gotLocation) {
            tvGpsLocation.append("\nBreitengrad: " + location.latitude + "\nLängengrad: " + location.longitude)
            myLocation = location
            gotLocation = true
            if (timesover)
                endtimer.start()
        }
    }
}
