package com.github.mucute.commposeiconconverter.converter

import com.github.mucute.commposeiconconverter.generator.KotlinCodeGenerator
import com.github.mucute.commposeiconconverter.parser.SvgParser
import java.io.File


/**
 * SVG到Kotlin代码转换器
 * 将SVG文件转换为包含路径数据的Kotlin源文件
 */
class SvgToImageVectorConverter {

    private val svgParser = SvgParser()
    private val codeGenerator = KotlinCodeGenerator()

    /**
     * 从SVG文件生成Kotlin文件
     */
    fun generateKotlinFileFromSvg(
        svgFile: File,
        outputFile: File,
        iconName: String = outputFile.nameWithoutExtension,
        packageName: String = "com.example.icons",
        iconParentClass: String? = null,
        iconStyle: String? = null
    ): Boolean {
        return try {
            val svgContent = svgFile.readText()
            val svgDocument = svgParser.parse(svgContent)
            val kotlinCode = codeGenerator.generateKotlinFile(svgDocument, iconName, packageName, iconParentClass, iconStyle)

            outputFile.parentFile?.mkdirs()
            outputFile.writeText(kotlinCode)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            println("转换失败: ${e.message}")
            false
        }
    }

    /**
     * 从SVG字符串生成Kotlin文件
     */
    fun generateKotlinFileFromString(
        svgContent: String,
        outputFile: File,
        iconName: String,
        packageName: String = "com.example.icons"
    ): Boolean {
        return try {
            val svgDocument = svgParser.parse(svgContent)
            val kotlinCode = codeGenerator.generateKotlinFile(svgDocument, iconName, packageName)

            outputFile.parentFile?.mkdirs()
            outputFile.writeText(kotlinCode)
            true
        } catch (e: Exception) {
            println("转换失败: ${e.message}")
            false
        }
    }

    /**
     * 批量转换目录中的SVG文件
     */
    fun generateKotlinFilesFromDirectory(
        inputDir: File,
        outputDir: File,
        packageName: String = "com.example.icons"
    ): List<File> {
        val generatedFiles = mutableListOf<File>()

        if (!inputDir.exists() || !inputDir.isDirectory) {
            println("输入目录不存在或不是目录: ${inputDir.absolutePath}")
            return generatedFiles
        }

        inputDir.listFiles { file -> file.extension.lowercase() == "svg" }?.forEach { svgFile ->
            val iconName = svgFile.nameWithoutExtension
            val outputFile = File(outputDir, "${iconName}.kt")

            if (generateKotlinFileFromSvg(svgFile, outputFile, iconName, packageName)) {
                generatedFiles.add(outputFile)
                println("已生成: ${outputFile.absolutePath}")
            } else {
                println("生成失败: ${svgFile.name}")
            }
        }

        return generatedFiles
    }

    /**
     * 生成Kotlin代码字符串（不保存到文件）
     */
    fun generateKotlinCode(
        svgContent: String,
        iconName: String,
        packageName: String = "com.example.icons"
    ): String {
        val svgDocument = svgParser.parse(svgContent)
        return codeGenerator.generateKotlinFile(svgDocument, iconName, packageName)
    }

    /**
     * 将字符串转换为驼峰命名
     */
    private fun String.toCamelCase(firstLowerCase: Boolean = false): String {
        val words = this.split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.isNotEmpty() }
            .map { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

        return if (words.isEmpty()) {
            ""
        } else {
            val first = if (firstLowerCase) words.first().replaceFirstChar { it.lowercase() } else words.first()
            first + words.drop(1).joinToString("")
        }
    }
}