# Taskfile IntelliJ Plugin

A comprehensive JetBrains IDE plugin that provides native integration with [Task](https://taskfile.dev), bringing taskfile discovery, parsing, and execution directly into your development environment.

## Features

- **Automatic Discovery**: Automatically finds and parses `Taskfile.yml`/`yaml` files in your project
- **Tool Window**: Dedicated panel showing all taskfiles and their tasks in a tree structure
- **Task Execution**: Run tasks directly from the IDE with integrated console output
- **Global Action**: Quick task runner accessible via `Ctrl+Shift+T` from anywhere in the IDE
- **Multi-Taskfile Support**: Handles projects with multiple taskfiles, showing directory context
- **Rich UI**: Tasks displayed with descriptions, run buttons, and clear visual hierarchy
- **Error Handling**: Comprehensive error reporting for malformed taskfiles and execution issues

## Installation

### From GitHub Releases

1. Download the latest plugin ZIP from [Releases](https://github.com/sam-phinizy/jb-taskfile/releases)
2. In your JetBrains IDE: **File** → **Settings** → **Plugins**
3. Click the gear icon → **Install Plugin from Disk**
4. Select the downloaded ZIP file
5. Restart your IDE

### From JetBrains Marketplace

*Coming soon - plugin will be available in the official marketplace*

## Usage

### Tool Window

After installation, the **Taskfile** tool window appears on the right side of your IDE. It shows:

- All discovered taskfiles with directory context (e.g., `/Taskfile.yml`, `sub/Taskfile.yml`)
- Tasks with green run buttons
- Task descriptions indented below task names
- Toolbar actions to refresh, expand/collapse all

### Running Tasks

**From Tool Window:**
- Double-click any task to execute it
- Click the green run button next to task names

**Global Action:**
- Press `Ctrl+Shift+T` (or **Run** → **Run Task...**)
- Search and select from all available tasks
- Tasks shown as "folder - task-name - description"

### Supported Taskfile Features

- All Task v3 YAML formats
- Task descriptions and commands
- Task dependencies
- Multiple taskfiles per project
- Various command formats (string, array, object)

## Development

### Building

```bash
./gradlew buildPlugin
```

### Testing

```bash
./gradlew test
```

### Running in Development IDE

```bash
./gradlew runIde
```

## Releasing

To create a new release:

1. Create a new tag following semantic versioning:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. The GitHub Action will automatically:
   - Build and test the plugin
   - Create a GitHub release with the plugin ZIP
   - Publish to JetBrains Marketplace (if secrets are configured)

### Required Secrets for Marketplace Publishing

Add these repository secrets for automatic marketplace publishing:

- `JETBRAINS_PUBLISH_TOKEN`: Token from [JetBrains Hub](https://plugins.jetbrains.com/author/me/tokens)

### Optional Secrets for Plugin Signing

For enhanced security (recommended for marketplace):

- `CERTIFICATE_CHAIN`: Plugin signing certificate chain
- `PRIVATE_KEY`: Plugin signing private key  
- `PRIVATE_KEY_PASSWORD`: Private key password

See [JetBrains Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html) for details.

## Architecture

The plugin follows IntelliJ Platform SDK best practices:

- **Services**: Core business logic (`TaskfileDiscoveryService`, `TaskfileParserService`, `TaskExecutionService`)
- **Actions**: User-triggered operations (`RunTaskAction`, toolbar actions)
- **Tool Windows**: UI components (`TaskfileToolWindowPanel`, custom tree renderers)
- **Models**: Data structures (`TaskfileModel`, `TaskModel`)
- **Error Handling**: Centralized error management with user notifications

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Run `./gradlew test` to ensure all tests pass
5. Submit a pull request

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Compatibility

- **IntelliJ Platform**: 2023.3+ (includes IntelliJ IDEA, PyCharm, WebStorm, etc.)
- **Java**: JDK 17+
- **Task**: All versions supporting Taskfile v3 format