package com.github.nrfr.data

object PresetCarriers {
    data class CarrierPreset(
        val name: String,
        val displayName: String,
        val region: String,
        val mccMnc: String? = null  // 新增
    )

    val presets = listOf(
        // 中国大陆运营商
        CarrierPreset("中国移动", "China Mobile", "CN", "46000"),
        CarrierPreset("中国联通", "China Unicom", "CN", "46001"),
        CarrierPreset("中国电信", "China Telecom", "CN", "46003"),

        // 中国香港运营商
        CarrierPreset("中国移动香港", "CMHK", "HK", "45412"),
        CarrierPreset("香港电讯", "HKT", "HK", "45400"),
        CarrierPreset("3香港", "3HK", "HK", "45406"),
        CarrierPreset("SmarTone", "SmarTone", "HK", "45410"),

        // 中国澳门运营商
        CarrierPreset("澳门电讯", "CTM", "MO", "45500"),
        CarrierPreset("3澳门", "3 Macau", "MO", "45503"),

        // 中国台湾运营商
        CarrierPreset("中华电信", "Chunghwa Telecom", "TW", "46692"),
        CarrierPreset("台湾大哥大", "Taiwan Mobile", "TW", "46697"),
        CarrierPreset("远传电信", "FarEasTone", "TW", "46601"),

        // 日本运营商
        CarrierPreset("NTT docomo", "NTT docomo", "JP", "44010"),
        CarrierPreset("au", "au by KDDI", "JP", "44051"),
        CarrierPreset("Softbank", "Softbank", "JP", "44020"),
        CarrierPreset("Rakuten", "Rakuten Mobile", "JP", "44052"),

        // 韩国运营商
        CarrierPreset("SK Telecom", "SK Telecom", "KR", "45005"),
        CarrierPreset("KT", "KT Corporation", "KR", "45002"),
        CarrierPreset("LG U+", "LG U+", "KR", "45006"),

        // 美国运营商
        CarrierPreset("AT&T", "AT&T", "US", "310410"),
        CarrierPreset("T-Mobile", "T-Mobile USA", "US", "310260"),
        CarrierPreset("Verizon", "Verizon", "US", "310004"),
        CarrierPreset("Sprint", "Sprint", "US", "310120"),

        // 英国运营商
        CarrierPreset("EE", "EE", "GB", "23430"),
        CarrierPreset("O2", "O2 UK", "GB", "23410"),
        CarrierPreset("Three", "Three UK", "GB", "23420"),
        CarrierPreset("Vodafone", "Vodafone UK", "GB", "23415"),

        // 新加坡运营商
        CarrierPreset("Singtel", "Singtel", "SG", "52501"),
        CarrierPreset("StarHub", "StarHub", "SG", "52505"),
        CarrierPreset("M1", "M1", "SG", "52503"),

        // 马来西亚运营商
        CarrierPreset("Maxis", "Maxis", "MY", "50212"),
        CarrierPreset("Celcom", "Celcom", "MY", "50213"),
        CarrierPreset("Digi", "Digi", "MY", "50216"),
        CarrierPreset("U Mobile", "U Mobile", "MY", "50218"),

        // 泰国运营商
        CarrierPreset("AIS", "AIS", "TH", "52001"),
        CarrierPreset("DTAC", "DTAC", "TH", "52018"),
        CarrierPreset("True Move H", "True Move H", "TH", "52023"),

        // 越南运营商
        CarrierPreset("Viettel", "Viettel Mobile", "VN", "45204"),
        CarrierPreset("Vinaphone", "Vinaphone", "VN", "45202"),
        CarrierPreset("Mobifone", "Mobifone", "VN", "45201"),

        // 印度尼西亚运营商
        CarrierPreset("Telkomsel", "Telkomsel", "ID", "51010"),
        CarrierPreset("Indosat", "Indosat Ooredoo", "ID", "51021"),
        CarrierPreset("XL Axiata", "XL Axiata", "ID", "51011"),

        // 菲律宾运营商
        CarrierPreset("Globe", "Globe Telecom", "PH", "51502"),
        CarrierPreset("Smart", "Smart Communications", "PH", "51503"),
        CarrierPreset("DITO", "DITO Telecommunity", "PH", "51504"),

        // 印度运营商
        CarrierPreset("Jio", "Reliance Jio", "IN", "40493"),
        CarrierPreset("Airtel", "Bharti Airtel", "IN", "40405"),
        CarrierPreset("Vi", "Vodafone Idea", "IN", "40407"),

        // 澳大利亚运营商
        CarrierPreset("Telstra", "Telstra", "AU", "50501"),
        CarrierPreset("Optus", "Optus", "AU", "50502"),
        CarrierPreset("Vodafone", "Vodafone AU", "AU", "50503"),

        // 加拿大运营商
        CarrierPreset("Bell", "Bell Mobility", "CA", "302610"),
        CarrierPreset("Rogers", "Rogers Wireless", "CA", "302720"),
        CarrierPreset("Telus", "Telus Mobility", "CA", "302220"),

        // 德国运营商
        CarrierPreset("Telekom", "T-Mobile DE", "DE", "26201"),
        CarrierPreset("Vodafone", "Vodafone DE", "DE", "26202"),
        CarrierPreset("O2", "O2 DE", "DE", "26203"),

        // 法国运营商
        CarrierPreset("Orange", "Orange FR", "FR", "20801"),
        CarrierPreset("SFR", "SFR", "FR", "20810"),
        CarrierPreset("Free", "Free Mobile", "FR", "20815"),
        CarrierPreset("Bouygues", "Bouygues Telecom", "FR", "20820"),

        // 意大利运营商
        CarrierPreset("TIM", "Telecom Italia", "IT", "22201"),
        CarrierPreset("Vodafone", "Vodafone IT", "IT", "22210"),
        CarrierPreset("Wind Tre", "Wind Tre", "IT", "22288"),

        // 西班牙运营商
        CarrierPreset("Movistar", "Movistar", "ES", "21401"),
        CarrierPreset("Vodafone", "Vodafone ES", "ES", "21404"),
        CarrierPreset("Orange", "Orange ES", "ES", "21403"),

        // 俄罗斯运营商
        CarrierPreset("MTS", "MTS", "RU", "25001"),
        CarrierPreset("MegaFon", "MegaFon", "RU", "25002"),
        CarrierPreset("Beeline", "Beeline", "RU", "25099"),

        // 巴西运营商
        CarrierPreset("Vivo", "Vivo", "BR", "72410"),
        CarrierPreset("Claro", "Claro", "BR", "72405"),
        CarrierPreset("TIM", "TIM Brasil", "BR", "72403"),

        // 自定义选项
        CarrierPreset("自定义", "", "", null)
    )
}
