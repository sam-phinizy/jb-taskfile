package com.github.samphinizy.taskfile.toolWindow

import com.github.samphinizy.taskfile.model.TaskModel
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.content.ContentManager
import org.mockito.Mockito.*

class TaskfileToolWindowFactoryTest : BasePlatformTestCase() {
    
    private lateinit var factory: TaskfileToolWindowFactory
    
    override fun setUp() {
        super.setUp()
        factory = TaskfileToolWindowFactory()
    }
    
    fun testShouldBeAvailable() {
        assertTrue(factory.shouldBeAvailable(project))
    }
    
    fun testCreateToolWindowContent() {
        val mockToolWindow = mock(ToolWindow::class.java)
        val mockContentManager = mock(ContentManager::class.java)
        
        `when`(mockToolWindow.contentManager).thenReturn(mockContentManager)
        
        factory.createToolWindowContent(project, mockToolWindow)
        
        verify(mockContentManager).addContent(any())
    }
}

class TaskfileNodeDataTest : BasePlatformTestCase() {
    
    fun testTaskfileNodeDataToStringForRoot() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val mockParentFile = mock(VirtualFile::class.java)
        val mockProject = mock(com.intellij.openapi.project.Project::class.java)
        
        `when`(mockVirtualFile.name).thenReturn("Taskfile.yml")
        `when`(mockVirtualFile.parent).thenReturn(mockParentFile)
        `when`(mockParentFile.path).thenReturn("/Users/test/project")
        `when`(mockProject.basePath).thenReturn("/Users/test/project")
        
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        val nodeData = TaskfileNodeData(taskfile, listOf(taskfile), mockProject)
        
        assertEquals("/Taskfile.yml", nodeData.toString())
    }
    
    fun testTaskfileNodeDataToStringForSubdirectory() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val mockParentFile = mock(VirtualFile::class.java)
        val mockProject = mock(com.intellij.openapi.project.Project::class.java)
        
        `when`(mockVirtualFile.name).thenReturn("Taskfile.yml")
        `when`(mockVirtualFile.parent).thenReturn(mockParentFile)
        `when`(mockParentFile.path).thenReturn("/Users/test/project/sub")
        `when`(mockProject.basePath).thenReturn("/Users/test/project")
        
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        val nodeData = TaskfileNodeData(taskfile, listOf(taskfile), mockProject)
        
        assertEquals("sub/Taskfile.yml", nodeData.toString())
    }
    
    fun testTaskfileNodeDataToStringWithNoBasePath() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val mockParentFile = mock(VirtualFile::class.java)
        val mockProject = mock(com.intellij.openapi.project.Project::class.java)
        
        `when`(mockVirtualFile.name).thenReturn("Taskfile.yml")
        `when`(mockVirtualFile.parent).thenReturn(mockParentFile)
        `when`(mockParentFile.path).thenReturn("/Users/test/project/sub")
        `when`(mockParentFile.name).thenReturn("sub")
        `when`(mockProject.basePath).thenReturn(null)
        
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        val nodeData = TaskfileNodeData(taskfile, listOf(taskfile), mockProject)
        
        assertEquals("sub/Taskfile.yml", nodeData.toString())
    }
}

class TaskNodeDataTest : BasePlatformTestCase() {
    
    fun testTaskNodeDataToString() {
        val task = TaskModel("test-task", "Test description")
        val nodeData = TaskNodeData(task)
        
        assertEquals("test-task", nodeData.toString())
    }
}

class TaskDescriptionDataTest : BasePlatformTestCase() {
    
    fun testTaskDescriptionDataToString() {
        val description = "This is a test description"
        val nodeData = TaskDescriptionData(description)
        
        assertEquals(description, nodeData.toString())
    }
}