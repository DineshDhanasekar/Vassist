package com.vassist.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.view.WindowManager
import com.vassist.app.receiver.ServiceRestartReceiver
import com.vassist.app.ui.FloatingWidgetView

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingWidgetView: FloatingWidgetView

    companion object {
        const val TAG = "FloatingWidgetService"

        fun startService(context: Context) {
            val startIntent = Intent(context, FloatingWidgetService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, FloatingWidgetService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatingWidgetView = FloatingWidgetView(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        if(::windowManager.isInitialized) windowManager.removeView(floatingWidgetView)
        val broadcastIntent = Intent(this, ServiceRestartReceiver::class.java)
        sendBroadcast(broadcastIntent)
    }


}
