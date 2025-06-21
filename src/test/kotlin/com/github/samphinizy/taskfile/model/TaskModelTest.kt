package com.github.samphinizy.taskfile.model

import com.intellij.openapi.vfs.VirtualFile
import junit.framework.TestCase
import org.mockito.Mockito.*

class TaskModelTest : TestCase() {
    
    fun testTaskModelDisplayNameWithDescription() {
        val task = TaskModel(
            name = "build",
            description = "Build the project",
            commands = listOf("gradle build")
        )
        
        assertEquals("build - Build the project", task.displayName)
    }
    
    fun testTaskModelDisplayNameWithoutDescription() {
        val task = TaskModel(
            name = "build",
            commands = listOf("gradle build")
        )
        
        assertEquals("build", task.displayName)
    }
    
    fun testTaskModelDisplayNameWithEmptyDescription() {
        val task = TaskModel(
            name = "build",
            description = "",
            commands = listOf("gradle build")
        )
        
        assertEquals("build", task.displayName)
    }
    
    fun testTaskModelDisplayNameWithBlankDescription() {
        val task = TaskModel(
            name = "build",
            description = "   ",
            commands = listOf("gradle build")
        )
        
        assertEquals("build", task.displayName)
    }
    
    fun testTaskModelWithDependencies() {
        val task = TaskModel(
            name = "deploy",
            description = "Deploy application",
            commands = listOf("kubectl apply -f deployment.yaml"),
            dependencies = listOf("build", "test")
        )
        
        assertEquals("deploy", task.name)
        assertEquals("Deploy application", task.description)
        assertEquals(listOf("kubectl apply -f deployment.yaml"), task.commands)
        assertEquals(listOf("build", "test"), task.dependencies)
    }
    
    fun testTaskModelDefaults() {
        val task = TaskModel(name = "simple")
        
        assertEquals("simple", task.name)
        assertNull(task.description)
        assertTrue(task.commands.isEmpty())
        assertTrue(task.dependencies.isEmpty())
        assertNull(task.taskfile)
    }
}

class TaskfileModelTest : TestCase() {
    
    fun testTaskfileModelProperties() {
        val virtualFile = mock(VirtualFile::class.java)
        `when`(virtualFile.name).thenReturn("Taskfile.yml")
        `when`(virtualFile.path).thenReturn("/project/Taskfile.yml")
        `when`(virtualFile.presentableUrl).thenReturn("project/Taskfile.yml")
        
        val tasks = listOf(
            TaskModel("build", "Build project"),
            TaskModel("test", "Run tests")
        )
        
        val taskfileModel = TaskfileModel(virtualFile, tasks)
        
        assertEquals("Taskfile.yml", taskfileModel.name)
        assertEquals("/project/Taskfile.yml", taskfileModel.path)
        assertEquals("project/Taskfile.yml", taskfileModel.relativePath)
        assertEquals(2, taskfileModel.tasks.size)
        assertEquals(virtualFile, taskfileModel.file)
    }
    
    fun testTaskfileModelWithEmptyTasks() {
        val virtualFile = mock(VirtualFile::class.java)
        `when`(virtualFile.name).thenReturn("Taskfile.yml")
        `when`(virtualFile.path).thenReturn("/project/Taskfile.yml")
        `when`(virtualFile.presentableUrl).thenReturn("project/Taskfile.yml")
        
        val taskfileModel = TaskfileModel(virtualFile, emptyList())
        
        assertEquals("Taskfile.yml", taskfileModel.name)
        assertTrue(taskfileModel.tasks.isEmpty())
    }
}