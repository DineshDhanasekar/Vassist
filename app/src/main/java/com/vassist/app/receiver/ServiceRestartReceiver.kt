package com.vassist.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vassist.app.service.FloatingWidgetService

class ServiceRestartReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(
            ServiceRestartReceiver::class.java.getSimpleName(),
            "Service Stops! Will Restarts if login"
        )
        context?.let { FloatingWidgetService.startService(it) }
    }

}
