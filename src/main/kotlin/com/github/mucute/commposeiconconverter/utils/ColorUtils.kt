package com.github.mucute.commposeiconconverter.utils

/**
 * 颜色工具类，用于处理SVG颜色值
 */
object ColorUtils {
    
    /**
     * 解析SVG颜色值为十六进制字符串
     */
    fun parseColor(colorValue: String): String {
        return when {
            colorValue.startsWith("#") -> {
                // 十六进制颜色
                normalizeHexColor(colorValue)
            }
            colorValue.startsWith("rgb(") -> {
                // RGB颜色
                parseRgbColor(colorValue)
            }
            colorValue.startsWith("rgba(") -> {
                // RGBA颜色
                parseRgbaColor(colorValue)
            }
            colorValue in namedColors -> {
                // 命名颜色
                namedColors[colorValue]!!
            }
            else -> {
                // 默认黑色
                "0xFF000000"
            }
        }
    }
    
    /**
     * 标准化十六进制颜色值
     */
    private fun normalizeHexColor(hex: String): String {
        val cleanHex = hex.removePrefix("#")
        return when (cleanHex.length) {
            3 -> {
                // #RGB -> 0xFFRRGGBB
                val r = cleanHex[0]
                val g = cleanHex[1]
                val b = cleanHex[2]
                "0xFF$r$r$g$g$b$b"
            }
            6 -> "0xFF$cleanHex"
            8 -> "0x$cleanHex" // 包含alpha通道
            else -> "0xFF000000"
        }
    }
    
    /**
     * 解析RGB颜色值
     */
    private fun parseRgbColor(rgb: String): String {
        val values = rgb.removePrefix("rgb(").removeSuffix(")")
            .split(",")
            .map { it.trim().toIntOrNull() ?: 0 }
        
        if (values.size >= 3) {
            val r = values[0].coerceIn(0, 255)
            val g = values[1].coerceIn(0, 255)
            val b = values[2].coerceIn(0, 255)
            return "0xFF${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
        }
        return "0xFF000000"
    }
    
    /**
     * 解析RGBA颜色值
     */
    private fun parseRgbaColor(rgba: String): String {
        val values = rgba.removePrefix("rgba(").removeSuffix(")")
            .split(",")
            .map { it.trim() }
        
        if (values.size >= 4) {
            val r = (values[0].toIntOrNull() ?: 0).coerceIn(0, 255)
            val g = (values[1].toIntOrNull() ?: 0).coerceIn(0, 255)
            val b = (values[2].toIntOrNull() ?: 0).coerceIn(0, 255)
            val a = ((values[3].toDoubleOrNull() ?: 1.0) * 255).toInt().coerceIn(0, 255)
            return "0x${a.toString(16).padStart(2, '0')}${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
        }
        return "0xFF000000"
    }
    
    /**
     * 常见的命名颜色映射
     */
    private val namedColors = mapOf(
        "black" to "0xFF000000",
        "white" to "0xFFffffff",
        "red" to "0xFFff0000",
        "green" to "0xFF008000",
        "blue" to "0xFF0000ff",
        "yellow" to "0xFFffff00",
        "cyan" to "0xFF00ffff",
        "magenta" to "0xFFff00ff",
        "silver" to "0xFFc0c0c0",
        "gray" to "0xFF808080",
        "maroon" to "0xFF800000",
        "olive" to "0xFF808000",
        "lime" to "0xFF00ff00",
        "aqua" to "0xFF00ffff",
        "teal" to "0xFF008080",
        "navy" to "0xFF000080",
        "fuchsia" to "0xFFff00ff",
        "purple" to "0xFF800080",
        "orange" to "0xFFffa500",
        "pink" to "0xFFffc0cb",
        "brown" to "0xFFa52a2a",
        "gold" to "0xFFffd700",
        "violet" to "0xFFee82ee",
        "indigo" to "0xFF4b0082",
        "darkred" to "0xFF8b0000",
        "darkgreen" to "0xFF006400",
        "darkblue" to "0xFF00008b",
        "lightgray" to "0xFFd3d3d3",
        "lightgrey" to "0xFFd3d3d3",
        "darkgray" to "0xFFa9a9a9",
        "darkgrey" to "0xFFa9a9a9",
        "transparent" to "0x00000000"
    )
}