package com.github.samphinizy.taskfile.model

import com.intellij.openapi.vfs.VirtualFile

data class TaskfileModel(
    val file: VirtualFile,
    val tasks: List<TaskModel>
) {
    val name: String get() = file.name
    val path: String get() = file.path
    val relativePath: String get() = file.presentableUrl
}

data class TaskModel(
    val name: String,
    val description: String? = null,
    val commands: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val taskfile: TaskfileModel? = null
) {
    val displayName: String get() = if (description.isNullOrBlank()) name else "$name - $description"
}