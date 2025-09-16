package com.github.mucute.commposeiconconverter.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.github.mucute.commposeiconconverter.converter.SvgToImageVectorConverter
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiManager
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import javax.swing.*

class ConvertToComposeIconDialog(
    private val project: Project,
    private val svgFile: VirtualFile?,
    private val selectedDirectory: VirtualFile? = null
) : DialogWrapper(project) {

    private val iconNameField = JBTextField()
    private val outputPathField = JBTextField()
    private val packageNameField = JBTextField()
    private val svgFilePathField = JBTextField()
    
    // 图标父类输入框
    private val iconParentClassField = JBTextField()
    
    // 图标样式选择框
    private val iconStyleComboBox = JComboBox(arrayOf("None", "Default", "Outline", "Bold", "Twotone", "Bulk", "Broken", "Linear"))
    
    // 文件名处理选项
    private val removeSpecialCharsCheckBox = JCheckBox("去除特殊符号", true)
    private val camelCaseCheckBox = JCheckBox("驼峰命名", true)
    
    // 存储原始文件名
    private var originalFileName: String = ""
    
    init {
        title = "Convert to Compose Icon"
        init()
        
        // 设置默认值
        svgFile?.let {
            originalFileName = it.nameWithoutExtension
            iconNameField.text = processFileName(originalFileName)
            svgFilePathField.text = it.path
        }
        
        // 根据右键选择的目录自动获取包名
        packageNameField.text = getPackageNameFromDirectory(selectedDirectory) ?: "com.example.icons"
        
        // 根据右键目录设置默认输出路径
        outputPathField.text = when {
            selectedDirectory?.isDirectory == true -> selectedDirectory.path
            selectedDirectory?.parent?.isDirectory == true -> selectedDirectory.parent.path
            else -> project.basePath + "/src/main/kotlin"
        }
        
        // 添加复选框监听器
        setupCheckBoxListeners()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        
        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("SVG File:"), createSvgFilePanel(), 1, false)
            .addLabeledComponent(JBLabel("Icon Name:"), iconNameField, 1, false)
            .addLabeledComponent(JBLabel("Package Name:"), packageNameField, 1, false)
            .addLabeledComponent(JBLabel("图标父类:"), createIconParentClassPanel(), 1, false)
            .addLabeledComponent(JBLabel("图标样式:"), iconStyleComboBox, 1, false)
            .addLabeledComponent(JBLabel("文件名处理:"), createFileNameOptionsPanel(), 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
            
        panel.add(formPanel, BorderLayout.CENTER)
        panel.preferredSize = Dimension(500, 320)
        
        return panel
    }
    
    private fun createSvgFilePanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(svgFilePathField, BorderLayout.CENTER)
        
        val browseButton = JButton("Browse...")
        browseButton.addActionListener {
            // 使用IntelliJ IDEA的文件选择器
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            descriptor.title = "选择SVG文件"
            descriptor.description = "选择要转换的SVG文件"
            descriptor.withFileFilter { file -> file.extension?.lowercase() == "svg" }
            
            val selectedFiles = FileChooser.chooseFiles(descriptor, project, null)
            if (selectedFiles.isNotEmpty()) {
                val selectedFile = selectedFiles[0]
                svgFilePathField.text = selectedFile.path
                // 自动设置图标名称
                if (iconNameField.text.isEmpty()) {
                    originalFileName = selectedFile.nameWithoutExtension
                    iconNameField.text = processFileName(originalFileName)
                }
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    private fun createOutputPathPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(outputPathField, BorderLayout.CENTER)
        
        val browseButton = JButton("Browse...")
        browseButton.addActionListener {
            val fileChooser = JFileChooser()
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileChooser.currentDirectory = File(project.basePath ?: "")
            
            if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                outputPathField.text = fileChooser.selectedFile.absolutePath
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    private fun createFileNameOptionsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(removeSpecialCharsCheckBox)
        panel.add(Box.createHorizontalStrut(10))
        panel.add(camelCaseCheckBox)
        return panel
    }
    
    private fun createIconParentClassPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(iconParentClassField, BorderLayout.CENTER)
        
        val browseButton = JButton("选择类")
        browseButton.addActionListener {
            try {
                val chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("选择图标父类")
                chooser.showDialog()
                val selectedClass = chooser.selected
                if (selectedClass != null) {
                    iconParentClassField.text = selectedClass.qualifiedName ?: selectedClass.name
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(project, "无法打开类选择器: ${e.message}", "错误")
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    private fun setupCheckBoxListeners() {
        val updateIconName = {
            if (originalFileName.isNotEmpty()) {
                iconNameField.text = processFileName(originalFileName)
            }
        }
        
        removeSpecialCharsCheckBox.addActionListener { updateIconName() }
        camelCaseCheckBox.addActionListener { updateIconName() }
    }

    override fun doOKAction() {
        val svgFilePath = svgFilePathField.text.trim()
        val iconName = iconNameField.text.trim()
        val packageName = packageNameField.text.trim()
        val iconParentClass = iconParentClassField.text.trim()
        val iconStyle = iconStyleComboBox.selectedItem as String
        
        if (svgFilePath.isEmpty()) {
            Messages.showErrorDialog(project, "请选择SVG文件", "错误")
            return
        }
        
        if (iconName.isEmpty()) {
            Messages.showErrorDialog(project, "请输入图标名称", "错误")
            return
        }
        
        val inputFile = File(svgFilePath)
        if (!inputFile.exists() || !inputFile.extension.equals("svg", ignoreCase = true)) {
            Messages.showErrorDialog(project, "请选择有效的SVG文件", "错误")
            return
        }
        
        try {
            val converter = SvgToImageVectorConverter()
            
            // 使用右键目录作为输出路径
            val outputPath = when {
                selectedDirectory?.isDirectory == true -> selectedDirectory.path
                selectedDirectory?.parent?.isDirectory == true -> selectedDirectory.parent.path
                else -> project.basePath + "/src/main/kotlin"
            }
            val outputDir = File(outputPath)
            
            // 根据复选框选项处理图标名称
            val processedIconName = processFileName(iconName)
            val outputFile = File(outputDir, "$processedIconName.kt")
            
            // 执行转换，传递图标类名和样式
            val generatedFiles = converter.generateKotlinFileFromSvg(
                inputFile, 
                outputFile, 
                processedIconName, 
                packageName,
                iconParentClass.takeIf { it.isNotEmpty() && it != "None" },
                iconStyle.takeIf { it != "None" }
            )
            
            Messages.showInfoMessage(
                project,
                "转换完成！个文件",
                "成功"
            )
            
            super.doOKAction()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "转换失败: ${e.message}",
                "错误"
            )
        }
    }
    
    private fun processFileName(fileName: String): String {
        var processed = fileName
        
        // 如果两个复选框都没勾选，直接返回原始文件名
        if (!removeSpecialCharsCheckBox.isSelected && !camelCaseCheckBox.isSelected) {
            return processed
        }
        
        // 先进行驼峰命名转换（在去除特殊符号之前）
        if (camelCaseCheckBox.isSelected) {
            processed = processed.toCamelCase()
        }
        
        // 然后去除特殊符号
        if (removeSpecialCharsCheckBox.isSelected) {
            processed = processed.replace(Regex("[^a-zA-Z0-9]"), "")
        }
        
        return processed
    }
    
    private fun String.toCamelCase(): String {
        val words = split("_", "-", " ")
        if (words.isEmpty()) return this
        
        return words.mapIndexed { index, word ->
            if (index == 0) {
                word.lowercase()
            } else {
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }.joinToString("")
    }
    
    /**
     * 根据目录获取包名
     */
    private fun getPackageNameFromDirectory(directory: VirtualFile?): String? {
        if (directory == null) return null
        
        return try {
            val psiManager = PsiManager.getInstance(project)
            val psiDirectory = psiManager.findDirectory(directory)
            
            if (psiDirectory != null) {
                val javaDirectoryService = JavaDirectoryService.getInstance()
                val psiPackage = javaDirectoryService.getPackage(psiDirectory)
                psiPackage?.qualifiedName
            } else {
                null
            }
        } catch (e: Exception) {
            // 如果获取包名失败，返回null使用默认值
            null
        }
    }
}