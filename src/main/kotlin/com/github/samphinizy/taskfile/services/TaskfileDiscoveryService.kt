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
        private val TASKFILE_NAMES = setOf(
            "Taskfile.yml",
            "Taskfile.yaml", 
            "taskfile.yml",
            "taskfile.yaml"
        )
        
        fun getInstance(project: Project): TaskfileDiscoveryService {
            return project.getService(TaskfileDiscoveryService::class.java)
        }
    }
    
    fun discoverTaskfiles(): List<VirtualFile> {
        return ApplicationManager.getApplication().runReadAction<List<VirtualFile>> {
            val taskfiles = mutableListOf<VirtualFile>()
            
            TASKFILE_NAMES.forEach { filename ->
                val files = FilenameIndex.getVirtualFilesByName(
                    filename, 
                    GlobalSearchScope.projectScope(project)
                )
                taskfiles.addAll(files)
            }
            
            taskfiles.distinctBy { it.path }
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