# 使用MVI框架封装的权限请求框架

## 外部调用

1. 支持在Activity和Fragment中使用；
2. 支持单个权限、多个权限和权限组申请；
3. 支持基础调用、协程await()、Flow方式，支持DSL链式调用；

## 权限申请实现逻辑

1. 权限申请主要逻辑放在无UI的Fragment中，此Fragment在申请过程中横竖屏切换无异常；
2. 申请结果判断优先级为：当权限全部授予时返回成功回调，回调中包含授予的权限列表；
   当部分授予时返回失败回调，回调中分别包含授予的和未授予的权限列表；
   最后当未授予的权限都为永久拒绝的权限，弹出DialogFragment提示框引导去设置页手动授予，提示文案根据权限动态生成；
3. 永久拒绝的权限跳转到设置页后，从设置页再回到应用后，继续检查权限，检查逻辑和顺序同第2点；

## 扩展使用

### 自定义权限组

```kotlin
//自定义权限组
val vendorSpecial = PermissionGroup.Custom(
    arrayOf("com.vendor.permission.SPECIAL_FEATURE"),
    "厂商特殊权限"
)
```

### 扩展支持规则

```kotlin
// 注册自定义规则，
PermissionSupportRegistry.registerChecker("com.vendor.permission.SPECIAL_FEATURE") { _ ->
    Build.VERSION.SDK_INT >= 33
}
```

## 申请权限

### 1) 回调 DSL

```kotlin
PermissionRequester.from(this)
    .permissions(
        PermissionGroups.PHONE, PermissionGroups.LOCATION,
        PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
        PermissionGroups.CAMERA, PermissionGroups.APPS,
        vendorSpecial
    ).request { result ->
        handleResult(result)
    }
```

### 2) 协程 await

```kotlin
lifecycleScope.launch {
    val result = PermissionRequester.from(this@MainActivity)
        .permissions(
            PermissionGroups.PHONE, PermissionGroups.LOCATION,
            PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
            PermissionGroups.CAMERA, PermissionGroups.APPS,
            vendorSpecial
        ).await()
    // handle result
    handleResult(result)
}
```

### 3) Flow

```kotlin
 lifecycleScope.launch {
    PermissionRequester.from(this@MainActivity)
        .permissions(
            PermissionGroups.PHONE, PermissionGroups.LOCATION,
            PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
            PermissionGroups.CAMERA, PermissionGroups.APPS,
            vendorSpecial
        ).asFlow()
        .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
        .collect {
            handleResult(it)
        }
}
//或
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
```

### 4) 使用ViewModel+Flow方式

```kotlin
//step 1
class MainViewModel : PermissionViewModel()
//step 2
private val vm: MainViewModel by viewModels()
//step 3
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        vm.permissionResult.collect { result ->
            handleResult(result)
        }
    }
}
//step 4
PermissionRequester.from(this@MainActivity)
    .permissions(
        PermissionGroups.PHONE, PermissionGroups.LOCATION,
        PermissionGroups.SMS, PermissionGroups.NOTIFICATIONS,
        PermissionGroups.CAMERA, PermissionGroups.APPS
    )
    .asFlowByViewModel(vm)
```

## 授权结果处理

```kotlin
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
```
