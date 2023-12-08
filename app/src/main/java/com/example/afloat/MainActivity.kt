package com.example.afloat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.afloat.ui.theme.FloatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FloatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Greeting()
                }
            }
        }
    }
    private var floatingWindowService: FloatingWindowService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FloatingWindowService.LocalBinder
            floatingWindowService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            floatingWindowService = null
        }
    }

    @Composable
    fun Greeting() {
        val context = LocalContext.current
        Column {
            Button(onClick = {
                if (Settings.canDrawOverlays(context)){
                    Toast.makeText(context,"已获取悬浮窗权限",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,"没有悬浮窗权限",Toast.LENGTH_SHORT).show()
                }
            }){
                Text("悬浮窗权限")
            }

            Button(onClick = {
                val serviceIntent = Intent(context, FloatingWindowService::class.java)
                startService(serviceIntent)
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }){
                Text("开启悬浮窗")
            }


            Button(onClick = {
                floatingWindowService?.ttt()
            }){
                Text("点击以下")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

}
