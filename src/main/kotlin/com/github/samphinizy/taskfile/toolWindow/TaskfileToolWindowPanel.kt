package com.github.samphinizy.taskfile.toolWindow

import com.github.samphinizy.taskfile.model.TaskModel
import com.github.samphinizy.taskfile.model.TaskfileModel
import com.github.samphinizy.taskfile.services.TaskfileDiscoveryService
import com.github.samphinizy.taskfile.services.TaskExecutionService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.TreeUIHelper
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.tree.TreeUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Font
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.tree.*

class TaskfileToolWindowPanel(private val project: Project) {
    
    private val tree = Tree()
    private val panel = JPanel(BorderLayout())
    
    init {
        setupTree()
        setupPanel()
        refreshTaskfiles()
    }
    
    fun getPanel(): JPanel = panel
    
    private fun setupTree() {
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.cellRenderer = TaskTreeCellRenderer()
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        
        // Completely disable selection background
        tree.putClientProperty("JTree.lineStyle", "None")
        
        // Try multiple ways to disable selection background
        val transparentColor = java.awt.Color(0, 0, 0, 0)
        tree.putClientProperty("Tree.selectionBackground", transparentColor)
        tree.putClientProperty("Tree.selectionBorderColor", transparentColor)
        tree.putClientProperty("Tree.textBackground", transparentColor)
        
        // Override UIManager defaults for this tree
        javax.swing.UIManager.put("Tree.selectionBackground", transparentColor)
        javax.swing.UIManager.put("Tree.selectionBorderColor", transparentColor)
        
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree)
        
        // Double-click listener for task execution
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                val path = tree.getPathForLocation(event.x, event.y) ?: return false
                val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return false
                val nodeData = node.getUserObject()
                
                when (nodeData) {
                    is TaskNodeData -> {
                        executeTask(nodeData.task)
                        return true
                    }
                    is TaskModel -> {
                        // Fallback for old format
                        executeTask(nodeData)
                        return true
                    }
                }
                return false
            }
        }.installOn(tree)
        
        // Single click listener for run button clicks
        tree.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                val path = tree.getPathForLocation(e.x, e.y) ?: return
                val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
                val nodeData = node.getUserObject()
                
                if (nodeData is TaskNodeData) {
                    // Check if click was on the run button area (first ~20 pixels)
                    val bounds = tree.getPathBounds(path)
                    if (bounds != null && e.x - bounds.x <= 20) {
                        executeTask(nodeData.task)
                    }
                }
            }
            
            override fun mousePressed(e: MouseEvent) {}
            override fun mouseReleased(e: MouseEvent) {}
            override fun mouseEntered(e: MouseEvent) {}
            override fun mouseExited(e: MouseEvent) {}
        })
        
        // Context menu setup would go here
    }
    
    private fun setupPanel() {
        val toolbar = createToolbar()
        
        panel.add(toolbar.component, BorderLayout.NORTH)
        panel.add(JScrollPane(tree), BorderLayout.CENTER)
    }
    
    private fun createToolbar(): ActionToolbar {
        val actionGroup = DefaultActionGroup().apply {
            add(RefreshAction())
            add(ExpandAllAction())
            add(CollapseAllAction())
        }
        
        return ActionManager.getInstance().createActionToolbar(
            "TaskfileToolWindow",
            actionGroup,
            true
        )
    }
    
    private fun refreshTaskfiles() {
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val discoveryService = TaskfileDiscoveryService.getInstance(project)
                val taskfiles = discoveryService.getAllTaskfiles()
                
                ApplicationManager.getApplication().invokeLater {
                    updateTreeModel(taskfiles)
                }
            } catch (e: Exception) {
                val errorHandler = com.github.samphinizy.taskfile.services.TaskfileErrorHandler.getInstance(project)
                errorHandler.handleDiscoveryError(e)
            }
        }
    }
    
    private fun updateTreeModel(taskfiles: List<TaskfileModel>) {
        val root = DefaultMutableTreeNode("Root")
        
        taskfiles.forEach { taskfile ->
            val taskfileNode = DefaultMutableTreeNode(TaskfileNodeData(taskfile, taskfiles, project))
            
            taskfile.tasks.forEach { task ->
                val taskNameNode = DefaultMutableTreeNode(TaskNodeData(task))
                taskfileNode.add(taskNameNode)
                
                // Add description as a child node if it exists
                if (!task.description.isNullOrBlank()) {
                    val descriptionNode = DefaultMutableTreeNode(TaskDescriptionData(task.description))
                    taskNameNode.add(descriptionNode)
                }
            }
            
            root.add(taskfileNode)
        }
        
        tree.model = DefaultTreeModel(root)
        TreeUtil.expandAll(tree)
    }
    
    private fun executeTask(task: TaskModel) {
        try {
            val executionService = TaskExecutionService.getInstance(project)
            executionService.executeTask(task)
        } catch (e: Exception) {
            val errorHandler = com.github.samphinizy.taskfile.services.TaskfileErrorHandler.getInstance(project)
            errorHandler.handleTaskExecutionError(task.name, e)
        }
    }
    
    // Toolbar Actions
    inner class RefreshAction : AnAction("Refresh", "Refresh taskfiles", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            refreshTaskfiles()
        }
    }
    
    inner class ExpandAllAction : AnAction("Expand All", "Expand all nodes", AllIcons.Actions.Expandall) {
        override fun actionPerformed(e: AnActionEvent) {
            TreeUtil.expandAll(tree)
        }
    }
    
    inner class CollapseAllAction : AnAction("Collapse All", "Collapse all nodes", AllIcons.Actions.Collapseall) {
        override fun actionPerformed(e: AnActionEvent) {
            TreeUtil.collapseAll(tree, 1)
        }
    }
}

