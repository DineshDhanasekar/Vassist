package com.vassist.app.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.AudioManager
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.vassist.app.R
import kotlinx.android.synthetic.main.floating_widget_layout.view.*
import java.io.*
import java.lang.RuntimeException


class FloatingWidgetView : ConstraintLayout, View.OnTouchListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
    )

    private var x: Int = 0
    private var y: Int = 0
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var clickStartTimer: Long = 0
    private val windowManager: WindowManager

    val mView =  View.inflate(context, R.layout.floating_widget_layout, this)

    init {
        setOnTouchListener(this)

        initViews()

        layoutParams.x = x
        layoutParams.y = y

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(this, layoutParams)
    }

    private fun initViews() {

        collapse_view.visibility = View.VISIBLE
        expand_view.visibility = View.GONE

        collapseIcon.setOnClickListener {
            collapse_view.visibility = View.VISIBLE
            expand_view.visibility = View.GONE
        }

        val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        volumeDownIcon.setOnClickListener {
            Toast.makeText(context, "clicked Volume Down widget", Toast.LENGTH_SHORT).show()
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
        volumeUpIcon.setOnClickListener {
            Toast.makeText(context, "clicked Volume Up widget", Toast.LENGTH_SHORT).show()
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        }
        screenShotIcon.setOnClickListener {
            Toast.makeText(context, "clicked screenShotIcon widget", Toast.LENGTH_SHORT).show()
            takeScreen()
        }
    }

    private fun takeScreenShot() {
        val rootView: View = mView.rootView.findViewById(android.R.id.content)
        store(getScreenShot(rootView)!!, "Screen.jpg")
    }

    companion object {
        private const val CLICK_DELTA = 200
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                clickStartTimer = System.currentTimeMillis()

                x = layoutParams.x
                y = layoutParams.y

                touchX = event.rawX
                touchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - clickStartTimer < CLICK_DELTA) {
                    collapse_view.visibility = View.GONE
                    expand_view.visibility = View.VISIBLE
                }
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = (x + event.rawX - touchX).toInt()
                layoutParams.y = (y + event.rawY - touchY).toInt()
                windowManager.updateViewLayout(this, layoutParams)
            }
        }
        return true
    }

    private fun getScreenShot(view: View): Bitmap? {
        val screenView = view.rootView
        screenView.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(screenView.drawingCache)
        screenView.isDrawingCacheEnabled = false
        return bitmap
    }

    private fun store(bm: Bitmap, fileName: String?) {
        val dirPath: String = Environment.getExternalStorageDirectory().absolutePath.toString() + "/Screenshots"
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fOut = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takeScreenShot(activity: Activity): Bitmap? {
        val view = activity.window.decorView
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val b1 = view.drawingCache
        val frame = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(frame)
        val statusBarHeight: Int = frame.top
        val displaymetrics = DisplayMetrics()
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics)
        val width = displaymetrics.widthPixels
        val height = displaymetrics.heightPixels
        val b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight)
        view.destroyDrawingCache()
        return b
    }

    fun takeScreen() {
        val string = runCommand("ls -l")
        Log.d("VAssist", "takeScreen: $string")
    }

    private fun runCommand(command:String): String? {
        return try {
            // Executes the command.
            val process = Runtime.getRuntime().exec(command)

            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            val reader = BufferedReader(
                    InputStreamReader(process.inputStream))
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuffer()
            while (reader.read(buffer).also { read = it } > 0) {
                output.append(buffer, 0, read)
            }
            reader.close()

            // Waits for the command to finish.
            process.waitFor()
            output.toString()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

}