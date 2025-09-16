package com.github.mucute.commposeiconconverter

import com.github.mucute.commposeiconconverter.converter.SvgToImageVectorConverter
import org.junit.Test
import java.io.File

/**
 * SVG到ImageVector转换器的测试类
 */
class SvgToImageVectorConverterTest {
    
    @Test
    fun testConvertSimpleSvg() {
        val simpleSvg = """
            <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7v10c0 5.55 3.84 10 9 11 1.04.18 2.07.18 3.11 0 5.16-1 9-5.45 9-11V7l-10-5z" fill="#000000"/>
            </svg>
        """.trimIndent()
        
        val imageVector = SvgToImageVectorConverter.convertFromString(simpleSvg, "TestIcon")
        
        // 验证基本属性
        assert(imageVector.name == "TestIcon")
        assert(imageVector.defaultWidth.value == 24f)
        assert(imageVector.defaultHeight.value == 24f)
        assert(imageVector.viewportWidth == 24f)
        assert(imageVector.viewportHeight == 24f)
        
        println("转换成功: ${imageVector.name}")
    }
    
    @Test
    fun testConvertSvgWithMultipleElements() {
        val complexSvg = """
            <svg width="48" height="48" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                <rect x="10" y="10" width="28" height="28" rx="4" fill="#FF5722"/>
                <circle cx="24" cy="24" r="8" fill="#FFFFFF"/>
                <line x1="16" y1="32" x2="32" y2="16" stroke="#000000" stroke-width="2"/>
            </svg>
        """.trimIndent()
        
        val imageVector = SvgToImageVectorConverter.convertFromString(complexSvg, "ComplexIcon")
        
        // 验证基本属性
        assert(imageVector.name == "ComplexIcon")
        assert(imageVector.defaultWidth.value == 48f)
        assert(imageVector.defaultHeight.value == 48f)
        
        println("复杂SVG转换成功: ${imageVector.name}")
    }
    
    @Test
    fun testConvertSvgWithGroup() {
        val groupSvg = """
            <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
                <g transform="translate(8,8)">
                    <rect x="0" y="0" width="16" height="16" fill="#2196F3"/>
                    <circle cx="8" cy="8" r="4" fill="#FFFFFF"/>
                </g>
            </svg>
        """.trimIndent()
        
        val imageVector = SvgToImageVectorConverter.convertFromString(groupSvg, "GroupIcon")
        
        println("分组SVG转换成功: ${imageVector.name}")
    }
    
    @Test
    fun testGenerateKotlinCode() {
        val simpleSvg = """
            <svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L2 7v10c0 5.55 3.84 10 9 11 1.04.18 2.07.18 3.11 0 5.16-1 9-5.45 9-11V7l-10-5z" fill="#000000"/>
            </svg>
        """.trimIndent()
        
        val imageVector = SvgToImageVectorConverter.convertFromString(simpleSvg, "ShieldIcon")
        val kotlinCode = SvgToImageVectorConverter.generateKotlinCode(
            imageVector, 
            "ShieldIcon", 
            "com.example.icons"
        )
        
        println("生成的Kotlin代码:")
        println(kotlinCode)
        
        // 验证生成的代码包含必要的元素
        assert(kotlinCode.contains("package com.example.icons"))
        assert(kotlinCode.contains("val ShieldIcon: ImageVector"))
        assert(kotlinCode.contains("defaultWidth = 24.0.dp"))
        assert(kotlinCode.contains("defaultHeight = 24.0.dp"))
    }
    
    @Test
    fun testBatchConversion() {
        // 创建测试SVG文件
        val testDir = File("test_svgs")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }
        
        val svgFiles = listOf(
            "icon1.svg" to """
                <svg width="24" height="24" viewBox="0 0 24 24">
                    <circle cx="12" cy="12" r="10" fill="#FF0000"/>
                </svg>
            """.trimIndent(),
            
            "icon2.svg" to """
                <svg width="24" height="24" viewBox="0 0 24 24">
                    <rect x="4" y="4" width="16" height="16" fill="#00FF00"/>
                </svg>
            """.trimIndent()
        )
        
        // 创建测试文件
        val createdFiles = svgFiles.map { (filename, content) ->
            val file = File(testDir, filename)
            file.writeText(content)
            file
        }
        
        try {
            // 批量转换
            val results = SvgToImageVectorConverter.convertMultiple(createdFiles)
            
            println("批量转换结果:")
            results.forEach { (name, imageVector) ->
                println("- $name: ${imageVector.defaultWidth} x ${imageVector.defaultHeight}")
            }
            
            assert(results.size == 2)
            assert(results.containsKey("Icon1"))
            assert(results.containsKey("Icon2"))
            
        } finally {
            // 清理测试文件
            createdFiles.forEach { it.delete() }
            testDir.delete()
        }
    }
}