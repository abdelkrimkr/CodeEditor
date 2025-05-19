package com.itsvks.code.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RopeTest {
    @Test
    fun testLeafCreationFromString() {
        val rope = Rope.fromString("hello\nworld")
        assertEquals(11, rope.length)
        assertEquals(1, rope.lineCount)
        assertEquals('h', rope.charAt(0))
        assertEquals('d', rope.charAt(10))
        assertEquals("hello\nworld", rope.toString())
    }

    @Test
    fun testLeafCreationFromCharArray() {
        val chars = "foo\nbar\nbaz".toCharArray()
        val rope = Rope.fromCharArray(chars, 0, chars.size)
        assertEquals(11, rope.length)
        assertEquals(2, rope.lineCount)
        assertEquals("foo\nbar\nbaz", rope.toString())
    }

    @Test
    fun testNodeStructure() {
        val rope = Rope.fromString("a".repeat(2000))
        assertTrue(rope is Rope.Node)
        assertEquals(2000, rope.length)
    }

    @Test
    fun testCharAt() {
        val rope = Rope.fromString("KotlinRope")
        for (i in 0 until rope.length) {
            assertEquals("KotlinRope"[i], rope.charAt(i))
        }
    }

    @Test
    fun testSlice() {
        val rope = Rope.fromString("abcdefg")
        val sub = rope.slice(2, 5)
        assertEquals("cde", sub.toString())
    }

    @Test
    fun testSliceRange() {
        val rope = Rope.fromString("abcdefg")
        val sub = rope.slice(2 .. 4)
        assertEquals("cde", sub.toString())
    }

    @Test
    fun testSplit() {
        val rope = Rope.fromString("abcde")
        val (left, right) = rope.split(2)
        assertEquals("ab", left.toString())
        assertEquals("cde", right.toString())
    }

    @Test
    fun testInsert() {
        val rope = Rope.fromString("HelloWorld")
        val inserted = rope.insert(5, " ")
        assertEquals("Hello World", inserted.toString())
    }

    @Test
    fun testRemove() {
        val rope = Rope.fromString("Hello Beautiful World")
        val removed = rope.remove(6, 16)
        assertEquals("Hello World", removed.toString())
    }

    @Test
    fun testRemoveRange() {
        val rope = Rope.fromString("Hello Beautiful World")
        val removed = rope.remove(6 .. 15)
        assertEquals("Hello World", removed.toString())
    }

    @Test
    fun testLineExtraction() {
        val rope = Rope.fromString("line1\nline2\nline3")
        assertEquals("line1\n", rope.line(0).toString())
        assertEquals("line2\n", rope.line(1).toString())
        assertEquals("line3", rope.line(2).toString())
    }

    @Test
    fun testLineIndexOutOfBounds() {
        val rope = Rope.fromString("one\ntwo")
        assertFailsWith<IllegalArgumentException> {
            rope.line(3)
        }
    }

    @Test
    fun testEqualsAndClone() {
        val original = Rope.fromString("clone me")
        val clone = original.clone()
        assertEquals(original.toString(), clone.toString())
        assertEquals(original.length, clone.length)
        assertEquals(original.lineCount, clone.lineCount)
        assertEquals(original, clone)
    }

    @Test
    fun testEmptyString() {
        val empty = Rope.fromString("")
        assertEquals(0, empty.length)
        assertEquals(0, empty.lineCount)
        assertTrue(empty.toString().isEmpty())
    }

    @Test
    fun testMaxLeafSizeLimit() {
        val text = "a".repeat(513)
        val rope = Rope.fromString(text)
        assertTrue(rope is Rope.Node)
    }

    @Test
    fun testConcat() {
        val a = Rope.fromString("abc")
        val b = Rope.fromString("def")
        val c = a + b
        assertEquals("abcdef", c.toString())
    }

    @Test
    fun testLeafLineCount() {
        val rope = Rope.Leaf("line1\nline2\nline3")
        assertEquals(2, rope.lineCount)
    }

    @Test
    fun testLeafCharAtOutOfBounds() {
        val rope = Rope.Leaf("abc")
        assertFailsWith<IllegalArgumentException> { rope[3] }
        assertFailsWith<IllegalArgumentException> { rope[-1] }
    }

    @Test
    fun testNodeLengthAndLineCount() {
        val left = Rope.Leaf("hello\n")
        val right = Rope.Leaf("world")
        val node = Rope.Node(left, right)

        assertEquals(11, node.length)
        assertEquals(1, node.lineCount)
    }

    @Test
    fun testFromCharArrayCreatesNode() {
        val str = "a".repeat(1024)
        val rope = Rope.fromCharArray(str.toCharArray(), 0, str.length)
        assertTrue(rope is Rope.Node)
        assertEquals(str.length, rope.length)
    }

    @Test
    fun testFromFileLoadsText() {
        val file = createTempFile().apply {
            writeText("line1\nline2\nline3")
            deleteOnExit()
        }

        val rope = Rope.fromFile(file)
        assertEquals(17, rope.length)
        assertEquals(2, rope.lineCount)
    }

    @Test
    fun testSplitInMiddle() {
        val rope = Rope.fromString("hello world")
        val (left, right) = rope.split(5)

        assertEquals("hello", left.toString())
        assertEquals(" world", right.toString())
    }

    @Test
    fun testSplitAtZeroAndLength() {
        val rope = Rope.fromString("abc")
        val (left0, right0) = rope.split(0)
        val (leftN, rightN) = rope.split(3)

        assertEquals("", left0.toString())
        assertEquals("abc", right0.toString())
        assertEquals("abc", leftN.toString())
        assertEquals("", rightN.toString())
    }

    @Test
    fun testRemoveMiddleSection() {
        val rope = Rope.fromString("hello world")
        val result = rope.remove(5, 6)

        assertEquals("helloworld", result.toString())
    }

    @Test
    fun testRemoveEntireText() {
        val rope = Rope.fromString("clear")
        val result = rope.remove(0, rope.length)

        assertEquals("", result.toString())
    }

    @Test
    fun testInsertAtStartMiddleEnd() {
        val base = Rope.fromString("ace")

        val start = base.insert(0, "X")
        val middle = base.insert(1, "X")
        val end = base.insert(3, "X")

        assertEquals("Xace", start.toString())
        assertEquals("aXce", middle.toString())
        assertEquals("aceX", end.toString())
    }

    @Test
    fun testSliceExtractSubstring() {
        val rope = Rope.fromString("abcdef")
        val slice = rope.slice(1, 4)

        assertEquals("bcd", slice.toString())
    }

    @Test
    fun testSliceWholeRope() {
        val rope = Rope.fromString("test")
        val sliced = rope.slice(0, rope.length)

        assertEquals(rope.toString(), sliced.toString())
    }

    @Test
    fun testToStringMatchesInput() {
        val str = "some\nmultiline\ntext"
        val rope = Rope.fromString(str)

        assertEquals(str, rope.toString())
    }

    @Test
    fun testPlusOperatorConcatenates() {
        val a = Rope.fromString("abc")
        val b = Rope.fromString("def")

        val result = a + b
        assertEquals("abcdef", result.toString())
    }

    @Test
    fun testLineLengthSingleLine() {
        val rope = Rope.fromString("hello")
        assertEquals(5, rope.lineLength(0))
    }

    @Test
    fun testLineLengthWithNewlines() {
        val rope = Rope.fromString("line1\nline2\nlongerLine3\n")
        assertEquals(6, rope.lineLength(0)) // "line1\n"
        assertEquals(6, rope.lineLength(1)) // "line2\n"
        assertEquals(12, rope.lineLength(2)) // "longerLine3\n"
    }

    @Test
    fun testLineLengthWithoutTrailingNewline() {
        val rope = Rope.fromString("a\nbb\nccc")
        assertEquals(2, rope.lineLength(0))  // "a\n"
        assertEquals(3, rope.lineLength(1))  // "bb\n"
        assertEquals(3, rope.lineLength(2))  // "ccc"
    }

    @Test
    fun testLineLengthThrowsOnInvalidIndex() {
        val rope = Rope.fromString("abc\ndef")
        assertFailsWith<IllegalArgumentException> {
            rope.lineLength(-1)
        }
        assertFailsWith<IllegalArgumentException> {
            rope.lineLength(3) // only 2 lines
        }
    }

    @Test
    fun testMaxLineLengthBasic() {
        val rope = Rope.fromString("a\nbb\nccc\n")
        assertEquals(3, rope.maxLineLength())
    }

    @Test
    fun testMaxLineLengthNoNewlines() {
        val rope = Rope.fromString("longline")
        assertEquals(8, rope.maxLineLength())
    }

    @Test
    fun testMaxLineLengthWithEmptyLines() {
        val rope = Rope.fromString("a\n\n\nabcde\nf\n")
        assertEquals(5, rope.maxLineLength())
    }

    @Test
    fun testLineLengthUpToValidCases() {
        val rope = Rope.fromString("ab\ncde\nfghi")
        assertEquals(1, rope.lineColToChar(0, 1)) // line 0: "ab", col 1 → 1
        assertEquals(4, rope.lineColToChar(1, 1)) // line 1 starts at 3 → 3 + 1
        assertEquals(9, rope.lineColToChar(2, 2)) // line 2 starts at 7 → 7 + 2
    }

    @Test
    fun testLineLengthUpToAtLineStart() {
        val rope = Rope.fromString("a\nb\nc")
        assertEquals(0, rope.lineColToChar(0, 0))
        assertEquals(2, rope.lineColToChar(1, 0))
        assertEquals(4, rope.lineColToChar(2, 0))
    }

    @Test
    fun testLargeFiles() {
        val fileContent = """
            fn main() {
                // In general, the `{}` will be automatically replaced with any
                // arguments. These will be stringified.
                println!("{} days", 31);

                // Positional arguments can be used. Specifying an integer inside `{}`
                // determines which additional argument will be replaced. Arguments start
                // at 0 immediately after the format string.
                println!("{0}, this is {1}. {1}, this is {0}", "Alice", "Bob");

                // As can named arguments.
                println!("{subject} {verb} {object}",
                         object="the lazy dog",
                         subject="the quick brown fox",
                         verb="jumps over");

                // Different formatting can be invoked by specifying the format character
                // after a `:`.
                println!("Base 10:               {}",   69420); // 69420
                println!("Base 2 (binary):       {:b}", 69420); // 10000111100101100
                println!("Base 8 (octal):        {:o}", 69420); // 207454
                println!("Base 16 (hexadecimal): {:x}", 69420); // 10f2c

                // You can right-justify text with a specified width. This will
                // output "    1". (Four white spaces and a "1", for a total width of 5.)
                println!("{number:>5}", number=1);

                // You can pad numbers with extra zeroes,
                println!("{number:0>5}", number=1); // 00001
                // and left-adjust by flipping the sign. This will output "10000".
                println!("{number:0<5}", number=1); // 10000

                // You can use named arguments in the format specifier by appending a `${'$'}`.
                println!("{number:0>width${'$'}}", number=1, width=5);

                // Rust even checks to make sure the correct number of arguments are used.
                println!("My name is {0}, {1} {0}", "Bond");
                // FIXME ^ Add the missing argument: "James"

                // Only types that implement fmt::Display can be formatted with `{}`. User-
                // defined types do not implement fmt::Display by default.

                #[allow(dead_code)] // disable `dead_code` which warn against unused module
                struct Structure(i32);

                // This will not compile because `Structure` does not implement
                // fmt::Display.
                // println!("This struct `{}` won't print...", Structure(3));
                // TODO ^ Try uncommenting this line

                // For Rust 1.58 and above, you can directly capture the argument from a
                // surrounding variable. Just like the above, this will output
                // "    1", 4 white spaces and a "1".
                let number: f64 = 1.0;
                let width: usize = 5;
                println!("{number:>width${'$'}}");
            }
        """.trimIndent()
        val file = createTempFile().apply {
            writeText(fileContent)
            deleteOnExit()
        }
        val rope = Rope.fromFile(file)

        assertEquals(fileContent, rope.toString())
        assertEquals(56, rope.lineCount)
        assertEquals(fileContent.length, rope.length)
        assertEquals(79, rope.maxLineLength())
        assertEquals(66, rope.lineLength(47))
    }
}
