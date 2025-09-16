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
    
    // Icon parent class input field
    private val iconParentClassField = JBTextField()
    
    // Icon style selection box
    private val iconStyleComboBox = JComboBox(arrayOf("None", "Default", "Outline", "Bold", "Twotone", "Bulk", "Broken", "Linear"))
    
    // File name processing options
    private val removeSpecialCharsCheckBox = JCheckBox("Remove Special Characters", true)
    private val camelCaseCheckBox = JCheckBox("Camel Case", true)
    
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
            .addLabeledComponent(JBLabel("Icon Parent Class:"), createIconParentClassPanel(), 1, false)
            .addLabeledComponent(JBLabel("Icon Style:"), iconStyleComboBox, 1, false)
            .addLabeledComponent(JBLabel("File Name Processing:"), createFileNameOptionsPanel(), 1, false)
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
            
            Messages.showInfoMessage(
                project,
                "Conversion completed! files",
                "Success"
            )
            
            super.doOKAction()
            
        } catch (e: Exception) {
            Messages.showErrorDialog(
                project,
                "Conversion failed: ${e.message}",
                "Error"
            )
        }
    }
    
    private fun processFileName(fileName: String): String {
        var processed = fileName
        
        // If both checkboxes are unchecked, return original filename
        if (!removeSpecialCharsCheckBox.isSelected && !camelCaseCheckBox.isSelected) {
            return processed
        }
        
        // First perform camel case conversion (before removing special characters)
        if (camelCaseCheckBox.isSelected) {
            processed = processed.toCamelCase()
        }
        
        // Then remove special characters
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