package com.github.samphinizy.taskfile.integration

import com.github.samphinizy.taskfile.actions.RunTaskAction
import com.github.samphinizy.taskfile.actions.TaskInfo
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.github.samphinizy.taskfile.services.TaskfileDiscoveryService
import com.github.samphinizy.taskfile.services.TaskfileParserService
import com.github.samphinizy.taskfile.toolWindow.TaskfileNodeData
import com.github.samphinizy.taskfile.toolWindow.TaskfileToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.ByteArrayInputStream

class TaskfilePluginIntegrationTest : BasePlatformTestCase() {
    
    fun testFullWorkflowFromDiscoveryToExecution() {
        // Create a sample Taskfile content
        val taskfileContent = """
            version: '3'
            
            vars:
              GREETING: Hello World
              
            tasks:
              hello:
                desc: Print a greeting
                cmds:
                  - echo "{{.GREETING}}"
                  
              build:
                desc: Build the application
                cmds:
                  - echo "Building..."
                  - echo "Build complete"
                deps:
                  - clean
                  
              clean:
                desc: Clean build artifacts
                cmds:
                  - echo "Cleaning..."
                  - rm -rf build/
                  
              test:
                desc: Run tests
                cmds:
                  - echo "Running tests..."
                  - npm test
                deps:
                  - build
                  
              deploy:
                desc: Deploy to production
                cmds:
                  - echo "Deploying..."
                  - kubectl apply -f deployment.yaml
                deps:
                  - task: test
                  - task: build
        """.trimIndent()
        
        // Test parsing
        val parserService = TaskfileParserService()
        val mockFile = createMockVirtualFile("Taskfile.yml", "/project/Taskfile.yml", taskfileContent)
        val taskfileModel = parserService.parseTaskfile(mockFile)
        
        assertNotNull("Taskfile should be parsed successfully", taskfileModel)
        assertEquals("Should have correct number of tasks", 5, taskfileModel!!.tasks.size)
        
        // Test individual tasks
        val helloTask = taskfileModel.tasks.find { it.name == "hello" }
        assertNotNull("Hello task should exist", helloTask)
        assertEquals("Print a greeting", helloTask!!.description)
        assertEquals(listOf("echo \"{{.GREETING}}\""), helloTask.commands)
        assertTrue("Hello task should have no dependencies", helloTask.dependencies.isEmpty())
        
        val buildTask = taskfileModel.tasks.find { it.name == "build" }
        assertNotNull("Build task should exist", buildTask)
        assertEquals("Build the application", buildTask!!.description)
        assertEquals(listOf("echo \"Building...\"", "echo \"Build complete\""), buildTask.commands)
        assertEquals(listOf("clean"), buildTask.dependencies)
        
        val deployTask = taskfileModel.tasks.find { it.name == "deploy" }
        assertNotNull("Deploy task should exist", deployTask)
        assertEquals("Deploy to production", deployTask!!.description)
        assertEquals(listOf("test", "build"), deployTask.dependencies)
        
        // Test display names
        assertEquals("hello - Print a greeting", helloTask.displayName)
        assertEquals("build - Build the application", buildTask.displayName)
        assertEquals("deploy - Deploy to production", deployTask.displayName)
        
        // Test task with no description
        val cleanTask = taskfileModel.tasks.find { it.name == "clean" }
        assertNotNull("Clean task should exist", cleanTask)
        assertEquals("clean - Clean build artifacts", cleanTask!!.displayName)
    }
    
