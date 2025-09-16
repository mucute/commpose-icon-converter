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
import com.intellij.ide.util.PropertiesComponent
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
    
    // Icon parent class input field
    private val iconParentClassField = JBTextField()
    
    // Icon style selection box
    private val iconStyleComboBox = JComboBox(arrayOf("None", "Default", "Outline", "Bold", "Twotone", "Bulk", "Broken", "Linear"))
    
    // 批量转换相关字段
    private lateinit var batchSvgDirectoryField: JBTextField
    private lateinit var batchOutputDirectoryField: JBTextField
    private lateinit var batchPackageNameField: JBTextField
    private lateinit var batchIconParentClassField: JBTextField
    private lateinit var batchIconStyleComboBox: JComboBox<String>
    
    // Tab面板引用
    private lateinit var tabbedPane: JTabbedPane
    
    // File name processing options - removed checkboxes as they are now default behavior
    
    // 存储原始文件名
    private var originalFileName: String = ""
    
    // Properties key for storing last selected icon parent class
    private val ICON_PARENT_CLASS_KEY = "compose.icon.converter.last.parent.class"
    
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
        
        // 读取并设置上次保存的Icon Parent Class
        val lastIconParentClass = PropertiesComponent.getInstance().getValue(ICON_PARENT_CLASS_KEY, "")
        if (lastIconParentClass.isNotEmpty()) {
            iconParentClassField.text = lastIconParentClass
        }
        
        // Removed setupCheckBoxListeners() call as checkboxes are no longer needed
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        
        // 创建Tab面板
        tabbedPane = JTabbedPane()
        
        // 单个转换Tab
        val singleConvertPanel = createSingleConvertPanel()
        tabbedPane.addTab("Single Convert", singleConvertPanel)
        
        // 批量转换Tab
        val batchConvertPanel = createBatchConvertPanel()
        tabbedPane.addTab("Batch Convert", batchConvertPanel)
        
        panel.add(tabbedPane, BorderLayout.CENTER)
        panel.preferredSize = Dimension(600, 400)
        
        return panel
    }
    
    private fun createBatchSvgDirectoryPanel(batchSvgDirectoryField: JBTextField): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(batchSvgDirectoryField, BorderLayout.CENTER)
        
        val browseButton = JButton("Browse...")
        browseButton.addActionListener {
            val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
            descriptor.title = "Select SVG Directory"
            descriptor.description = "Select directory containing SVG files"
            
            val selectedFiles = FileChooser.chooseFiles(descriptor, project, null)
            if (selectedFiles.isNotEmpty()) {
                val selectedDir = selectedFiles[0]
                batchSvgDirectoryField.text = selectedDir.path
                // 自动设置输出目录为同一目录
                if (batchOutputDirectoryField.text.isEmpty()) {
                    batchOutputDirectoryField.text = selectedDir.path
                }
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    private fun createBatchOutputDirectoryPanel(batchOutputDirectoryField: JBTextField): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(batchOutputDirectoryField, BorderLayout.CENTER)
        
        val browseButton = JButton("Browse...")
        browseButton.addActionListener {
            val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
            descriptor.title = "Select Output Directory"
            descriptor.description = "Select directory for generated Kotlin files"
            
            val selectedFiles = FileChooser.chooseFiles(descriptor, project, null)
            if (selectedFiles.isNotEmpty()) {
                val selectedDir = selectedFiles[0]
                batchOutputDirectoryField.text = selectedDir.path
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    private fun createBatchIconParentClassPanel(batchIconParentClassField: JBTextField): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(batchIconParentClassField, BorderLayout.CENTER)
        
        val browseButton = JButton("Select Class")
        browseButton.addActionListener {
            try {
                val chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Select Icon Parent Class")
                chooser.showDialog()
                val selectedClass = chooser.selected
                if (selectedClass != null) {
                    batchIconParentClassField.text = selectedClass.qualifiedName ?: selectedClass.name
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(project, "Cannot open class chooser: ${e.message}", "Error")
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    private fun createSingleConvertPanel(): JComponent {
        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("SVG File:"), createSvgFilePanel(), 1, false)
            .addLabeledComponent(JBLabel("Icon Name:"), iconNameField, 1, false)
            .addLabeledComponent(JBLabel("Package Name:"), packageNameField, 1, false)
            .addLabeledComponent(JBLabel("Icon Parent Class:"), createIconParentClassPanel(), 1, false)
            .addLabeledComponent(JBLabel("Icon Style:"), iconStyleComboBox, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
            
        return formPanel
    }
    
    private fun createBatchConvertPanel(): JComponent {
        // 批量转换相关字段
        val batchSvgDirectoryField = JBTextField()
        val batchOutputDirectoryField = JBTextField()
        val batchPackageNameField = JBTextField()
        val batchIconParentClassField = JBTextField()
        val batchIconStyleComboBox = JComboBox(arrayOf("None", "Default", "Outline", "Bold", "Twotone", "Bulk", "Broken", "Linear"))
        
        // 设置默认值
        batchPackageNameField.text = packageNameField.text
        batchIconParentClassField.text = iconParentClassField.text
        batchIconStyleComboBox.selectedItem = iconStyleComboBox.selectedItem
        
        // 根据右键目录设置默认路径
        selectedDirectory?.let { dir ->
            batchSvgDirectoryField.text = dir.path
            batchOutputDirectoryField.text = if (dir.isDirectory) dir.path else dir.parent?.path ?: ""
        }
        
        val formPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("SVG Directory:"), createBatchSvgDirectoryPanel(batchSvgDirectoryField), 1, false)
            .addLabeledComponent(JBLabel("Output Directory:"), createBatchOutputDirectoryPanel(batchOutputDirectoryField), 1, false)
            .addLabeledComponent(JBLabel("Package Name:"), batchPackageNameField, 1, false)
            .addLabeledComponent(JBLabel("Icon Parent Class:"), createBatchIconParentClassPanel(batchIconParentClassField), 1, false)
            .addLabeledComponent(JBLabel("Icon Style:"), batchIconStyleComboBox, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
            
        // 存储批量转换字段的引用
        this.batchSvgDirectoryField = batchSvgDirectoryField
        this.batchOutputDirectoryField = batchOutputDirectoryField
        this.batchPackageNameField = batchPackageNameField
        this.batchIconParentClassField = batchIconParentClassField
        this.batchIconStyleComboBox = batchIconStyleComboBox
            
        return formPanel
    }
    
    private fun createSvgFilePanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(svgFilePathField, BorderLayout.CENTER)
        
        val browseButton = JButton("Browse...")
        browseButton.addActionListener {
            // 使用IntelliJ IDEA的文件选择器
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            descriptor.title = "Select SVG File"
            descriptor.description = "Select SVG file to convert"
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

    
    // Removed createFileNameOptionsPanel method as checkboxes are no longer needed
    
    private fun createIconParentClassPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(iconParentClassField, BorderLayout.CENTER)
        
        val browseButton = JButton("Select Class")
        browseButton.addActionListener {
            try {
                val chooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser("Select Icon Parent Class")
                chooser.showDialog()
                val selectedClass = chooser.selected
                if (selectedClass != null) {
                    iconParentClassField.text = selectedClass.qualifiedName ?: selectedClass.name
                }
            } catch (e: Exception) {
                Messages.showErrorDialog(project, "Cannot open class chooser: ${e.message}", "Error")
            }
        }
        panel.add(browseButton, BorderLayout.EAST)
        
        return panel
    }
    
    // Removed setupCheckBoxListeners method as checkboxes are no longer needed

    override fun doOKAction() {
        // 获取当前选中的Tab
        val selectedTabIndex = tabbedPane.selectedIndex
        
        if (selectedTabIndex == 0) {
            // 单个转换
            performSingleConversion()
        } else {
            // 批量转换
            performBatchConversion()
        }
    }
    
    private fun performSingleConversion() {
        val svgFilePath = svgFilePathField.text.trim()
        val iconName = iconNameField.text.trim()
        val packageName = packageNameField.text.trim()
        val iconParentClass = iconParentClassField.text.trim()
        val iconStyle = iconStyleComboBox.selectedItem as String
        
        if (svgFilePath.isEmpty()) {
            Messages.showErrorDialog(project, "Please select SVG file", "Error")
            return
        }
        
        if (iconName.isEmpty()) {
            Messages.showErrorDialog(project, "Please enter icon name", "Error")
            return
        }
        
        val inputFile = File(svgFilePath)
        if (!inputFile.exists() || !inputFile.extension.equals("svg", ignoreCase = true)) {
            Messages.showErrorDialog(project, "Please select valid SVG file", "Error")
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

            // 转换成功后保存当前选择的Icon Parent Class
            if (iconParentClass.isNotEmpty() && iconParentClass != "None") {
                PropertiesComponent.getInstance().setValue(ICON_PARENT_CLASS_KEY, iconParentClass)
            }

            super.doOKAction()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "Conversion failed: ${e.message}",
                "Error"
            )
        }
    }
    
    private fun performBatchConversion() {
        val svgDirectoryPath = batchSvgDirectoryField.text.trim()
        val outputDirectoryPath = batchOutputDirectoryField.text.trim()
        val packageName = batchPackageNameField.text.trim()
        val iconParentClass = batchIconParentClassField.text.trim()
        val iconStyle = batchIconStyleComboBox.selectedItem as String
        
        if (svgDirectoryPath.isEmpty()) {
            Messages.showErrorDialog(project, "Please select SVG directory", "Error")
            return
        }
        
        if (outputDirectoryPath.isEmpty()) {
            Messages.showErrorDialog(project, "Please select output directory", "Error")
            return
        }
        
        val svgDirectory = File(svgDirectoryPath)
        if (!svgDirectory.exists() || !svgDirectory.isDirectory) {
            Messages.showErrorDialog(project, "Please select valid SVG directory", "Error")
            return
        }
        
        val outputDirectory = File(outputDirectoryPath)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        
        // 获取目录中所有SVG文件
        val svgFiles = svgDirectory.listFiles { file ->
            file.isFile && file.extension.equals("svg", ignoreCase = true)
        }
        
        if (svgFiles.isNullOrEmpty()) {
            Messages.showErrorDialog(project, "No SVG files found in the selected directory", "Error")
            return
        }
        
        try {
            val converter = SvgToImageVectorConverter()
            var successCount = 0
            var failureCount = 0
            val failedFiles = mutableListOf<String>()
            
            // 批量转换所有SVG文件
            for (svgFile in svgFiles) {
                try {
                    val iconName = processFileName(svgFile.nameWithoutExtension)
                    val outputFile = File(outputDirectory, "$iconName.kt")
                    
                    converter.generateKotlinFileFromSvg(
                        svgFile,
                        outputFile,
                        iconName,
                        packageName,
                        iconParentClass.takeIf { it.isNotEmpty() && it != "None" },
                        iconStyle.takeIf { it != "None" }
                    )
                    
                    successCount++
                } catch (e: Exception) {
                    failureCount++
                    failedFiles.add("${svgFile.name}: ${e.message}")
                }
            }
            
            // 显示转换结果
            val message = buildString {
                append("Batch conversion completed!\n")
                append("Successfully converted: $successCount files\n")
                if (failureCount > 0) {
                    append("Failed: $failureCount files\n\n")
                    append("Failed files:\n")
                    failedFiles.forEach { append("- $it\n") }
                }
            }
            
            if (failureCount == 0) {
                Messages.showInfoMessage(project, message, "Batch Conversion Success")
            } else {
                Messages.showWarningDialog(project, message, "Batch Conversion Completed with Errors")
            }
            
            // 转换成功后保存当前选择的Icon Parent Class
            if (iconParentClass.isNotEmpty() && iconParentClass != "None") {
                PropertiesComponent.getInstance().setValue(ICON_PARENT_CLASS_KEY, iconParentClass)
            }

            super.doOKAction()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "Batch conversion failed: ${e.message}",
                "Error"
            )
        }
    }
    
    private fun processFileName(fileName: String): String {
        // Apply default processing: remove special characters and convert to Pascal case
        var processed = fileName.replace(Regex("[^a-zA-Z0-9]"), "")
        processed = processed.toPascalCase()
        return processed
    }
    
    private fun String.toPascalCase(): String {
        val words = split("_", "-", " ")
        if (words.isEmpty()) return this
        
        // If there's only one word (no separators), return it as is to preserve original casing
        if (words.size == 1) return this
        
        return words.mapNotNull { word ->
            if (word.isEmpty()) null else word.lowercase().replaceFirstChar { it.uppercase() }
        }.joinToString("")
    }
    
    /**
     * Get package name from directory
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
            // If getting package name fails, return null to use default value
            null
        }
    }
}