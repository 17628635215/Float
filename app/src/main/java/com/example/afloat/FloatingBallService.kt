package com.example.afloat

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class FloatingBallService : Service(){
    private var isServiceRunning = false
    private val floatViewLifecycleOwner = FloatViewLifecycleOwner()
    private lateinit var windowManager: WindowManager
    private lateinit var contentView: View
    private lateinit var params: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        floatViewLifecycleOwner.onCreate()
        createFloatingWindow()
    }

    @Composable
    fun FloatingWindowContent() {
        val f = windowManager.currentWindowMetrics.bounds
        val rockerRadius by remember { mutableIntStateOf(15) }
        var chassisRadius by remember { mutableIntStateOf(rockerRadius) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        Box(Modifier.clip(CircleShape).size((chassisRadius*2).dp).background(Color.Unspecified)
            .pointerInput(Unit){
                detectDragGesturesAfterLongPress { _, dragAmount ->
                    params.x =
                        (params.x + dragAmount.x.toInt()).coerceIn(-f.width()/2..f.width()/2)
                    params.y =
                        (params.y + dragAmount.y.toInt()).coerceIn(-f.height()/2..f.height()/2)
                    windowManager.updateViewLayout(contentView,params)
                }
            },Alignment.Center) {
            Box(Modifier
                .offset {
                    val radius = sqrt(offsetX.pow(2) + offsetY.pow(2))
                    val radius1 = (chassisRadius - rockerRadius).dp.roundToPx()
                    if (radius > radius1){
                        IntOffset(((offsetX/radius) * radius1).roundToInt(), ((offsetY/radius) * radius1).roundToInt())
                    }else{
                        IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                    }
                }
                .clip(CircleShape)
                .size((rockerRadius*2).dp)
                .border(2.dp, Color(0x99ffffff), CircleShape)
                .background(Color.Unspecified)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            val radius = (2.5 * rockerRadius).roundToInt()
                            chassisRadius = radius
                            params.width = 2 * radius.dp.roundToPx()
                            params.height = 2 * radius.dp.roundToPx()
                            windowManager.updateViewLayout(contentView,params)
                        },
                        onDrag = { _, dragAmount ->
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        },
                        onDragEnd = {
                            if (abs(offsetX) > abs(offsetY)) {
                                if (offsetX > 0) {
                                    Toast.makeText(applicationContext, "你触发了右滑事件", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(applicationContext, "你触发了左滑事件", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (offsetY > 0) {
                                    Toast.makeText(applicationContext, "你触发了下滑事件", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(applicationContext, "你触发了上滑事件", Toast.LENGTH_SHORT).show()
                                }
                            }
                            windowManager.updateViewLayout(contentView,params)
                            params.width = 2 * rockerRadius.dp.roundToPx()
                            params.height = 2 * rockerRadius.dp.roundToPx()
                            chassisRadius = rockerRadius
                            offsetX = 0f
                            offsetY = 0f
                        }
                    )
                })
        }
    }

    private fun createFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            0, 0,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        params.javaClass.getField("privateFlags").apply {
            val noAnimFlag = params.javaClass.getField("PRIVATE_FLAG_NO_MOVE_ANIMATION").getInt(params)
            this.setInt(params, this.getInt(params) or noAnimFlag)
        }

        contentView = ComposeView(this).apply {
            setContent { FloatingWindowContent() }
            floatViewLifecycleOwner.attachToDecorView(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(!isServiceRunning){
            windowManager.addView(contentView, params)
            isServiceRunning = true
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(contentView)
        floatViewLifecycleOwner.onDestroy()
    }

    inner class LocalBinder : Binder() {
        fun getService(): FloatingBallService = this@FloatingBallService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
}

