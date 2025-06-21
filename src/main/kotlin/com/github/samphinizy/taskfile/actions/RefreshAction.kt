package com.github.samphinizy.taskfile.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class RefreshAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Taskfile") ?: return
        
        // The actual refresh logic is handled by the toolbar actions in TaskfileToolWindowPanel
        // This action is for global menu integration if needed
    }
}