# Changelog

All notable changes to the Taskfile IntelliJ Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-12-XX

### Added
- **Automatic Taskfile Discovery**: Scans project for all Taskfile.yml/yaml files
- **Tool Window Integration**: Dedicated panel showing tasks in tree structure  
- **One-Click Task Execution**: Run tasks directly from IDE with console output
- **Global Run Action**: Quick task runner accessible via `Ctrl+Shift+T`
- **Multi-Taskfile Support**: Handles projects with multiple taskfiles in different directories
- **Directory Context Display**: Shows which folder each taskfile belongs to
- **Rich Task Information**: Displays task names, descriptions, and dependencies
- **Error Handling**: Comprehensive error reporting for malformed files and execution issues
- **Keyboard Shortcuts**: Full keyboard navigation and shortcuts
- **Console Integration**: Task output appears in dedicated console tabs

### Technical Features
- Support for all Taskfile v3 YAML formats
- Task dependency resolution and display
- Various command formats (string, array, object)
- Background task discovery with caching
- Thread-safe file operations
- Comprehensive test coverage (58+ tests)

### UI Enhancements
- Green run buttons next to each task
- Indented task descriptions  
- Expandable/collapsible task tree
- Toolbar actions (refresh, expand all, collapse all)
- Custom tree cell renderer for better visual hierarchy
- Support for both light and dark IDE themes

### Compatibility
- IntelliJ Platform 2023.3+
- All JetBrains IDEs (IntelliJ IDEA, PyCharm, WebStorm, etc.)
- Java 17+
- All operating systems (Windows, macOS, Linux)