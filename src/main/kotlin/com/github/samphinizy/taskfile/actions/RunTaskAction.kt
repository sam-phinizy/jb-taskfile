package com.github.samphinizy.taskfile.actions

import com.github.samphinizy.taskfile.model.TaskModel
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.github.samphinizy.taskfile.services.TaskExecutionService
import com.github.samphinizy.taskfile.services.TaskfileDiscoveryService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.util.ui.EmptyIcon
import javax.swing.Icon

class RunTaskAction : AnAction("Run Task...", "Run a Taskfile task", null) {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        ApplicationManager.getApplication().executeOnPooledThread {
            val discoveryService = TaskfileDiscoveryService.getInstance(project)
            val taskfiles = discoveryService.getAllTaskfiles()
            val allTasks = collectAllTasks(taskfiles)
            
            ApplicationManager.getApplication().invokeLater {
                if (allTasks.isEmpty()) {
                    // Could show a notification that no tasks were found
                    return@invokeLater
                }
                
                val popup = JBPopupFactory.getInstance().createListPopup(
                    TaskListPopupStep(project, allTasks)
                )
                popup.showCenteredInCurrentWindow(project)
            }
        }
    }
    
    private fun collectAllTasks(taskfiles: List<TaskfileModel>): List<TaskInfo> {
        val tasks = mutableListOf<TaskInfo>()
        
        taskfiles.forEach { taskfile ->
            taskfile.tasks.forEach { task ->
                // Use the same logic as TaskfileNodeData for consistency
                val folderName = getTaskfileDisplayPath(taskfile)
                
                val displayText = if (task.description.isNullOrBlank()) {
                    "$folderName - ${task.name}"
                } else {
                    "$folderName - ${task.name} - ${task.description}"
                }
                
                tasks.add(TaskInfo(
                    task = task,
                    taskfile = taskfile,
                    displayText = displayText
                ))
            }
        }
        
        return tasks.sortedBy { it.displayText }
    }
    
    private fun getTaskfileDisplayPath(taskfile: TaskfileModel): String {
        val taskfilePath = taskfile.file.parent?.path ?: ""
        
        // Get just the directory name for display
        val folderName = if (taskfilePath.isNotEmpty()) {
            taskfilePath.substringAfterLast("/")
        } else {
            "root"
        }
        
        return if (folderName.isEmpty()) "root" else folderName
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}

data class TaskInfo(
    val task: TaskModel,
    val taskfile: TaskfileModel,
    val displayText: String
)

class TaskListPopupStep(
    private val project: Project,
    private val tasks: List<TaskInfo>
) : BaseListPopupStep<TaskInfo>("Select Task to Run", tasks) {
    
    override fun getTextFor(value: TaskInfo): String = value.displayText
    
    override fun getIconFor(value: TaskInfo): Icon? = EmptyIcon.ICON_16
    
    override fun onChosen(selectedValue: TaskInfo, finalChoice: Boolean): PopupStep<*>? {
        if (finalChoice) {
            val executionService = TaskExecutionService.getInstance(project)
            executionService.executeTask(selectedValue.task)
        }
        return null
    }
    
    override fun isSpeedSearchEnabled(): Boolean = true
}