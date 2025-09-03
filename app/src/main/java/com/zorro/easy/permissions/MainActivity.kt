package com.zorro.easy.permissions

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zorro.easy.permissions.databinding.ActivityMainBinding
import com.zorro.easy.permissions.model.PermissionEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

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
        //自定义权限组
        val vendorSpecial = PermissionGroup.Custom(
            arrayOf("com.vendor.permission.SPECIAL_FEATURE"),
            "厂商特殊权限"
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
        vb.buttonAwait.setOnClickListener {
            lifecycleScope.launch {
                val result = PermissionRequester.from(this@MainActivity)
                    .permissions(
                        PermissionGroups.PHONE, PermissionGroups.LOCATION,
                        PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
                        PermissionGroups.CAMERA, PermissionGroups.APPS,
                        vendorSpecial
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
//                    .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
//                    .collect {
//                        handleResult(it)
//                    }
//            }
            PermissionRequester.from(this@MainActivity)
                .permissions(
                    PermissionGroups.PHONE, PermissionGroups.LOCATION,
                    PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
                    PermissionGroups.CAMERA, PermissionGroups.APPS,
                    vendorSpecial
                ).asFlow()
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
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
                .asFlowByViewModel(vm)
        }
    }

    /**
     * 授权结果处理
     */
    private fun handleResult(result: PermissionEvent) {
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