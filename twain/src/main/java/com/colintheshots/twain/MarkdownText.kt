package com.colintheshots.twain

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.annotation.IdRes
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import io.noties.markwon.Markwon

/**
 * Based on the MarkdownText library for Jetpack Compose, this Composable offers a viewer for rendered Markdown
 *
 * @param markdown markdown text to render
 * @param modifier modifiers for Jetpack Compose view
 * @param color color override for the text style
 * @param fontSize font size override for the text style
 * @param textAlign alignment for the rendered text
 * @param maxLines maximum number of lines to display
 * @param fontResource font resource ID to use
 * @param style text style to use
 * @param viewId view id to assign to the TextView
 * @param textTruncated callback for determining if the text is truncated
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    @FontRes fontResource: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    textTruncated: (Boolean) -> Unit = {}
) {
    val defaultColor: Color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
    val context: Context = LocalContext.current
    val markdownRender: Markwon = remember { createMarkdownRender(context) }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            createTextView(
                context = ctx,
                color = color,
                defaultColor = defaultColor,
                fontSize = fontSize,
                textAlign = textAlign,
                maxLines = maxLines,
                fontResource = fontResource,
                style = style,
                viewId = viewId,
                textTruncated = textTruncated
            )
        },
        update = { textView ->
            markdownRender.setMarkdown(textView, markdown)
        }
    )
}

private fun createTextView(
    context: Context,
    color: Color = Color.Unspecified,
    defaultColor: Color,
    fontSize: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    @FontRes fontResource: Int? = null,
    style: TextStyle,
    @IdRes viewId: Int? = null,
    textTruncated: (Boolean) -> Unit
): TextView {

    val textColor = color.takeOrElse { style.color.takeOrElse { defaultColor } }
    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = fontSize,
            textAlign = textAlign,
        )
    )
    return TextView(context).apply {

        setTextColor(textColor.toArgb())
        setMaxLines(maxLines)
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mergedStyle.fontSize.value)

        viewId?.let { id = viewId }
        textAlign?.let { align ->
            textAlignment = when (align) {
                TextAlign.Left, TextAlign.Start -> View.TEXT_ALIGNMENT_TEXT_START
                TextAlign.Right, TextAlign.End -> View.TEXT_ALIGNMENT_TEXT_END
                TextAlign.Center -> View.TEXT_ALIGNMENT_CENTER
                else -> View.TEXT_ALIGNMENT_TEXT_START
            }
        }

        fontResource?.let { font ->
            typeface = ResourcesCompat.getFont(context, font)
        }

        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val isTruncated = (layout?.lineCount ?: 0) > maxLines
            textTruncated(isTruncated)
            if (isTruncated && text.substring(text.lastIndex) != "\u2026") {
                // Workaround known issue with spannables and ellipsize https://github.com/noties/Markwon/issues/180
                val end = layout.getLineEnd(maxLines - 1)
                val newVal = text.subSequence(0, end - 1)
                setText(newVal, TextView.BufferType.SPANNABLE)
                append("\u2026")
            }
        }

        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                val isTruncated = (layout?.lineCount ?: 0) > maxLines
                textTruncated(isTruncated)
            }
        })
    }
}
