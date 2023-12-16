package com.example.afloat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
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
import androidx.compose.ui.Alignment
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
    private var floatingBallService: FloatingBallService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            floatingBallService = (service as FloatingBallService.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            floatingBallService = null
        }
    }

    @Composable
    fun Greeting() {
        val context = LocalContext.current
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Button(onClick = {
                if (Settings.canDrawOverlays(context)){
                    Toast.makeText(context,"已获取悬浮窗权限",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context,"没有悬浮窗权限,正在跳转设置",Toast.LENGTH_SHORT).show()
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.packageName
                    ))
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }){
                Text("悬浮窗权限")
            }

            Button(onClick = {
                val serviceIntent = Intent(context, FloatingBallService::class.java)
                startService(serviceIntent)
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            }){
                Text("开启悬浮窗")
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

}