// Helper classes for different node types
data class TaskfileNodeData(val taskfile: TaskfileModel, val allTaskfiles: List<TaskfileModel>, val project: Project) {
    override fun toString(): String {
        val taskfileName = taskfile.file.name
        val projectBasePath = project.basePath
        val taskfilePath = taskfile.file.parent?.path ?: ""
        
        // Calculate relative path from project root
        val relativePath = if (projectBasePath != null && taskfilePath.startsWith(projectBasePath)) {
            val relative = taskfilePath.removePrefix(projectBasePath).removePrefix("/")
            relative
        } else {
            taskfile.file.parent?.name ?: "unknown"
        }
        
        return if (relativePath.isEmpty()) {
            "/$taskfileName"  // Root level
        } else {
            "$relativePath/$taskfileName"  // Show the relative path
        }
    }
}

data class TaskNodeData(val task: TaskModel) {
    override fun toString(): String = task.name
}

data class TaskDescriptionData(val description: String) {
    override fun toString(): String = description
}

// Custom tree cell renderer for better visual presentation
class TaskTreeCellRenderer : DefaultTreeCellRenderer() {
    
    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        
        // Force selected to false to prevent default selection styling
        val component = super.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row, false)
        
        if (component is JLabel) {
            val node = value as? DefaultMutableTreeNode
            val nodeData = node?.userObject
            
            when (nodeData) {
                is TaskfileNodeData -> {
                    component.icon = AllIcons.FileTypes.Yaml
                    component.text = nodeData.toString()
                    component.font = component.font.deriveFont(Font.BOLD)
                    
                    // Always remove selection background
                    component.isOpaque = false
                    component.background = tree.background
                }
                
                is TaskNodeData -> {
                    component.icon = AllIcons.RunConfigurations.TestState.Run
                    component.text = nodeData.task.name
                    component.font = component.font.deriveFont(Font.PLAIN)
                    component.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    component.toolTipText = "Click to run ${nodeData.task.name}"
                    
                    // Always remove selection background
                    component.isOpaque = false
                    component.background = tree.background
                }
                
                is TaskDescriptionData -> {
                    component.icon = null
                    component.text = "  ${nodeData.description}"  // Indent with spaces
                    component.font = component.font.deriveFont(Font.ITALIC)
                    component.foreground = Color.GRAY
                    
                    // Always remove selection background
                    component.isOpaque = false
                    component.background = tree.background
                }
            }
        }
        
        return component
    }
}