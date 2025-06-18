package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.example.lib.FileEvent
import org.example.lib.tokenizers.Tokenizer
import java.io.File
import kotlin.time.Duration.Companion.seconds

private val userDir: String = System.getProperty("user.dir")

// Example 1: Basic Setup and Simple Search
suspend fun basicUsageExample() {
    println("=== Basic Usage Example ===")

    val testDocsPath = "$userDir/file-examples"
    val indexer =
        FileIndexer.builder()
            .addPath(testDocsPath)
            .build()

    try {
        indexer.start()

        val results = indexer.search("kotlin")
        println("Found ${results.size} files containing 'kotlin':")
        results.forEach { result ->
            println("  - ${result.file}: ${result.matches}")
        }

        val multiResults = indexer.search(listOf("kotlin", "coroutines"))
        println("\nFound ${multiResults.size} files containing 'kotlin' or 'coroutines':")
        multiResults.forEach { result ->
            println("  - ${result.file}: ${result.matches}")
        }
    } finally {
        indexer.close()
    }
}

// Example 2: Advanced Configuration with Custom Tokenizer and File Filter
suspend fun advancedConfigurationExample() {
    println("=== Advanced Configuration Example ===")

    class CodeTokenizer : Tokenizer {
        override fun tokenize(content: String): Set<String> {
            return content.split(Regex("[\\s\\p{Punct}&&[^_]]"))
                .filter { it.isNotBlank() }.toSet()
        }

        override fun normalize(word: String): String = word.lowercase()
    }

    val testDocsPath = "$userDir/file-examples"
    val indexer =
        FileIndexer.builder()
            .addPath(testDocsPath)
            .tokenizer(CodeTokenizer())
            .fileFilter { path ->
                path.endsWith(".kt") ||
                    path.endsWith(".java") ||
                    path.endsWith(".ru") ||
                    path.endsWith(".txt") ||
                    path.endsWith(".md")
            }
            .build()

    try {
        indexer.start()

        val codeResults = indexer.search(listOf("suspend", "coroutine", "async"))
        println("Code files with async patterns: ${codeResults.size}")

        codeResults.take(5).forEach { result ->
            println("  ${File(result.file).name}: ${result.matches.joinToString(", ")}")
        }
    } finally {
        indexer.close()
    }
}

// Example 3: Real-time File Monitoring
suspend fun fileMonitoringExample() {
    println("=== File Monitoring Example ===")

    val testDocsPath = "$userDir/file-examples"
    val indexer =
        FileIndexer.builder()
            .addPath(testDocsPath)
            .build()

    try {
        indexer.start()

        val monitoringJob =
            CoroutineScope(Dispatchers.IO).launch {
                indexer.watchForChanges()
                    .collect { event ->
                        when (event) {
                            is FileEvent.Created ->
                                println("New file created: ${File(event.filePath).name}")
                            is FileEvent.Modified ->
                                println("File modified: ${File(event.filePath).name}")
                            is FileEvent.Deleted ->
                                println("File deleted: ${File(event.filePath).name}")
                        }
                    }
            }

        val delay = 20
        println("Monitoring started for $delay seconds. Make some file changes in $testDocsPath...")
        delay(delay.seconds)

        monitoringJob.cancel()
    } finally {
        indexer.close()
    }
}

// Example 4: Live Search with Real-time Updates
suspend fun liveSearchExample() {
    println("=== Live Search Example ===")

    val testDocsPath = "$userDir/file-examples"
    val indexer =
        FileIndexer.builder()
            .addPath(testDocsPath)
            .build()

    try {
        indexer.start()

        val todoWatchJob =
            CoroutineScope(Dispatchers.IO).launch {
                indexer.watchForWord("TODO")
                    .collect { result ->
                        println("TODO found in: ${File(result.file).name}")
                    }
            }

        val keywordWatchJob =
            CoroutineScope(Dispatchers.IO).launch {
                indexer.watchForWords(listOf("bug", "fix", "issue"))
                    .collect { results ->
                        println("Found ${results.size} files with bug-related keywords")
                        results.take(3).forEach { result ->
                            println("   - ${File(result.file).name}: ${result.matches.joinToString(", ")}")
                        }
                    }
            }

        val delay = 20
        println("Live search active for $delay seconds. Modify files to see real-time updates...")
        delay(delay.seconds)

        todoWatchJob.cancel()
        keywordWatchJob.cancel()
    } finally {
        indexer.close()
    }
}

