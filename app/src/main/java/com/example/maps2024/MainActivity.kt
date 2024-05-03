package com.example.maps2024

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task

class MainActivity : AppCompatActivity() {
    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                Toast.makeText( this,"thanks", Toast.LENGTH_SHORT).show()
                currentLocation()
            } else{
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                myDialog("please i want take location", { registerForActivityResult() })

            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerForActivityResult()
    }

    private fun registerForActivityResult() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                currentLocation()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.

            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
lateinit var fusedLocationProviderClient:FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
   fusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            currentLocation()
            Toast.makeText(this, "location", Toast.LENGTH_SHORT).show()
                Log.e("location","${it.latitude}")
        }
        fusedLocationProviderClient.requestLocationUpdates(
          locationRequest, LocationListener {
                Log.e("location","${it.latitude}")
            }, Looper.getMainLooper()
        )
    }

    override fun onResume() {
        super.onResume()
        getLastLocation()
    }
lateinit var locationRequest: LocationRequest
    private fun currentLocation(){
        locationRequest = LocationRequest.Builder(
            LocationRequest.PRIORITY_HIGH_ACCURACY,5000,
        ).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)


        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            getLastLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        200)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    private fun myDialog(message:String ,positiveButton:()->Unit) {
   val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage(message)
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton("no",DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
        })
        alertDialog.setPositiveButton("yes",DialogInterface.OnClickListener{
            dialog, which ->
            positiveButton.invoke()
            dialog.dismiss()
        })
        alertDialog.create()
        alertDialog.show()
    }
}