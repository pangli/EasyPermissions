package com.zorro.easy.permissions

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zorro.easy.permissions.databinding.ActivityMainBinding
import com.zorro.easy.permissions.model.PermissionEvent
import com.zorro.easy.permissions.viewmodel.PermissionViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    private val vm: PermissionViewModel by viewModels()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        ViewCompat.setOnApplyWindowInsetsListener(vb.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        vb.buttonView.setOnClickListener {
//            request()
            PermissionRequester.from(this)
                .permissions(
                    PermissionGroup.PHONE, PermissionGroup.LOCATION,
                    PermissionGroup.SMS, PermissionGroup.NOTIFICATIONS,
                    PermissionGroup.CAMERA, PermissionGroup.APPS
                ).request { result ->
                    when (result) {
                        is PermissionEvent.AllGranted -> {
                            vb.textView.text = "已授予\n${result.granted.joinToString("\n")}"
                            Toast.makeText(this, "成功", Toast.LENGTH_SHORT).show()
                        }

                        is PermissionEvent.Partial -> {
                            vb.textView.text =
                                "已授予\n${result.granted.joinToString("\n")}\n未授予\n${
                                    result.denied.joinToString("\n")
                                }"
                            Toast.makeText(this, "部分成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                vm.result.collect { result ->
//                    when (result) {
//                        is PermissionResult.AllGranted -> {
//                            vb.textView.text = "已授予\n${result.granted.joinToString("\n")}"
//                            Toast.makeText(this@MainActivity, "成功", Toast.LENGTH_SHORT).show()
//                        }
//
//                        is PermissionResult.Partial -> {
//                            vb.textView.text =
//                                "已授予\n${result.granted.joinToString("\n")}\n未授予\n${
//                                    result.denied.joinToString("\n")
//                                }"
//                            Toast.makeText(this@MainActivity, "部分成功", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            }
//        }
    }

    private fun request() {
        PermissionRequester.from(this@MainActivity)
            .permissions(
                PermissionGroup.PHONE, PermissionGroup.LOCATION,
                PermissionGroup.SMS, PermissionGroup.NOTIFICATIONS,
                PermissionGroup.CAMERA, PermissionGroup.APPS
            )
            .asFlowByViewModel()
    }
}