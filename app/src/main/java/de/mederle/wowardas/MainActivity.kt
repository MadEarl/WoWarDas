package de.mederle.wowardas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.mederle.wowardas.StorageSQL.Companion.DATABASE_NAME
import kotlin.system.exitProcess

private const val PERMISSION_REQUEST = 10

class MainActivity : AppCompatActivity(), LocationListener {

    companion object {
        lateinit var storageSQL: StorageSQL
    }

    private var hasGps = false
    private lateinit var tvGpsLocation: TextView
    private lateinit var tvCountdownTimer: TextView
    private lateinit var doItAgainButton: Button
    private var stay: Boolean = false
    private lateinit var locationManager: LocationManager

    // Show GPS coordinates for this time, then exit if button not tapped
    private val endTimer = object : CountDownTimer(5000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            tvCountdownTimer.visibility = View.VISIBLE
            tvCountdownTimer.text = ""
            tvCountdownTimer.append(getString(R.string.countdown_timer) + (millisUntilFinished / 1000).toString())
        }

        override fun onFinish() {
            if (!stay) {
                Log.d("WoWarDas", "endTimer: Now I should terminate.")
                this@MainActivity.finishAndRemoveTask()
                exitProcess(0)
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
                        shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(
                            this,
                            getString(R.string.permission_denied),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.grant_permissions),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            if (allSuccess)
                Toast.makeText(this, getString(R.string.permissions_granted), Toast.LENGTH_SHORT)
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
        doItAgainButton = findViewById(R.id.btn_get_additional_location)
        if (!stay) doItAgainButton.visibility = View.GONE
        Log.d("WoWarDas", "Just set doItAgainButton to gone.")
        storageSQL = StorageSQL(this, DATABASE_NAME, null, 2)

        if (checkPermission(permissions)) {
            getLocation(locationManager)
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        val showButton = findViewById<Button>(R.id.btn_show_locations)
        showButton.setOnClickListener {
            stay = true
            endTimer.cancel()
            val intent = Intent(this, LocationViewActivity::class.java)
            startActivity(intent)
        }

        doItAgainButton.setOnClickListener {
            if (!stay) {
                stay = true
                endTimer.cancel()
                tvCountdownTimer.text = ""
                doItAgainButton.text = getString(R.string.get_additional_location)
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500,
                    0f,
                    this,
                    mainLooper
                )
                getLocation(locationManager)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("WoWarDas", "Coming back to MainActivity, changing btn text.")
        if (stay) {
            doItAgainButton.text = getString(R.string.get_additional_location)
            doItAgainButton.visibility = View.VISIBLE
            tvCountdownTimer.visibility = View.INVISIBLE
        }
    }

    //@RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private fun getLocation(locationManager: LocationManager) {
        tvGpsLocation.text = getString(R.string.searching_for_GPS)
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            Log.d("WoWarDas", "hasGps")
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500,
                0f,
                this,
                mainLooper
            )
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        tvGpsLocation.text =
                getString(R.string.latitude)  + location.latitude + "\n" + getString(R.string.longitude) + location.longitude
        val sqlEntry = Entry(location.latitude.toFloat(), location.longitude.toFloat())
        storageSQL.addEntry(this, sqlEntry)
        locationManager.removeUpdates(this)
        if (!stay) {
            doItAgainButton.text = getString(R.string.dont_go)
            doItAgainButton.visibility = View.VISIBLE
            Log.d("WoWarDas", "End Timer starting ...")
            endTimer.start()
        }
    }
}
