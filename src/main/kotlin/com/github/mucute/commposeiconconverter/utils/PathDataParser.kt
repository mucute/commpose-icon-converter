package com.github.mucute.commposeiconconverter.utils

object PathDataParser {
    
    fun parsePathData(pathData: String): List<String> {
        val cleanedData = pathData.replace(Regex("\\s+"), " ").trim()
        val tokens = tokenize(cleanedData)
        val commands = mutableListOf<String>()
        
        // 当前位置跟踪（用于相对命令）
        var currentX = 0.0
        var currentY = 0.0
        var lastMoveX = 0.0
        var lastMoveY = 0.0
        
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            
            when (token.uppercase()) {
                "M" -> {
                    val isRelative = token == "m"
                    val x = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 2)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative && commands.isNotEmpty()) {
                        commands.add("moveToRelative(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("moveTo(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    lastMoveX = currentX
                    lastMoveY = currentY
                    println("Moved to ($currentX, $currentY)")
                    i += 3
                }
                
                "L" -> {
                    val isRelative = token == "l"
                    val x = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 2)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("lineToRelative(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("lineTo(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    i += 3
                }
                
                "H" -> {
                    val isRelative = token == "h"
                    val x = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("horizontalLineToRelative(${formatFloat(x)}f)")
                        currentX += x
                    } else {
                        commands.add("horizontalLineTo(${formatFloat(x)}f)")
                        currentX = x
                    }
                    
                    i += 2
                }
                
                "V" -> {
                    val isRelative = token == "v"
                    val y = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("verticalLineToRelative(${formatFloat(y)}f)")
                        currentY += y
                    } else {
                        commands.add("verticalLineTo(${formatFloat(y)}f)")
                        currentY = y
                    }
                    
                    i += 2
                }
                
                "C" -> {
                    val isRelative = token == "c"
                    val x1 = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    val y1 = tokens.getOrNull(i + 2)?.toDoubleOrNull() ?: 0.0
                    val x2 = tokens.getOrNull(i + 3)?.toDoubleOrNull() ?: 0.0
                    val y2 = tokens.getOrNull(i + 4)?.toDoubleOrNull() ?: 0.0
                    val x = tokens.getOrNull(i + 5)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 6)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("curveToRelative(${formatFloat(x1)}f, ${formatFloat(y1)}f, ${formatFloat(x2)}f, ${formatFloat(y2)}f, ${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("curveTo(${formatFloat(x1)}f, ${formatFloat(y1)}f, ${formatFloat(x2)}f, ${formatFloat(y2)}f, ${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    i += 7
                }
                
                "S" -> {
                    val isRelative = token == "s"
                    val x2 = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    val y2 = tokens.getOrNull(i + 2)?.toDoubleOrNull() ?: 0.0
                    val x = tokens.getOrNull(i + 3)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 4)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("reflectiveCurveToRelative(${formatFloat(x2)}f, ${formatFloat(y2)}f, ${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("reflectiveCurveTo(${formatFloat(x2)}f, ${formatFloat(y2)}f, ${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    i += 5
                }
                
                "Q" -> {
                    val isRelative = token == "q"
                    val x1 = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    val y1 = tokens.getOrNull(i + 2)?.toDoubleOrNull() ?: 0.0
                    val x = tokens.getOrNull(i + 3)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 4)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("quadToRelative(${formatFloat(x1)}f, ${formatFloat(y1)}f, ${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("quadTo(${formatFloat(x1)}f, ${formatFloat(y1)}f, ${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    i += 5
                }
                
                "T" -> {
                    val isRelative = token == "t"
                    val x = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 2)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("reflectiveQuadToRelative(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("reflectiveQuadTo(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    i += 3
                }
                
                "A" -> {
                    // Arc命令比较复杂，简化为lineTo
                    val isRelative = token == "a"
                    val x = tokens.getOrNull(i + 6)?.toDoubleOrNull() ?: 0.0
                    val y = tokens.getOrNull(i + 7)?.toDoubleOrNull() ?: 0.0
                    
                    if (isRelative) {
                        commands.add("lineToRelative(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX += x
                        currentY += y
                    } else {
                        commands.add("lineTo(${formatFloat(x)}f, ${formatFloat(y)}f)")
                        currentX = x
                        currentY = y
                    }
                    
                    i += 8
                }
                
                "Z", "z" -> {
                    commands.add("close()")
                    currentX = lastMoveX
                    currentY = lastMoveY
                    i += 1
                }
                
                else -> {
                    // 跳过无法识别的token
                    i += 1
                }
            }
        }
        
        return commands
    }
    
    private fun tokenize(pathData: String): List<String> {
        val tokens = mutableListOf<String>()
        val regex = Regex("[MmLlHhVvCcSsQqTtAaZz]|[-+]?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?")
        
        regex.findAll(pathData).forEach { matchResult ->
            tokens.add(matchResult.value)
        }
        
        return tokens
    }
    
    private fun formatFloat(value: Double): String {
        return if (value == value.toInt().toDouble()) {
            value.toInt().toString()
        } else {
            String.format("%.2f", value).trimEnd('0').trimEnd('.')
        }
    }
}