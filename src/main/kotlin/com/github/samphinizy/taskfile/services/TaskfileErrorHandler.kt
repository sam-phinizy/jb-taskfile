package com.github.samphinizy.taskfile.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

@Service
class TaskfileErrorHandler(private val project: Project) {
    
    companion object {
        private const val NOTIFICATION_GROUP_ID = "Taskfile Plugin"
        
        fun getInstance(project: Project): TaskfileErrorHandler {
            return project.getService(TaskfileErrorHandler::class.java)
        }
    }
    
    fun handleParsingError(file: VirtualFile, error: Exception) {
        val message = "Failed to parse Taskfile: ${file.name}\nError: ${error.message}"
        showErrorNotification("Taskfile Parsing Error", message)
    }
    
    fun handleTaskExecutionError(taskName: String, error: Exception) {
        val message = when {
            error.message?.contains("task", ignoreCase = true) == true -> 
                "Task CLI not found. Please install Taskfile (https://taskfile.dev/installation/) and ensure it's in your PATH."
            error.message?.contains("Cannot run program", ignoreCase = true) == true ->
                "Task CLI not found. Please install Taskfile (https://taskfile.dev/installation/) and ensure it's in your PATH."
            error.message?.contains("No such file", ignoreCase = true) == true ->
                "Task CLI not found. Please install Taskfile (https://taskfile.dev/installation/) and ensure it's in your PATH."
            else -> "Failed to execute task '$taskName': ${error.message}"
        }
        
        showErrorNotification("Task Execution Error", message)
    }
    
    fun handleDiscoveryError(error: Exception) {
        val message = "Failed to discover Taskfiles in project: ${error.message}"
        showWarningNotification("Taskfile Discovery Warning", message)
    }
    
    fun handleMalformedTaskfile(file: VirtualFile, details: String = "Invalid YAML format") {
        val message = "Taskfile '${file.name}' has invalid format: $details"
        showWarningNotification("Malformed Taskfile", message)
    }
    
    fun showTaskNotFoundError(taskName: String) {
        val message = "Task '$taskName' not found. Please check your Taskfile configuration."
        showErrorNotification("Task Not Found", message)
    }
    
    private fun showErrorNotification(title: String, content: String) {
        showNotification(title, content, NotificationType.ERROR)
    }
    
    private fun showWarningNotification(title: String, content: String) {
        showNotification(title, content, NotificationType.WARNING)
    }
    
    private fun showInfoNotification(title: String, content: String) {
        showNotification(title, content, NotificationType.INFORMATION)
    }
    
    private fun showNotification(title: String, content: String, type: NotificationType) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(title, content, type)
        
        notification.notify(project)
    }
}