    fun testComplexTaskfileStructures() {
        val complexTaskfileContent = """
            version: '3'
            
            includes:
              docker: ./docker/Taskfile.yml
              
            tasks:
              # Task with minimal definition
              simple: echo "Simple task"
              
              # Task with array commands
              array-cmds:
                - echo "First command"
                - echo "Second command"
                
              # Task with complex dependencies
              complex:
                desc: Complex task with various dependency formats
                cmds:
                  - echo "Running complex task"
                deps:
                  - simple
                  - task: array-cmds
                  - task: build
                    vars:
                      ENV: production
                      
              # Task with environment variables
              env-task:
                desc: Task with environment variables
                env:
                  NODE_ENV: development
                  DEBUG: true
                cmds:
                  - echo "NODE_ENV is {{.ENV}}"
                  - npm run dev
        """.trimIndent()
        
        val parserService = TaskfileParserService()
        val mockFile = createMockVirtualFile("Taskfile.yml", "/project/Taskfile.yml", complexTaskfileContent)
        val taskfileModel = parserService.parseTaskfile(mockFile)
        
        assertNotNull("Complex taskfile should be parsed", taskfileModel)
        assertEquals("Should parse all tasks", 4, taskfileModel!!.tasks.size)
        
        // Test simple task
        val simpleTask = taskfileModel.tasks.find { it.name == "simple" }
        assertNotNull("Simple task should exist", simpleTask)
        assertEquals(listOf("echo \"Simple task\""), simpleTask!!.commands)
        
        // Test array commands task
        val arrayTask = taskfileModel.tasks.find { it.name == "array-cmds" }
        assertNotNull("Array commands task should exist", arrayTask)
        assertEquals(listOf("echo \"First command\"", "echo \"Second command\""), arrayTask!!.commands)
        
        // Test complex dependencies
        val complexTask = taskfileModel.tasks.find { it.name == "complex" }
        assertNotNull("Complex task should exist", complexTask)
        assertEquals(listOf("simple", "array-cmds", "build"), complexTask!!.dependencies)
    }
    
    fun testErrorHandling() {
        val malformedContent = """
            version: '3'
            tasks:
              broken:
                desc: "This quote is not closed
                cmds:
                  - echo "test"
        """.trimIndent()
        
        val parserService = TaskfileParserService()
        val mockFile = createMockVirtualFile("Taskfile.yml", "/project/Taskfile.yml", malformedContent)
        
        // Should throw exception for malformed YAML
        try {
            parserService.parseTaskfile(mockFile)
            fail("Expected exception for malformed YAML")
        } catch (e: Exception) {
            // Expected behavior - malformed YAML should throw exception
            assertTrue("Should throw parsing exception", e.message?.contains("quote") == true || 
                      e is org.yaml.snakeyaml.parser.ParserException)
        }
    }
    
    fun testEmptyTaskfile() {
        val emptyContent = """
            version: '3'
        """.trimIndent()
        
        val parserService = TaskfileParserService()
        val mockFile = createMockVirtualFile("Taskfile.yml", "/project/Taskfile.yml", emptyContent)
        val result = parserService.parseTaskfile(mockFile)
        
        assertNotNull("Empty taskfile should be valid", result)
        assertTrue("Empty taskfile should have no tasks", result!!.tasks.isEmpty())
    }
    
    fun testMultipleTaskfilesUIIntegration() {
        // Test that multiple taskfiles show directory context properly
        val rootTaskfileContent = """
            version: '3'
            tasks:
              root-task:
                desc: Root level task
                cmds:
                  - echo "Root task"
        """.trimIndent()
        
        val subTaskfileContent = """
            version: '3'
            tasks:
              sub-task:
                desc: Subdirectory task
                cmds:
                  - echo "Sub task"
        """.trimIndent()
        
        val parserService = TaskfileParserService()
        val rootFile = createMockVirtualFile("Taskfile.yml", "/project/Taskfile.yml", rootTaskfileContent)
        val subFile = createMockVirtualFile("Taskfile.yml", "/project/sub/Taskfile.yml", subTaskfileContent)
        
        val rootTaskfile = parserService.parseTaskfile(rootFile)
        val subTaskfile = parserService.parseTaskfile(subFile)
        
        assertNotNull("Root taskfile should parse", rootTaskfile)
        assertNotNull("Sub taskfile should parse", subTaskfile)
        
        // Test TaskfileNodeData display logic
        val rootNodeData = TaskfileNodeData(rootTaskfile!!, listOf(rootTaskfile, subTaskfile!!), project)
        val subNodeData = TaskfileNodeData(subTaskfile, listOf(rootTaskfile, subTaskfile), project)
        
        // These should show directory context
        assertTrue("Root should show path context", rootNodeData.toString().contains("Taskfile.yml"))
        assertTrue("Sub should show path context", subNodeData.toString().contains("Taskfile.yml"))
    }
    
