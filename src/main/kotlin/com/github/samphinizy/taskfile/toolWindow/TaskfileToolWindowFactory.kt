package com.github.samphinizy.taskfile.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class TaskfileToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        println("DEBUG: Creating Taskfile tool window content")
        val taskfilePanel = TaskfileToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(taskfilePanel.getPanel(), "", false)
        toolWindow.contentManager.addContent(content)
        println("DEBUG: Taskfile tool window content created successfully")
    }
    
    override fun shouldBeAvailable(project: Project): Boolean = true
}