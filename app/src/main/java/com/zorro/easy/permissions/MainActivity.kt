package com.zorro.easy.permissions

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textView = findViewById<TextView>(R.id.text_view)
        findViewById<Button>(R.id.button_view).setOnClickListener {
            PermissionRequester.from(this)
                .permissions(
                    PermissionGroup.PHONE, PermissionGroup.LOCATION,
                    PermissionGroup.SMS, PermissionGroup.NOTIFICATIONS,
                    PermissionGroup.CAMERA, PermissionGroup.APPS
                ).request { result ->
                    when (result) {
                        is PermissionResult.AllGranted -> {
                            textView.text = "已授予：${result.granted.joinToString("\n")}"
                            Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show()
                        }

                        is PermissionResult.Partial -> {
                            textView.text = "已授予：${result.granted.joinToString("\n")}\n未授予：${
                                result.denied.joinToString("\n")
                            }"
                            Toast.makeText(this, "部分成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }
}