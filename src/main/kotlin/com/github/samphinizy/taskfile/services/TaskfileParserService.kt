package com.github.samphinizy.taskfile.services

import com.github.samphinizy.taskfile.model.TaskModel
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VirtualFile
import org.yaml.snakeyaml.Yaml
import java.io.InputStreamReader

@Service
class TaskfileParserService {
    
    companion object {
        fun getInstance(): TaskfileParserService {
            return ApplicationManager.getApplication()
                .getService(TaskfileParserService::class.java)
        }
    }
    
    private val yaml = Yaml()
    
    fun parseTaskfile(file: VirtualFile): TaskfileModel? {
        return try {
            val content = file.inputStream.use { inputStream ->
                yaml.load(InputStreamReader(inputStream)) as? Map<String, Any>
            } ?: return null
            
            val tasks = parseTasks(content)
            TaskfileModel(file, tasks)
        } catch (e: Exception) {
            // Throw exception to be handled by caller with proper error handling
            throw e
        }
    }
    
    private fun parseTasks(content: Map<String, Any>): List<TaskModel> {
        val tasksSection = content["tasks"] as? Map<String, Any> ?: return emptyList()
        
        return tasksSection.map { (taskName, taskData) ->
            when (taskData) {
                is Map<*, *> -> {
                    val taskMap = taskData as Map<String, Any>
                    TaskModel(
                        name = taskName,
                        description = taskMap["desc"] as? String,
                        commands = parseCommands(taskMap["cmds"]),
                        dependencies = parseDependencies(taskMap["deps"])
                    )
                }
                is List<*> -> {
                    // Simple command list format
                    TaskModel(
                        name = taskName,
                        commands = taskData.filterIsInstance<String>()
                    )
                }
                is String -> {
                    // Single command format
                    TaskModel(
                        name = taskName,
                        commands = listOf(taskData)
                    )
                }
                else -> TaskModel(name = taskName)
            }
        }
    }
    
    private fun parseCommands(cmds: Any?): List<String> {
        return when (cmds) {
            is List<*> -> cmds.filterIsInstance<String>()
            is String -> listOf(cmds)
            else -> emptyList()
        }
    }
    
    private fun parseDependencies(deps: Any?): List<String> {
        return when (deps) {
            is List<*> -> deps.mapNotNull { dep ->
                when (dep) {
                    is String -> dep
                    is Map<*, *> -> (dep as? Map<String, Any>)?.get("task") as? String
                    else -> null
                }
            }
            is String -> listOf(deps)
            else -> emptyList()
        }
    }
}