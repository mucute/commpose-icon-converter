package com.github.mucute.commposeiconconverter.utils

/**
 * SVG transform parser
 */
object TransformParser {
    
    /**
     * Transform matrix data class
     */
    data class Transform(
        val translateX: Float = 0f,
        val translateY: Float = 0f,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val rotation: Float = 0f,
        val skewX: Float = 0f,
        val skewY: Float = 0f
    )
    
    /**
     * Parse SVG transform string
     */
    fun parseTransform(transformString: String?): Transform {
        if (transformString.isNullOrBlank()) {
            return Transform()
        }
        
        var translateX = 0f
        var translateY = 0f
        var scaleX = 1f
        var scaleY = 1f
        var rotation = 0f
        
        // Match various transform functions
        val translateRegex = Regex("""translate\s*\(\s*([^,\s]+)(?:\s*,\s*([^)]+))?\s*\)""")
        val scaleRegex = Regex("""scale\s*\(\s*([^,\s]+)(?:\s*,\s*([^)]+))?\s*\)""")
        val rotateRegex = Regex("""rotate\s*\(\s*([^,\s]+)(?:\s*,\s*([^,\s]+)\s*,\s*([^)]+))?\s*\)""")
        val matrixRegex = Regex("""matrix\s*\(\s*([^,\s]+)\s*,\s*([^,\s]+)\s*,\s*([^,\s]+)\s*,\s*([^,\s]+)\s*,\s*([^,\s]+)\s*,\s*([^)]+)\s*\)""")
        
        // Parse translate
        translateRegex.findAll(transformString).forEach { match ->
            translateX += match.groupValues[1].toFloatOrNull() ?: 0f
            translateY += match.groupValues[2].takeIf { it.isNotBlank() }?.toFloatOrNull() ?: 0f
        }
        
        // Parse scale
        scaleRegex.findAll(transformString).forEach { match ->
            val sx = match.groupValues[1].toFloatOrNull() ?: 1f
            val sy = match.groupValues[2].takeIf { it.isNotBlank() }?.toFloatOrNull() ?: sx
            scaleX *= sx
            scaleY *= sy
        }
        
        // Parse rotate
        rotateRegex.findAll(transformString).forEach { match ->
            val angle = match.groupValues[1].toFloatOrNull() ?: 0f
            rotation += angle
            
            // If rotation center is specified
            if (match.groupValues[2].isNotBlank() && match.groupValues[3].isNotBlank()) {
                val cx = match.groupValues[2].toFloatOrNull() ?: 0f
                val cy = match.groupValues[3].toFloatOrNull() ?: 0f
                
                // rotate(angle, cx, cy) - rotate around specified point
                // Simplified handling here, only record angle
                // In actual implementation, need to consider rotation center
            }
        }
        
        // Parse matrix
        matrixRegex.findAll(transformString).forEach { match ->
            val a = match.groupValues[1].toFloatOrNull() ?: 1f
            val b = match.groupValues[2].toFloatOrNull() ?: 0f
            val c = match.groupValues[3].toFloatOrNull() ?: 0f
            val d = match.groupValues[4].toFloatOrNull() ?: 1f
            val e = match.groupValues[5].toFloatOrNull() ?: 0f
            val f = match.groupValues[6].toFloatOrNull() ?: 0f
            
            // matrix(a, b, c, d, e, f) represents:
            // [a c e]
            // [b d f]
            // [0 0 1]
            
            // Simplified handling here, extract translation and scale information
            translateX += e
            translateY += f
            scaleX *= a
            scaleY *= d
        }
        
        return Transform(
            translateX = translateX,
            translateY = translateY,
            scaleX = scaleX,
            scaleY = scaleY,
            rotation = rotation
        )
    }
    
    /**
     * Convert degrees to radians
     */
    fun degreesToRadians(degrees: Float): Float {
        return degrees * Math.PI.toFloat() / 180f
    }
    
    /**
     * Convert radians to degrees
     */
    fun radiansToDegrees(radians: Float): Float {
        return radians * 180f / Math.PI.toFloat()
    }
}