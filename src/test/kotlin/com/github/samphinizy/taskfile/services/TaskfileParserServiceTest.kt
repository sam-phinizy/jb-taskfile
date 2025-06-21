package com.github.samphinizy.taskfile.services

import com.github.samphinizy.taskfile.model.TaskModel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream

class TaskfileParserServiceTest : BasePlatformTestCase() {
    
    private lateinit var parserService: TaskfileParserService
    
    override fun setUp() {
        super.setUp()
        parserService = TaskfileParserService()
    }
    
    fun testParseBasicTaskfile() {
        val yamlContent = """
            version: '3'
            tasks:
              hello:
                desc: Say hello
                cmds:
                  - echo "Hello World"
              build:
                desc: Build the project
                cmds:
                  - gradle build
                deps:
                  - clean
        """.trimIndent()
        
        val virtualFile = createMockVirtualFile(yamlContent)
        val result = parserService.parseTaskfile(virtualFile)
        
        assertNotNull(result)
        assertEquals(2, result!!.tasks.size)
        
        val helloTask = result.tasks.find { it.name == "hello" }
        assertNotNull(helloTask)
        assertEquals("Say hello", helloTask!!.description)
        assertEquals(listOf("echo \"Hello World\""), helloTask.commands)
        assertTrue(helloTask.dependencies.isEmpty())
        
        val buildTask = result.tasks.find { it.name == "build" }
        assertNotNull(buildTask)
        assertEquals("Build the project", buildTask!!.description)
        assertEquals(listOf("gradle build"), buildTask.commands)
        assertEquals(listOf("clean"), buildTask.dependencies)
    }
    
    fun testParseTaskfileWithSimpleCommandFormat() {
        val yamlContent = """
            version: '3'
            tasks:
              simple:
                - echo "Simple task"
                - ls -la
        """.trimIndent()
        
        val virtualFile = createMockVirtualFile(yamlContent)
        val result = parserService.parseTaskfile(virtualFile)
        
        assertNotNull(result)
        assertEquals(1, result!!.tasks.size)
        
        val simpleTask = result.tasks.first()
        assertEquals("simple", simpleTask.name)
        assertNull(simpleTask.description)
        assertEquals(listOf("echo \"Simple task\"", "ls -la"), simpleTask.commands)
    }
    
    fun testParseTaskfileWithSingleStringCommand() {
        val yamlContent = """
            version: '3'
            tasks:
              single: echo "Single command"
        """.trimIndent()
        
        val virtualFile = createMockVirtualFile(yamlContent)
        val result = parserService.parseTaskfile(virtualFile)
        
        assertNotNull(result)
        assertEquals(1, result!!.tasks.size)
        
        val singleTask = result.tasks.first()
        assertEquals("single", singleTask.name)
        assertEquals(listOf("echo \"Single command\""), singleTask.commands)
    }
    
    fun testParseTaskfileWithComplexDependencies() {
        val yamlContent = """
            version: '3'
            tasks:
              deploy:
                desc: Deploy application
                cmds:
                  - kubectl apply -f deployment.yaml
                deps:
                  - task: build
                  - task: test
                  - clean
        """.trimIndent()
        
        val virtualFile = createMockVirtualFile(yamlContent)
        val result = parserService.parseTaskfile(virtualFile)
        
        assertNotNull(result)
        val deployTask = result!!.tasks.first()
        assertEquals("deploy", deployTask.name)
        assertEquals(listOf("build", "test", "clean"), deployTask.dependencies)
    }
    
    fun testParseMalformedYaml() {
        val yamlContent = """
            version: '3'
            tasks:
              broken:
                desc: "Unclosed quote
                cmds:
                  - echo "test"
        """.trimIndent()
        
        val virtualFile = createMockVirtualFile(yamlContent)
        
        // Should throw exception for malformed YAML, which will be caught by discovery service
        assertThrows(Exception::class.java) {
            parserService.parseTaskfile(virtualFile)
        }
    }
    
    private fun assertThrows(expectedType: Class<out Throwable>, block: () -> Unit) {
        try {
            block()
            fail("Expected ${expectedType.simpleName} to be thrown")
        } catch (e: Throwable) {
            if (!expectedType.isInstance(e)) {
                fail("Expected ${expectedType.simpleName} but got ${e::class.simpleName}: ${e.message}")
            }
        }
    }
    
    fun testParseEmptyTaskfile() {
        val yamlContent = """
            version: '3'
        """.trimIndent()
        
        val virtualFile = createMockVirtualFile(yamlContent)
        val result = parserService.parseTaskfile(virtualFile)
        
        assertNotNull(result)
        assertTrue(result!!.tasks.isEmpty())
    }
    
    private fun createMockVirtualFile(content: String): VirtualFile {
        val virtualFile = mock(VirtualFile::class.java)
        `when`(virtualFile.inputStream).thenReturn(ByteArrayInputStream(content.toByteArray()))
        `when`(virtualFile.name).thenReturn("Taskfile.yml")
        `when`(virtualFile.path).thenReturn("/test/Taskfile.yml")
        return virtualFile
    }
}