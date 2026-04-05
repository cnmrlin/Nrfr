package com.github.nrfr.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.nrfr.data.CountryPresets
import com.github.nrfr.data.PresetCarriers
import com.github.nrfr.manager.CarrierConfigManager
import com.github.nrfr.model.SimCardInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onShowAbout: () -> Unit) {
    val context = LocalContext.current
    var selectedSimCard by remember { mutableStateOf<SimCardInfo?>(null) }
    var selectedCountryCode by remember { mutableStateOf("") }
    var customCountryCode by remember { mutableStateOf("") }
    var isCustomCountryCode by remember { mutableStateOf(false) }
    var selectedCarrier by remember { mutableStateOf<PresetCarriers.CarrierPreset?>(null) }
    var customCarrierName by remember { mutableStateOf("") }
    var isSimCardMenuExpanded by remember { mutableStateOf(false) }
    var isCountryCodeMenuExpanded by remember { mutableStateOf(false) }
    var isCarrierMenuExpanded by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val simCards = remember(context, refreshTrigger) { CarrierConfigManager.getSimCards(context) }

    LaunchedEffect(simCards, selectedSimCard) {
        if (selectedSimCard != null) {
            selectedSimCard = simCards.find { it.slot == selectedSimCard?.slot }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nrfr") },
                actions = {
                    IconButton(onClick = onShowAbout) {
                        Icon(Icons.Default.Info, contentDescription = "关于")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SIM卡选择
            ExposedDropdownMenuBox(
                expanded = isSimCardMenuExpanded,
                onExpandedChange = { isSimCardMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSimCard?.let { "SIM ${it.slot} (${it.carrierName})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择SIM卡") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSimCardMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isSimCardMenuExpanded,
                    onDismissRequest = { isSimCardMenuExpanded = false }
                ) {
                    simCards.forEach { simCard ->
                        DropdownMenuItem(
                            text = { Text("SIM ${simCard.slot} (${simCard.carrierName})") },
                            onClick = {
                                selectedSimCard = simCard
                                isSimCardMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // 当前配置卡片
            selectedSimCard?.let { simCard ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("当前配置", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (simCard.currentConfig.isEmpty()) {
                            Text("无覆盖配置", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            simCard.currentConfig.forEach { (key, value) ->
                                Text("$key: $value", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // 国家码选择
            ExposedDropdownMenuBox(
                expanded = isCountryCodeMenuExpanded,
                onExpandedChange = { isCountryCodeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = when {
                        isCustomCountryCode -> "自定义"
                        selectedCountryCode.isEmpty() -> ""
                        else -> CountryPresets.countries.find { it.code == selectedCountryCode }
                            ?.let { "${it.name} (${it.code})" } ?: selectedCountryCode
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择国家码") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCountryCodeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isCountryCodeMenuExpanded,
                    onDismissRequest = { isCountryCodeMenuExpanded = false }
                ) {
                    CountryPresets.countries.forEach { countryInfo ->
                        DropdownMenuItem(
                            text = { Text("${countryInfo.name} (${countryInfo.code})") },
                            onClick = {
                                selectedCountryCode = countryInfo.code
                                isCustomCountryCode = false
                                isCountryCodeMenuExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("自定义") },
                        onClick = {
                            isCustomCountryCode = true
                            isCountryCodeMenuExpanded = false
                        }
                    )
                }
            }

            // 自定义国家码输入
            if (isCustomCountryCode) {
                val focusManager = LocalFocusManager.current
                OutlinedTextField(
                    value = customCountryCode,
                    onValueChange = {
                        if (it.length <= 2 && it.all { char -> char.isLetter() }) {
                            customCountryCode = it.uppercase()
                            selectedCountryCode = it.uppercase()
                        }
                    },
                    label = { Text("自定义国家码 (2位字母)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 运营商选择
            ExposedDropdownMenuBox(
                expanded = isCarrierMenuExpanded,
                onExpandedChange = { isCarrierMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCarrier?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择运营商") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCarrierMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isCarrierMenuExpanded,
                    onDismissRequest = { isCarrierMenuExpanded = false }
                ) {
                    // 分组显示运营商
                    PresetCarriers.presets
                        .groupBy { it.region }
                        .forEach { (region, carriers) ->
                            if (region.isNotEmpty()) {
                                val regionName = CountryPresets.countries.find { it.code == region }?.name ?: region
                                Text(
                                    regionName,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                carriers.forEach { carrier ->
                                    DropdownMenuItem(
                                        text = { Text(carrier.displayName) },
                                        onClick = {
                                            selectedCarrier = carrier
                                            customCarrierName = carrier.displayName
                                            isCarrierMenuExpanded = false
                                        }
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }

                    // 自定义选项
                    PresetCarriers.presets
                        .filter { it.region.isEmpty() }
                        .forEach { carrier ->
                            DropdownMenuItem(
                                text = { Text(carrier.displayName) },
                                onClick = {
                                    selectedCarrier = carrier
                                    customCarrierName = carrier.displayName
                                    isCarrierMenuExpanded = false
                                }
                            )
                        }
                }
            }

            // 自定义运营商名称输入
            if (selectedCarrier?.name == "自定义") {
                OutlinedTextField(
                    value = customCarrierName,
                    onValueChange = { customCarrierName = it },
                    label = { Text("自定义运营商名称") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 按钮
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = {
                        selectedSimCard?.let { simCard ->
                            try {
                                CarrierConfigManager.resetCarrierConfig(context, simCard.subId)
                                Toast.makeText(context, "设置已还原", Toast.LENGTH_SHORT).show()
                                refreshTrigger++
                                selectedCountryCode = ""
                                selectedCarrier = null
                                customCarrierName = ""
                            } catch (e: Exception) {
                                Toast.makeText(context, "还原失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedSimCard != null
                ) {
                    Text("还原设置")
                }

                Button(
                    onClick = {
                        selectedSimCard?.let { simCard ->
                            try {
                                val countryCode = if (isCustomCountryCode) customCountryCode.takeIf { it.length == 2 } else selectedCountryCode
                                val carrierName = if (selectedCarrier?.name == "自定义") {
                                    customCarrierName.takeIf { it.isNotEmpty() }
                                } else {
                                    selectedCarrier?.displayName
                                }
                                val mccMnc = selectedCarrier?.mccMnc
                                CarrierConfigManager.setCarrierConfig(context, simCard.subId, countryCode, carrierName, mccMnc)
                                Toast.makeText(context, "设置已保存", Toast.LENGTH_SHORT).show()
                                refreshTrigger++
                            } catch (e: Exception) {
                                Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedSimCard != null && (selectedCountryCode.isNotEmpty() || isCustomCountryCode || selectedCarrier != null)
                ) {
                    Text("保存生效")
                }
            }
        }
    }
}