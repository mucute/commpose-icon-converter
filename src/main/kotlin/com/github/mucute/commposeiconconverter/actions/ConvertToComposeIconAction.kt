package com.github.mucute.commposeiconconverter.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.github.mucute.commposeiconconverter.ui.ConvertToComposeIconDialog

class ConvertToComposeIconAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        
        // 在New菜单中始终显示此操作
        e.presentation.isEnabledAndVisible = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        // 获取右键目录作为默认输出路径
        val selectedDirectory = e.getData(CommonDataKeys.VIRTUAL_FILE)
        
        // 显示转换对话框，传递选中的目录
        val dialog = ConvertToComposeIconDialog(project, null, selectedDirectory)
        dialog.show()
    }
}