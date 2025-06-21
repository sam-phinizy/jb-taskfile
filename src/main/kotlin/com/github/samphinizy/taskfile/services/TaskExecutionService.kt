package com.github.samphinizy.taskfile.services

import com.github.samphinizy.taskfile.model.TaskModel
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

@Service
class TaskExecutionService(private val project: Project) {
    
    companion object {
        fun getInstance(project: Project): TaskExecutionService {
            return project.getService(TaskExecutionService::class.java)
        }
    }
    
    fun executeTask(task: TaskModel) {
        try {
            val workingDirectory = determineWorkingDirectory(task)
            val commandLine = createCommandLine(task, workingDirectory)
            
            val processHandler = ProcessHandlerFactory.getInstance().createProcessHandler(commandLine)
            val console = createConsole(task)
            
            console.attachToProcess(processHandler)
            
            val descriptor = RunContentDescriptor(
                console as ExecutionConsole,
                processHandler,
                console.component,
                "Task: ${task.name}"
            )
            
            RunContentManager.getInstance(project).showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)
            processHandler.startNotify()
            
        } catch (e: ExecutionException) {
            val errorHandler = TaskfileErrorHandler.getInstance(project)
            errorHandler.handleTaskExecutionError(task.name, e)
        }
    }
    
    private fun determineWorkingDirectory(task: TaskModel): File {
        // For now, use project base directory
        // In a full implementation, this would be the directory containing the taskfile
        return File(project.basePath ?: ".")
    }
    
    private fun createCommandLine(task: TaskModel, workingDirectory: File): GeneralCommandLine {
        return GeneralCommandLine().apply {
            this.workDirectory = workingDirectory
            exePath = "task"
            addParameter(task.name)
        }
    }
    
    private fun createConsole(task: TaskModel): ConsoleView {
        val console = com.intellij.execution.impl.ConsoleViewImpl(project, true)
        
        console.print("Executing task: ${task.name}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        if (!task.description.isNullOrBlank()) {
            console.print("Description: ${task.description}\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        }
        console.print("\n", ConsoleViewContentType.SYSTEM_OUTPUT)
        
        return console
    }
}