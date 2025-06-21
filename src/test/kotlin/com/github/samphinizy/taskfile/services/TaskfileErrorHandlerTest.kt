package com.github.samphinizy.taskfile.services

import com.intellij.execution.ExecutionException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.*

class TaskfileErrorHandlerTest : BasePlatformTestCase() {
    
    private lateinit var errorHandler: TaskfileErrorHandler
    
    override fun setUp() {
        super.setUp()
        errorHandler = TaskfileErrorHandler(project)
    }
    
    fun testHandleParsingError() {
        val mockFile = mock(VirtualFile::class.java)
        `when`(mockFile.name).thenReturn("Taskfile.yml")
        
        val exception = RuntimeException("Invalid YAML syntax")
        
        // This should not throw an exception
        assertDoesNotThrow {
            errorHandler.handleParsingError(mockFile, exception)
        }
    }
    
    fun testHandleTaskExecutionErrorWithTaskNotFound() {
        val exception = ExecutionException("Cannot run program \"task\": error=2, No such file or directory")
        
        assertDoesNotThrow {
            errorHandler.handleTaskExecutionError("build", exception)
        }
    }
    
    fun testHandleTaskExecutionErrorWithGenericError() {
        val exception = ExecutionException("Task failed with exit code 1")
        
        assertDoesNotThrow {
            errorHandler.handleTaskExecutionError("test", exception)
        }
    }
    
    fun testHandleDiscoveryError() {
        val exception = RuntimeException("Failed to scan project files")
        
        assertDoesNotThrow {
            errorHandler.handleDiscoveryError(exception)
        }
    }
    
    fun testHandleMalformedTaskfile() {
        val mockFile = mock(VirtualFile::class.java)
        `when`(mockFile.name).thenReturn("Taskfile.yml")
        
        assertDoesNotThrow {
            errorHandler.handleMalformedTaskfile(mockFile, "Missing tasks section")
        }
    }
    
    fun testShowTaskNotFoundError() {
        assertDoesNotThrow {
            errorHandler.showTaskNotFoundError("nonexistent-task")
        }
    }
    
    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
            // If no exception is thrown, test passes
        } catch (e: Exception) {
            fail("Expected no exception but got: ${e.message}")
        }
    }
}