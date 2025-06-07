package com.itsvks.code.core

data class FoldableRange(
    val startLine: Int,
    val endLine: Int,
    // val startColumn: Int, // Let's omit column for now for simplicity
    val type: String = "braces" // For future expansion (e.g., "imports", "comments")
)

fun findBraceFoldableRanges(lines: List<String>): List<FoldableRange> {
    val ranges = mutableListOf<FoldableRange>()
    val openBraceStack = ArrayDeque<Pair<Int, Int>>() // Pair of (lineIndex, charIndexInLine)

    lines.forEachIndexed { lineIndex, line ->
        line.forEachIndexed { charIndex, char ->
            if (char == '{') {
                openBraceStack.addLast(Pair(lineIndex, charIndex))
            } else if (char == '}') {
                if (openBraceStack.isNotEmpty()) {
                    val (startLine, _) = openBraceStack.removeLast()
                    // Add if the range spans multiple lines and is not empty
                    if (startLine < lineIndex && lineIndex > startLine + 1) {
                        // Ensure at least one line is hidden (startLine, startLine+1 ... lineIndex)
                        // Foldable if it hides startLine+1 to lineIndex-1
                        ranges.add(FoldableRange(startLine = startLine, endLine = lineIndex))
                    }
                }
            }
        }
    }
    // Sort ranges by start line, then by end line descending for nested blocks
    // This helps in processing them later if needed.
    return ranges.sortedWith(compareBy({ it.startLine }, { -it.endLine }))
}
