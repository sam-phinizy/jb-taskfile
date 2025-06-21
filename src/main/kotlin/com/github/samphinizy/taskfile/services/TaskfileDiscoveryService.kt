package com.github.samphinizy.taskfile.services

import com.github.samphinizy.taskfile.model.TaskfileModel
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

@Service
class TaskfileDiscoveryService(private val project: Project) {
    
    companion object {
        // All supported taskfile names as per https://taskfile.dev/usage/
        // Listed in order of priority (Task checks in this order)
        val TASKFILE_NAMES = listOf(
            "Taskfile.yml",
            "taskfile.yml",
            "Taskfile.yaml",
            "taskfile.yaml",
            "Taskfile.dist.yml",
            "taskfile.dist.yml",
            "Taskfile.dist.yaml",
            "taskfile.dist.yaml"
        )
        
        fun getInstance(project: Project): TaskfileDiscoveryService {
            return project.getService(TaskfileDiscoveryService::class.java)
        }
    }
    
    fun discoverTaskfiles(): List<VirtualFile> {
        return ApplicationManager.getApplication().runReadAction<List<VirtualFile>> {
            val allTaskfiles = mutableListOf<VirtualFile>()
            
            TASKFILE_NAMES.forEach { filename ->
                val files = FilenameIndex.getVirtualFilesByName(
                    filename, 
                    GlobalSearchScope.projectScope(project)
                )
                allTaskfiles.addAll(files)
            }
            
            // Group by directory and apply priority-based filtering
            val taskfilesByDirectory = allTaskfiles.groupBy { it.parent?.path ?: "" }
            
            taskfilesByDirectory.values.flatMap { filesInDirectory ->
                // If multiple taskfiles exist in the same directory, pick highest priority one
                if (filesInDirectory.size <= 1) {
                    filesInDirectory
                } else {
                    // Find the highest priority taskfile in this directory
                    val priorityFile = TASKFILE_NAMES.firstNotNullOfOrNull { priorityName ->
                        filesInDirectory.find { it.name == priorityName }
                    }
                    listOfNotNull(priorityFile)
                }
            }
        }
    }
    
    fun parseTaskfile(file: VirtualFile): TaskfileModel? {
        return try {
            val parserService = TaskfileParserService.getInstance()
            parserService.parseTaskfile(file)
        } catch (e: Exception) {
            val errorHandler = TaskfileErrorHandler.getInstance(project)
            errorHandler.handleParsingError(file, e)
            null
        }
    }
    
    fun getAllTaskfiles(): List<TaskfileModel> {
        return discoverTaskfiles().mapNotNull { parseTaskfile(it) }
    }
}