    fun testRunTaskActionIntegration() {
        val taskfileContent = """
            version: '3'
            tasks:
              build:
                desc: Build the project
                cmds:
                  - echo "Building..."
              test:
                desc: Run tests
                cmds:
                  - echo "Testing..."
        """.trimIndent()
        
        val parserService = TaskfileParserService()
        val mockFile = createMockVirtualFile("Taskfile.yml", "/project/sub/Taskfile.yml", taskfileContent)
        val taskfile = parserService.parseTaskfile(mockFile)
        
        assertNotNull("Taskfile should parse", taskfile)
        
        // Test TaskInfo creation for run action
        val buildTask = taskfile!!.tasks.find { it.name == "build" }
        assertNotNull("Build task should exist", buildTask)
        
        val taskInfo = TaskInfo(buildTask!!, taskfile, "sub - build - Build the project")
        
        assertEquals("Task info should have correct display text", "sub - build - Build the project", taskInfo.displayText)
        assertEquals("Task info should reference correct task", buildTask, taskInfo.task)
        assertEquals("Task info should reference correct taskfile", taskfile, taskInfo.taskfile)
    }
    
    fun testToolWindowPanelCreation() {
        // Test that tool window panel can be created successfully
        val panel = TaskfileToolWindowPanel(project)
        val jPanel = panel.getPanel()
        
        assertNotNull("Panel should be created", jPanel)
        assertTrue("Panel should have components", jPanel.componentCount > 0)
    }
    
    fun testAllSupportedTaskfileNames() {
        // Test parsing with all officially supported taskfile names
        val taskfileContent = """
            version: '3'
            tasks:
              test:
                desc: Test task
                cmds:
                  - echo "Testing"
        """.trimIndent()
        
        val supportedNames = listOf(
            "Taskfile.yml",
            "taskfile.yml", 
            "Taskfile.yaml",
            "taskfile.yaml",
            "Taskfile.dist.yml",
            "taskfile.dist.yml",
            "Taskfile.dist.yaml",
            "taskfile.dist.yaml"
        )
        
        val parserService = TaskfileParserService()
        
        supportedNames.forEach { filename ->
            val mockFile = createMockVirtualFile(filename, "/project/$filename", taskfileContent)
            val result = parserService.parseTaskfile(mockFile)
            
            assertNotNull("Should parse $filename successfully", result)
            assertEquals("Should have correct tasks for $filename", 1, result!!.tasks.size)
            assertEquals("Should parse task name correctly for $filename", "test", result.tasks[0].name)
        }
    }
    
    fun testDistTaskfileVariants() {
        // Test that .dist variants are properly supported
        val taskfileContent = """
            version: '3'
            tasks:
              dist-task:
                desc: Distribution task
                cmds:
                  - echo "From dist file"
        """.trimIndent()
        
        val distNames = listOf(
            "Taskfile.dist.yml",
            "taskfile.dist.yml",
            "Taskfile.dist.yaml", 
            "taskfile.dist.yaml"
        )
        
        val parserService = TaskfileParserService()
        
        distNames.forEach { filename ->
            val mockFile = createMockVirtualFile(filename, "/project/$filename", taskfileContent)
            val result = parserService.parseTaskfile(mockFile)
            
            assertNotNull("Should parse $filename successfully", result)
            assertEquals("Should parse dist task", "dist-task", result!!.tasks[0].name)
            assertEquals("Should parse dist description", "Distribution task", result.tasks[0].description)
        }
    }
        
    private fun createMockVirtualFile(name: String, path: String, content: String): VirtualFile {
        val virtualFile = org.mockito.Mockito.mock(VirtualFile::class.java)
        val parentFile = org.mockito.Mockito.mock(VirtualFile::class.java)
        
        org.mockito.Mockito.`when`(virtualFile.name).thenReturn(name)
        org.mockito.Mockito.`when`(virtualFile.path).thenReturn(path)
        org.mockito.Mockito.`when`(virtualFile.presentableUrl).thenReturn(path.removePrefix("/project/"))
        org.mockito.Mockito.`when`(virtualFile.inputStream).thenReturn(ByteArrayInputStream(content.toByteArray()))
        
        // Setup parent file for directory path logic
        val parentPath = path.substringBeforeLast("/")
        org.mockito.Mockito.`when`(parentFile.path).thenReturn(parentPath)
        org.mockito.Mockito.`when`(parentFile.name).thenReturn(parentPath.substringAfterLast("/"))
        org.mockito.Mockito.`when`(virtualFile.parent).thenReturn(parentFile)
        
        return virtualFile
    }
}