package com.github.samphinizy.taskfile.toolWindow

import com.github.samphinizy.taskfile.model.TaskModel
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.mockito.Mockito.*
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

class TaskfileToolWindowPanelTest : BasePlatformTestCase() {
    
    private lateinit var panel: TaskfileToolWindowPanel
    
    override fun setUp() {
        super.setUp()
        panel = TaskfileToolWindowPanel(project)
    }
    
    fun testGetPanel() {
        val jPanel = panel.getPanel()
        assertNotNull(jPanel)
        assertTrue(jPanel is JPanel)
    }
    
    fun testPanelInitialization() {
        val jPanel = panel.getPanel()
        
        // Panel should have been initialized with BorderLayout
        assertNotNull(jPanel.layout)
        assertTrue(jPanel.componentCount > 0)
    }
}

class TaskTreeCellRendererTest : BasePlatformTestCase() {
    
    private lateinit var renderer: TaskTreeCellRenderer
    
    override fun setUp() {
        super.setUp()
        renderer = TaskTreeCellRenderer()
    }
    
    fun testRenderTaskfileNode() {
        val mockVirtualFile = mock(VirtualFile::class.java)
        val mockParentFile = mock(VirtualFile::class.java)
        
        `when`(mockVirtualFile.name).thenReturn("Taskfile.yml")
        `when`(mockVirtualFile.parent).thenReturn(mockParentFile)
        `when`(mockParentFile.path).thenReturn("/Users/test/project")
        
        val task = TaskModel("test-task", "Test description")
        val taskfile = TaskfileModel(mockVirtualFile, listOf(task))
        val nodeData = TaskfileNodeData(taskfile, listOf(taskfile), project)
        val node = DefaultMutableTreeNode(nodeData)
        
        val tree = javax.swing.JTree()
        val component = renderer.getTreeCellRendererComponent(
            tree, node, false, false, false, 0, false
        )
        
        assertNotNull(component)
        assertTrue(component is javax.swing.JLabel)
    }
    
    fun testRenderTaskNode() {
        val task = TaskModel("test-task", "Test description")
        val nodeData = TaskNodeData(task)
        val node = DefaultMutableTreeNode(nodeData)
        
        val tree = javax.swing.JTree()
        val component = renderer.getTreeCellRendererComponent(
            tree, node, false, false, false, 0, false
        )
        
        assertNotNull(component)
        assertTrue(component is javax.swing.JLabel)
        
        val label = component as javax.swing.JLabel
        assertEquals("test-task", label.text)
    }
    
    fun testRenderDescriptionNode() {
        val description = "This is a test description"
        val nodeData = TaskDescriptionData(description)
        val node = DefaultMutableTreeNode(nodeData)
        
        val tree = javax.swing.JTree()
        val component = renderer.getTreeCellRendererComponent(
            tree, node, false, false, false, 0, false
        )
        
        assertNotNull(component)
        assertTrue(component is javax.swing.JLabel)
        
        val label = component as javax.swing.JLabel
        assertEquals("  $description", label.text)
    }
}