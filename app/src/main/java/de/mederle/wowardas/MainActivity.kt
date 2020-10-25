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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity(), LocationListener {

    private var hasGps = false
    private lateinit var tvGpsLocation: TextView
    private lateinit var tvCountdownTimer: TextView
    private var stay: Boolean = false
    private lateinit var locationManager: LocationManager

    // Show GPS coordinates for this time, then exit if button not tapped
    private val endTimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            tvCountdownTimer.text = ""
            tvCountdownTimer.append("Knopf drücken oder App beendet sich in " + (millisUntilFinished / 1000).toString())
        }

        override fun onFinish() {
            if (!stay) {
                Log.d("WoWarDas", "endTimer: Now I should terminate.")
                this@MainActivity.finishAndRemoveTask()
                System.exit(0)
            } else {
                tvCountdownTimer.text = ""
            }
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
                            "In Einstellungen Berechtigung erteilen",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            if (allSuccess)
                Toast.makeText(this, "Alle nötigen Berechtigungen vorhanden!", Toast.LENGTH_SHORT)
                    .show()
            getLocation(locationManager)
        }
    }

    //@RequiresApi(Build.VERSION_CODES.R)
    //@SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvGpsLocation = findViewById(R.id.txt_location)
        tvCountdownTimer = findViewById(R.id.countdown_timer)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (checkPermission(permissions)) {
            getLocation(locationManager)
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        val showButton = findViewById<Button>(R.id.btn_show_locations)
        showButton.setOnClickListener {
            stay = true
            val intent = Intent(this, LocationViewActivity::class.java)
            startActivity(intent)
        }
    }

    //@RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private fun getLocation(locationManager: LocationManager) {
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            Log.d("WoWarDas", "hasGps")
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, this, mainLooper)

        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onLocationChanged(location: Location) {
        tvGpsLocation.text =
            "Breitengrad: ${location.latitude}\nLängengrad: ${location.longitude}"
        val sqlEntry = Entry(location.latitude.toFloat(), location.longitude.toFloat())
        val sqlConnection = StorageSQL(this, null)
        sqlConnection.addEntry(sqlEntry)
        locationManager.removeUpdates(this)
        Log.d("WoWarDas", "End Timer starting ...")
        endTimer.start()
    }
}