// Example 5: Code Analysis Tool
class CodeAnalyzer {
    private val testDocsPath = "$userDir/file-examples"
    private val indexer =
        FileIndexer.builder()
            .addPath(testDocsPath)
            .fileFilter { path ->
                path.endsWith(".kt") || path.endsWith(".java")
            }
            .build()

    suspend fun start() {
        indexer.start()
    }

    suspend fun findDeprecatedUsage(): List<String> {
        return indexer.search(listOf("@Deprecated", "deprecated"))
            .map { File(it.file).absolutePath }
    }

    suspend fun findTestFiles(): List<String> {
        return indexer.search(listOf("@Test", "test", "Test"))
            .map { File(it.file).absolutePath }
    }

    suspend fun monitorCodeQuality() {
        val qualityKeywords = listOf("TODO", "FIXME", "HACK", "BUG")

        indexer.watchForWords(qualityKeywords)
            .collect { results ->
                val qualityIssues = results.groupBy { it.matches.first() }
                println("Code Quality Report:")
                qualityIssues.forEach { (keyword, files) ->
                    println("  $keyword: ${files.size} files")
                }
            }
    }

    fun close() = indexer.close()
}

suspend fun codeAnalysisExample() {
    println("=== Code Analysis Tool Example ===")

    val analyzer = CodeAnalyzer()

    try {
        analyzer.start()

        val deprecatedFiles = analyzer.findDeprecatedUsage()
        println("Files with deprecated code: ${deprecatedFiles.size}")

        val testFiles = analyzer.findTestFiles()
        println("Test files found: ${testFiles.size}")

        val qualityJob =
            CoroutineScope(Dispatchers.IO).launch {
                analyzer.monitorCodeQuality()
            }

        val delay = 20
        println("Live analysis active for $delay seconds. Modify files to see real-time updates...")
        delay(delay.seconds)
        qualityJob.cancel()
    } finally {
        analyzer.close()
    }
}

fun displayMenu() {
    println("\n" + "=".repeat(50))
    println("FileIndexer Library Examples")
    println("=".repeat(50))
    println("Choose an example to run:")
    println()
    println("1. Basic Usage Example")
    println("2. Advanced Configuration Example")
    println("3. File Monitoring Example")
    println("4. Live Search Example")
    println("5. Code Analysis Tool Example")
    println("0. Exit")
    println()
    print("Enter your choice (0-5): ")
}

suspend fun runExample(choice: Int): Boolean {
    return try {
        when (choice) {
            1 -> {
                basicUsageExample()
                true
            }
            2 -> {
                advancedConfigurationExample()
                true
            }
            3 -> {
                fileMonitoringExample()
                true
            }
            4 -> {
                liveSearchExample()
                true
            }
            5 -> {
                codeAnalysisExample()
                true
            }
            0 -> {
                println("Goodbye!")
                false
            }
            else -> {
                println("Invalid choice. Please enter a number between 0 and 5.")
                true
            }
        }
    } catch (e: Exception) {
        println("Error running example: ${e.message}")
        e.printStackTrace()
        true
    }
}

fun main() =
    runBlocking {
        println("Current directory: $userDir")
        println("Test documents should be located in: $userDir/file-examples")

        val testDocsDir = File("$userDir/file-examples")
        if (!testDocsDir.exists()) {
            println("\nWarning: file-examples directory not found!")
            println("Creating file-examples directory...")
            testDocsDir.mkdirs()
            println("Please add some test files to the file-examples directory and restart the application.")
            return@runBlocking
        }

        var continueRunning = true

        while (continueRunning) {
            displayMenu()

            val input = readLine()?.trim()
            val choice = input?.toIntOrNull()

            if (choice != null) {
                println()
                continueRunning = runExample(choice)

                if (continueRunning && choice != 0) {
                    println("\nPress Enter to continue...")
                    readLine()
                }
            } else {
                println("Invalid input. Please enter a number.")
            }
        }
    }
