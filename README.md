# File Indexer Example

This is a Kotlin-based example application that demonstrates file indexing, real-time search, and file monitoring capabilities using [file-indexer](https://github.com/edwarddiehl/file-indexer) library.

## `file-examples` Directory

The `[file-examples][https://github.com/EdwardDiehl/file-indexer-example/tree/main/file-examples` directory contains sample files used to demonstrate the app's functionality. You can:

- Add new files
- Edit existing ones
- Use a variety of file types (`.kt`, `.java`, `.txt`, `.md`, etc.)

The application uses this directory to index content and respond to search queries or file changes.

## Running the Application

To run the app from the console, use the following command from the project root directory:

```bash
./gradlew run --console=plain
```

## Using the App

After running, you'll see a menu like:

```
==================================================
FileIndexer Library Examples
==================================================
Choose an example to run:

1. Basic Usage Example
2. Advanced Configuration Example
3. File Monitoring Example
4. Live Search Example
5. Code Analysis Tool Example
0. Exit

Enter your choice (0-5):
```

Enter a number to run the corresponding example.

## Notes

- If the `file-examples` directory doesn't exist, the app will create it on first run
- Make sure to place meaningful content in that directory for the examples to work as intended
- Use `--console=plain` to avoid Gradle progress bar interference with interactive input
