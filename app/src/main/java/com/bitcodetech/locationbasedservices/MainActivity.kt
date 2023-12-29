package com.bitcodetech.locationbasedservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bitcodetech.locationbasedservices.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var binding : ActivityMainBinding

    private val locationBR = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val location =
                intent!!.getSerializableExtra(LocationManager.KEY_LOCATION_CHANGED) as Location
            binding.txtInfo.text = "*** ${location.latitude} , ${location.longitude}"
        }
    }

    @SuppressLint("NewApi", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //register br

        registerReceiver(
            locationBR,
            IntentFilter("in.bitcode.LOC"),
            RECEIVER_EXPORTED
        )

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if( !locationManager.isLocationEnabled) {
            startActivity( Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        binding.txtInfo.append("All Providers: \n")
        for(provider in locationManager.allProviders) {
            binding.txtInfo.append("$provider \n")
        }
        binding.txtInfo.append("------------------\n")

        val enabledProviders = locationManager.getProviders(true)

        for(provider in locationManager.allProviders) {
            val locationProvider = locationManager.getProvider(provider)
            binding.txtInfo.append("${locationProvider!!.name} \n")
            binding.txtInfo.append("Power: ${locationProvider!!.powerRequirement} \n")
            binding.txtInfo.append("Cost? ${locationProvider!!.hasMonetaryCost()} \n")
            binding.txtInfo.append("Accuracy: ${locationProvider!!.accuracy} \n")
            binding.txtInfo.append("Speed? ${locationProvider!!.supportsSpeed()} \n")
            binding.txtInfo.append("Cell Needed? ${locationProvider!!.requiresCell()} \n")
            binding.txtInfo.append("Sat Needed? ${locationProvider!!.requiresSatellite()} \n")
            binding.txtInfo.append("NW Needed? ${locationProvider!!.requiresNetwork()} \n")
            binding.txtInfo.append("Alt Needed? ${locationProvider!!.supportsAltitude()} \n")

            val location = locationManager.getLastKnownLocation(provider)
            if(location != null) {
                binding.txtInfo.append("last location: \n ${location.latitude} - ${location.longitude} \n")
            }

            binding.txtInfo.append("-----------------------\n")
        }

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isCostAllowed = true
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.isAltitudeRequired = true

        val matchingProviders = locationManager.getProviders(criteria, true)
        binding.txtInfo.append("Matching Providers: \n")
        for(provider in matchingProviders) {
            binding.txtInfo.append("$provider \n")
        }
        binding.txtInfo.append("------------------------\n")

        val bestProvider = locationManager.getBestProvider(criteria, true)
        binding.txtInfo.append("Best Provider: $bestProvider \n")
        binding.txtInfo.append("------------------------\n")


        val locationListener = MyLocationListener()
        locationManager.requestLocationUpdates(
            bestProvider!!,
            1000L,
            100.0f,
            locationListener
        )

        val intent = Intent("in.bitcode.LOC")
        val pendingLocationIntent = PendingIntent.getBroadcast(
            this,
            2,
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            100.0F,
            pendingLocationIntent
        )

        //locationManager.removeUpdates(locationListener)

    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            binding.txtInfo.text = "${location.latitude} , ${location.longitude} "
        }

    }
}