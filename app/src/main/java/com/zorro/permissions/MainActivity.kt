package com.zorro.permissions

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zorro.easy.permissions.PermissionGroups
import com.zorro.easy.permissions.PermissionRequester
import com.zorro.easy.permissions.PermissionSupportRegistry
import com.zorro.permissions.databinding.ActivityMainBinding
import com.zorro.easy.permissions.model.PermissionEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        enableEdgeToEdge()
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        ViewCompat.setOnApplyWindowInsetsListener(vb.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //自定义权限组
        val vendorSpecial = PermissionGroups.custom(
            arrayOf("com.vendor.permission.SPECIAL_FEATURE"),
            getString(R.string.test_label)
        )
        // 注册自定义规则，
        PermissionSupportRegistry.registerChecker("com.vendor.permission.SPECIAL_FEATURE") { _ ->
            Build.VERSION.SDK_INT >= 33
        }
        vb.buttonView.setOnClickListener {
            PermissionRequester.from(this)
                .permissions(
                    PermissionGroups.PHONE, PermissionGroups.LOCATION,
                    PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
                    PermissionGroups.CAMERA, PermissionGroups.APPS,
                    vendorSpecial
                ).request { result ->
                    handleResult(result)
                }
        }
        val vendorSpecial2 = PermissionGroups.custom(
            arrayOf("com.test.permission.TEST"),
            R.string.test_label
        )
        vb.buttonAwait.setOnClickListener {
            lifecycleScope.launch {
                val result = PermissionRequester.from(this@MainActivity)
                    .permissions(
                        PermissionGroups.PHONE, PermissionGroups.LOCATION,
                        PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
                        PermissionGroups.CAMERA, PermissionGroups.APPS,
                        vendorSpecial2
                    ).await()
                handleResult(result)
            }
        }
        vb.buttonFlow.setOnClickListener {
//            lifecycleScope.launch {
//                PermissionRequester.from(this@MainActivity)
//                    .permissions(
//                        PermissionGroups.PHONE, PermissionGroups.LOCATION,
//                        PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
//                        PermissionGroups.CAMERA, PermissionGroups.APPS,
//                        vendorSpecial
//                    ).asFlow()
//                    .collect {
//                        handleResult(it)
//                    }
//            }
            PermissionRequester.from(this@MainActivity)
                .permissions(
                    PermissionGroups.PHONE, PermissionGroups.LOCATION,
                    PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
                    PermissionGroups.CAMERA, PermissionGroups.APPS
                ).asFlow()
                .onEach {
                    handleResult(it)
                }
                .launchIn(lifecycleScope)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.permissionResult.collect { result ->
                    handleResult(result)
                }
            }
        }
        vb.buttonVmFlow.setOnClickListener {
            PermissionRequester.from(this@MainActivity)
                .permissions(
                    PermissionGroups.PHONE, PermissionGroups.LOCATION,
                    PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
                    PermissionGroups.CAMERA, PermissionGroups.APPS
                )
                .setShowSettingDialog(false)
                .asFlowByViewModel(vm)
        }
    }

    /**
     * 授权结果处理
     */
    @SuppressLint("SetTextI18n")
    private fun handleResult(result: PermissionEvent) {
        when (result) {
            is PermissionEvent.AllGranted -> {
                vb.textView.text = "Granted\n${result.granted.joinToString("\n")}"
                Toast.makeText(this, "Success AllGranted", Toast.LENGTH_SHORT).show()
            }

            is PermissionEvent.PartialGranted -> {
                vb.textView.text =
                    "Granted\n${result.granted.joinToString("\n")}\nDenied\n${
                        result.denied.joinToString("\n")
                    }"
                Toast.makeText(this, "Partial Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy")
    }
}