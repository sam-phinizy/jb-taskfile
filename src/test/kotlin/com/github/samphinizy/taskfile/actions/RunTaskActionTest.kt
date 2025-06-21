package com.github.samphinizy.taskfile.actions

import com.github.samphinizy.taskfile.model.TaskModel
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.github.samphinizy.taskfile.services.TaskExecutionService
import com.github.samphinizy.taskfile.services.TaskfileDiscoveryService
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.*

class RunTaskActionTest : BasePlatformTestCase() {
    
    private lateinit var action: RunTaskAction
    private lateinit var mockDiscoveryService: TaskfileDiscoveryService
    private lateinit var mockExecutionService: TaskExecutionService
    
    override fun setUp() {
        super.setUp()
        action = RunTaskAction()
        mockDiscoveryService = mock(TaskfileDiscoveryService::class.java)
        mockExecutionService = mock(TaskExecutionService::class.java)
    }
    
    fun testActionPerformedWithNoProject() {
        val mockEvent = mock(AnActionEvent::class.java)
        `when`(mockEvent.project).thenReturn(null)
        
        action.actionPerformed(mockEvent)
        
        verifyNoInteractions(mockDiscoveryService)
    }
    
    fun testActionPerformedWithEmptyTaskfiles() {
        val mockEvent = mock(AnActionEvent::class.java)
        `when`(mockEvent.project).thenReturn(project)
        
        action.actionPerformed(mockEvent)
        
        // Should not crash with empty taskfiles
    }
    
    fun testUpdateActionWithProject() {
        val mockEvent = mock(AnActionEvent::class.java)
        val mockPresentation = mock(Presentation::class.java)
        
        `when`(mockEvent.project).thenReturn(project)
        `when`(mockEvent.presentation).thenReturn(mockPresentation)
        
        action.update(mockEvent)
        
        verify(mockPresentation).isEnabled = true
    }
    
    fun testUpdateActionWithoutProject() {
        val mockEvent = mock(AnActionEvent::class.java)
        val mockPresentation = mock(Presentation::class.java)
        
        `when`(mockEvent.project).thenReturn(null)
        `when`(mockEvent.presentation).thenReturn(mockPresentation)
        
        action.update(mockEvent)
        
        verify(mockPresentation).isEnabled = false
    }
}

class TaskInfoTest : BasePlatformTestCase() {
    
    fun testTaskInfoCreation() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        
        val taskInfo = TaskInfo(task, taskfile, "root - test-task - Test description")
        
        assertEquals(task, taskInfo.task)
        assertEquals(taskfile, taskInfo.taskfile)
        assertEquals("root - test-task - Test description", taskInfo.displayText)
    }
}

class TaskListPopupStepTest : BasePlatformTestCase() {
    
    fun testTaskListPopupStep() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        val taskInfo = TaskInfo(task, taskfile, "root - test-task - Test description")
        val tasks = listOf(taskInfo)
        
        val popupStep = TaskListPopupStep(project, tasks)
        
        assertEquals("Select Task to Run", popupStep.title)
        assertEquals("root - test-task - Test description", popupStep.getTextFor(taskInfo))
        assertTrue(popupStep.isSpeedSearchEnabled)
    }
    
    fun testOnChosenWithFinalChoice() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        val taskInfo = TaskInfo(task, taskfile, "root - test-task - Test description")
        val tasks = listOf(taskInfo)
        
        val popupStep = TaskListPopupStep(project, tasks)
        
        val result = popupStep.onChosen(taskInfo, true)
        
        assertNull(result)
    }
}