package com.zorro.easy.permissions

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.button_view).setOnClickListener {
            PermissionRequester.from(
                this,
                PermissionGroup.PHONE, PermissionGroup.LOCATION,
                PermissionGroup.SMS, PermissionGroup.NOTIFICATIONS,
                PermissionGroup.CAMERA, PermissionGroup.APPS
            ).onDenied {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }.onGranted {
                Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show()
            }.request()
        }
    }
}