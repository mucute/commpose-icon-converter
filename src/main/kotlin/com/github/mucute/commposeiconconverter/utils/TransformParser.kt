package com.github.mucute.commposeiconconverter.utils

/**
 * SVG变换解析器
 */
class TransformParser {
    
    /**
     * 变换矩阵数据类
     */
    data class Transform(
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val translateX: Float = 0f,
        val translateY: Float = 0f,
        val rotation: Float = 0f,
        val skewX: Float = 0f,
        val skewY: Float = 0f
    )
    
    /**
     * 解析SVG变换字符串
     */
    fun parseTransform(transformStr: String?): Transform? {
        if (transformStr.isNullOrBlank()) return null
        
        var result = Transform()
        
        // 匹配各种变换函数
        val transformRegex = Regex("(\\w+)\\s*\\(([^)]+)\\)")
        val matches = transformRegex.findAll(transformStr)
        
        for (match in matches) {
            val function = match.groupValues[1]
            val params = parseParameters(match.groupValues[2])
            
            result = when (function.lowercase()) {
                "translate" -> applyTranslate(result, params)
                "scale" -> applyScale(result, params)
                "rotate" -> applyRotate(result, params)
                "skewx" -> applySkewX(result, params)
                "skewy" -> applySkewY(result, params)
                "matrix" -> applyMatrix(result, params)
                else -> result
            }
        }
        
        return result
    }
    
    private fun parseParameters(paramStr: String): List<Float> {
        return paramStr.split(Regex("[,\\s]+"))
            .filter { it.isNotBlank() }
            .mapNotNull { it.toFloatOrNull() }
    }
    
    private fun applyTranslate(transform: Transform, params: List<Float>): Transform {
        return when (params.size) {
            1 -> transform.copy(
                translateX = transform.translateX + params[0],
                translateY = transform.translateY
            )
            2 -> transform.copy(
                translateX = transform.translateX + params[0],
                translateY = transform.translateY + params[1]
            )
            else -> transform
        }
    }
    
    private fun applyScale(transform: Transform, params: List<Float>): Transform {
        return when (params.size) {
            1 -> transform.copy(
                scaleX = transform.scaleX * params[0],
                scaleY = transform.scaleY * params[0]
            )
            2 -> transform.copy(
                scaleX = transform.scaleX * params[0],
                scaleY = transform.scaleY * params[1]
            )
            else -> transform
        }
    }
    
    private fun applyRotate(transform: Transform, params: List<Float>): Transform {
        return when (params.size) {
            1 -> transform.copy(rotation = transform.rotation + params[0])
            3 -> {
                // rotate(angle, cx, cy) - 围绕指定点旋转
                // 这里简化处理，只记录角度
                transform.copy(rotation = transform.rotation + params[0])
            }
            else -> transform
        }
    }
    
    private fun applySkewX(transform: Transform, params: List<Float>): Transform {
        return if (params.isNotEmpty()) {
            transform.copy(skewX = transform.skewX + params[0])
        } else transform
    }
    
    private fun applySkewY(transform: Transform, params: List<Float>): Transform {
        return if (params.isNotEmpty()) {
            transform.copy(skewY = transform.skewY + params[0])
        } else transform
    }
    
    private fun applyMatrix(transform: Transform, params: List<Float>): Transform {
        // matrix(a, b, c, d, e, f)
        // 这里简化处理，提取平移和缩放信息
        return if (params.size == 6) {
            transform.copy(
                scaleX = transform.scaleX * params[0],
                scaleY = transform.scaleY * params[3],
                translateX = transform.translateX + params[4],
                translateY = transform.translateY + params[5]
            )
        } else transform
    }
    
    /**
     * 将角度转换为弧度
     */
    fun degreesToRadians(degrees: Float): Float {
        return degrees * Math.PI.toFloat() / 180f
    }
    
    /**
     * 将弧度转换为角度
     */
    fun radiansToDegrees(radians: Float): Float {
        return radians * 180f / Math.PI.toFloat()
    }
}