package com.github.nrfr.manager

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.github.nrfr.model.SimCardInfo
import java.io.BufferedReader
import java.io.InputStreamReader

object CarrierConfigManager {
    private const val TAG = "CarrierConfigManager"
    private const val SU_PATH = "/system/bin/su"
    private const val PREFS_NAME = "nrfr_config"
    private const val KEY_ORIGINAL_SIM_NUMERIC = "original_sim_numeric"
    private const val KEY_ORIGINAL_SIM_ISO = "original_sim_iso"
    private const val KEY_ORIGINAL_OPERATOR_NUMERIC = "original_operator_numeric"
    private const val KEY_ORIGINAL_OPERATOR_ISO = "original_operator_iso"
    private const val KEY_LAST_SUB_ID = "last_sub_id"
    private const val KEY_LAST_COUNTRY_CODE = "last_country_code"
    private const val KEY_LAST_CARRIER_NAME = "last_carrier_name"
    private const val KEY_LAST_MCC_MNC = "last_mcc_mnc"
    private const val KEY_SAVED = "saved"

    private fun getProp(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "getprop $key"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            process.waitFor()
            reader.close()
            if (result.isNullOrEmpty()) null else result
        } catch (e: Exception) {
            null
        }
    }

    private fun getSettingsValue(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "settings get global $key"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            process.waitFor()
            reader.close()
            if (result == "null" || result.isNullOrEmpty()) null else result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get settings value for $key", e)
            null
        }
    }

    fun getSimCards(context: Context): List<SimCardInfo> {
        Log.d(TAG, "getSimCards called")
        val simCards = mutableListOf<SimCardInfo>()

        try {
            val subIds = listOf(2, 1)
            for ((index, subId) in subIds.withIndex()) {
                val carrierName = getCarrierNameBySubId(context, subId)
                val config = getCurrentConfig(subId)
                simCards.add(SimCardInfo(index + 1, subId, carrierName, config))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get SIM cards", e)
        }

        return simCards
    }

    private fun getCurrentConfig(subId: Int): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val countryCode = getSettingsValue("sim_country_iso_override_$subId")
        if (!countryCode.isNullOrEmpty()) {
            result["国家码"] = countryCode.uppercase()
        }
        val carrierName = getSettingsValue("sim_carrier_name_override_$subId")
        if (!carrierName.isNullOrEmpty()) {
            result["运营商名称"] = carrierName
        }
        return result
    }

    private fun getCarrierNameBySubId(context: Context, subId: Int): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            ?: return ""
        return try {
            val tm = telephonyManager.createForSubscriptionId(subId)
            tm.networkOperatorName ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun createBootScript(mccMnc: String, countryCode: String) {
        try {
            // 创建配置文件
            val configContent = "MCC_MNC=\"$mccMnc\"\nCOUNTRY_CODE=\"$countryCode\""
            Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "echo '$configContent' > /data/local/tmp/nrfr_config"))

            val script = """#!/system/bin/sh
# Nrfr 开机自启脚本
sleep 10
if [ -f /data/local/tmp/nrfr_config ]; then
    . /data/local/tmp/nrfr_config
    if [ ! -z "${'$'}MCC_MNC" ] && [ ! -z "${'$'}COUNTRY_CODE" ]; then
        /system/bin/setprop gsm.sim.operator.numeric "${'$'}MCC_MNC,${'$'}MCC_MNC"
        /system/bin/setprop gsm.sim.operator.iso-country "${'$'}COUNTRY_CODE,${'$'}COUNTRY_CODE"
        /system/bin/setprop gsm.operator.numeric "${'$'}MCC_MNC"
        /system/bin/setprop gsm.operator.iso-country "${'$'}COUNTRY_CODE"
    fi
fi
"""
            // 1. Magisk / KernelSU / SukiSU 服务目录
            val serviceDirs = listOf(
                "/data/adb/service.d",
                "/data/adb/ksu/service.d",
                "/data/adb/modules/nrfr"
            )
            for (dir in serviceDirs) {
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "mkdir -p $dir"))
                if (dir.contains("modules")) {
                    Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "echo '$script' > $dir/service.sh"))
                    Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "chmod 755 $dir/service.sh"))
                    val moduleProp = "id=nrfr\nversion=1\nversionCode=1\nauthor=Nrfr\ndescription=Auto apply SIM config"
                    Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "echo '$moduleProp' > $dir/module.prop"))
                } else {
                    Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "echo '$script' > $dir/nrfr.sh"))
                    Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "chmod 755 $dir/nrfr.sh"))
                }
            }

            // 2. init.d 兼容目录
            val initDDirs = listOf(
                "/system/etc/init.d",
                "/etc/init.d",
                "/data/etc/init.d",
                "/vendor/etc/init.d"
            )
            for (dir in initDDirs) {
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "mount -o remount,rw /system 2>/dev/null"))
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "mkdir -p $dir"))
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "echo '$script' > $dir/99nrfr"))
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "chmod 755 $dir/99nrfr"))
            }

            // 3. 创建独立脚本供用户手动使用
            val manualScript = """#!/system/bin/sh
$script
"""
            Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "echo '$manualScript' > /data/local/tmp/nrfr_manual.sh"))
            Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "chmod 755 /data/local/tmp/nrfr_manual.sh"))

            Log.d(TAG, "Boot script created in multiple locations")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create boot script", e)
        }
    }

    private fun removeBootScript() {
        try {
            // 清理配置文件
            Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "rm -f /data/local/tmp/nrfr_config"))
            Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "rm -f /data/local/tmp/nrfr_manual.sh"))

            // 清理服务目录
            val serviceDirs = listOf(
                "/data/adb/service.d/nrfr.sh",
                "/data/adb/ksu/service.d/nrfr.sh",
                "/data/adb/modules/nrfr/service.sh",
                "/data/adb/modules/nrfr/module.prop"
            )
            for (file in serviceDirs) {
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "rm -f $file"))
            }

            // 清理 init.d 目录
            val initDFiles = listOf(
                "/system/etc/init.d/99nrfr",
                "/etc/init.d/99nrfr",
                "/data/etc/init.d/99nrfr",
                "/vendor/etc/init.d/99nrfr"
            )
            for (file in initDFiles) {
                Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "rm -f $file"))
            }

            // 清理模块目录
            Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "rm -rf /data/adb/modules/nrfr"))

            Log.d(TAG, "Boot script removed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove boot script", e)
        }
    }

    fun setCarrierConfig(context: Context, subId: Int, countryCode: String?, carrierName: String?, mccMnc: String?) {
        try {
            val hasRoot = hasRequiredPermissions(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val process = Runtime.getRuntime().exec(SU_PATH)
            val dos = java.io.DataOutputStream(process.outputStream)

            // settings 修改
            if (!countryCode.isNullOrEmpty() && countryCode.length == 2) {
                dos.writeBytes("settings put global sim_country_iso_override_$subId ${countryCode.lowercase()}\n")
            }
            if (!carrierName.isNullOrEmpty()) {
                dos.writeBytes("settings put global sim_carrier_name_override_$subId \"$carrierName\"\n")
            }

            // setprop 修改（只有 root 才执行）
            if (hasRoot && !mccMnc.isNullOrEmpty() && !countryCode.isNullOrEmpty()) {
                // 首次执行时保存原始值到 SharedPreferences
                if (!prefs.contains(KEY_ORIGINAL_SIM_NUMERIC)) {
                    val simNumeric = getProp("gsm.sim.operator.numeric")
                    val simIso = getProp("gsm.sim.operator.iso-country")
                    val operatorNumeric = getProp("gsm.operator.numeric")
                    val operatorIso = getProp("gsm.operator.iso-country")

                    prefs.edit().apply {
                        if (simNumeric != null) putString(KEY_ORIGINAL_SIM_NUMERIC, simNumeric)
                        if (simIso != null) putString(KEY_ORIGINAL_SIM_ISO, simIso)
                        if (operatorNumeric != null) putString(KEY_ORIGINAL_OPERATOR_NUMERIC, operatorNumeric)
                        if (operatorIso != null) putString(KEY_ORIGINAL_OPERATOR_ISO, operatorIso)
                        putBoolean(KEY_SAVED, true)
                        apply()
                    }
                }

                dos.writeBytes("setprop gsm.sim.operator.numeric \"$mccMnc,$mccMnc\"\n")
                dos.writeBytes("setprop gsm.sim.operator.iso-country \"$countryCode,$countryCode\"\n")
                dos.writeBytes("setprop gsm.operator.numeric \"$mccMnc\"\n")
                dos.writeBytes("setprop gsm.operator.iso-country \"$countryCode\"\n")

                // 保存配置到 SharedPreferences
                prefs.edit().apply {
                    putInt(KEY_LAST_SUB_ID, subId)
                    putString(KEY_LAST_COUNTRY_CODE, countryCode)
                    putString(KEY_LAST_CARRIER_NAME, carrierName)
                    putString(KEY_LAST_MCC_MNC, mccMnc)
                    putBoolean(KEY_SAVED, true)
                    apply()
                }

                // 创建开机自启脚本
                createBootScript(mccMnc, countryCode)
            }

            dos.writeBytes("exit\n")
            dos.flush()
            process.waitFor()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set carrier config", e)
        }
    }

    fun resetCarrierConfig(context: Context, subId: Int) {
        try {
            val hasRoot = hasRequiredPermissions(context)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val process = Runtime.getRuntime().exec(SU_PATH)
            val dos = java.io.DataOutputStream(process.outputStream)

            // 恢复 settings
            dos.writeBytes("settings delete global sim_country_iso_override_$subId\n")
            dos.writeBytes("settings delete global sim_carrier_name_override_$subId\n")

            // 恢复 setprop 原始值（只有 root 才执行）
            if (hasRoot) {
                val simNumeric = prefs.getString(KEY_ORIGINAL_SIM_NUMERIC, null)
                val simIso = prefs.getString(KEY_ORIGINAL_SIM_ISO, null)
                val operatorNumeric = prefs.getString(KEY_ORIGINAL_OPERATOR_NUMERIC, null)
                val operatorIso = prefs.getString(KEY_ORIGINAL_OPERATOR_ISO, null)

                if (simNumeric != null) {
                    dos.writeBytes("setprop gsm.sim.operator.numeric \"$simNumeric\"\n")
                }
                if (simIso != null) {
                    dos.writeBytes("setprop gsm.sim.operator.iso-country \"$simIso\"\n")
                }
                if (operatorNumeric != null) {
                    dos.writeBytes("setprop gsm.operator.numeric \"$operatorNumeric\"\n")
                }
                if (operatorIso != null) {
                    dos.writeBytes("setprop gsm.operator.iso-country \"$operatorIso\"\n")
                }

                // 清除保存的配置
                prefs.edit().clear().apply()

                // 删除开机自启脚本
                removeBootScript()
            }

            dos.writeBytes("exit\n")
            dos.flush()
            process.waitFor()
            Log.d(TAG, "resetCarrierConfig completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset carrier config", e)
        }
    }

    fun applySavedConfig(context: Context, subId: Int, countryCode: String, carrierName: String?, mccMnc: String) {
        try {
            val process = Runtime.getRuntime().exec(SU_PATH)
            val dos = java.io.DataOutputStream(process.outputStream)

            dos.writeBytes("setprop gsm.sim.operator.numeric \"$mccMnc,$mccMnc\"\n")
            dos.writeBytes("setprop gsm.sim.operator.iso-country \"$countryCode,$countryCode\"\n")
            dos.writeBytes("setprop gsm.operator.numeric \"$mccMnc\"\n")
            dos.writeBytes("setprop gsm.operator.iso-country \"$countryCode\"\n")

            dos.writeBytes("exit\n")
            dos.flush()
            process.waitFor()
            Log.d(TAG, "applySavedConfig completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply saved config", e)
        }
    }

    fun hasRequiredPermissions(context: Context): Boolean {
        val hasRoot = try {
            val process = Runtime.getRuntime().exec(arrayOf(SU_PATH, "-c", "exit"))
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }

        if (hasRoot) return true

        return try {
            rikka.shizuku.Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }
}