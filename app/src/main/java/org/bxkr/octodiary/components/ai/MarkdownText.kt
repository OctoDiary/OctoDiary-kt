package org.bxkr.octodiary.components.ai

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Простой Markdown рендерер для AI ответов
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = parseMarkdown(text),
        modifier = modifier.fillMaxWidth(),
        color = color,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Парсинг Markdown в AnnotatedString
 */
private fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val lines = text.split("\n")
        
        lines.forEachIndexed { index, line ->
            when {
                // Заголовки
                line.startsWith("### ") -> {
                    pushStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    append(line.substring(4))
                    pop()
                }
                line.startsWith("## ") -> {
                    pushStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                    append(line.substring(3))
                    pop()
                }
                line.startsWith("# ") -> {
                    pushStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold))
                    append(line.substring(2))
                    pop()
                }
                // Списки
                line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ") -> {
                    append("• ")
                    parseInlineMarkdown(line.trimStart().substring(2))
                }
                // Нумерованные списки
                line.trimStart().matches(Regex("^\\d+\\.\\s.*")) -> {
                    val num = line.trimStart().takeWhile { it.isDigit() || it == '.' }
                    append("$num ")
                    parseInlineMarkdown(line.trimStart().substring(num.length + 1))
                }
                // Код блок
                line.trim().startsWith("```") -> {
                    pushStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color.Gray.copy(alpha = 0.2f)
                    ))
                    append(line.replace("```", ""))
                    pop()
                }
                // Обычный текст
                else -> {
                    parseInlineMarkdown(line)
                }
            }
            
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

/**
 * Парсинг inline Markdown (жирный, курсив, код)
 */
private fun AnnotatedString.Builder.parseInlineMarkdown(text: String) {
    var i = 0
    while (i < text.length) {
        when {
            // Жирный текст **text**
            text.substring(i).startsWith("**") -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(i + 2, end))
                    pop()
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            // Курсив *text*
            text.substring(i).startsWith("*") && !text.substring(i).startsWith("**") -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1 && !text.substring(end).startsWith("**")) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Inline код `code`
            text.substring(i).startsWith("`") -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color.Gray.copy(alpha = 0.2f)
                    ))
                    append(text.substring(i + 1, end))
                    pop()
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            // Обычный символ
            else -> {
                append(text[i])
                i++
            }
        }
    }
}
