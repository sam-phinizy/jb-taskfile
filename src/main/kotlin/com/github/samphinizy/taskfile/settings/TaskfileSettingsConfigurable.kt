package com.github.samphinizy.taskfile.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JLabel

class TaskfileSettingsConfigurable : Configurable {
    
    override fun getDisplayName(): String = "Taskfile"
    
    override fun createComponent(): JComponent {
        // Placeholder for settings UI - will be enhanced in P1 features
        return JLabel("Taskfile settings will be available in future versions")
    }
    
    override fun isModified(): Boolean = false
    
    override fun apply() {
        // No settings to apply yet
    }
}