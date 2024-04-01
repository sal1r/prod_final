package com.example.swipecsat

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build

fun checkPermissions(
    parent: ActivityResultCaller,
    context: Context
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val req =
                parent.registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

            req.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
