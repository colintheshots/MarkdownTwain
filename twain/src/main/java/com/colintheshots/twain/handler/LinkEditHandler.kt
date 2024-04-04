package com.colintheshots.twain.handler

import android.text.Editable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.compose.ui.text.TextRange
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.PersistedSpans

class LinkEditHandler(private val onClick: OnClick) : AbstractEditHandler<LinkSpan>() {
    interface OnClick {
        fun onClick(widget: View, text: String, link: String, range: TextRange)
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(EditLinkSpan::class.java) {
            EditLinkSpan(
                onClick
            )
        }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: LinkSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val editLinkSpan = persistedSpans[EditLinkSpan::class.java]
        editLinkSpan.link = span.link

        // First first __letter__ to find link content (scheme start in URL, receiver in email address)
        // NB! do not use phone number auto-link (via LinkifyPlugin) as we cannot guarantee proper link
        //  display. For example, we _could_ also look for a digit, but:
        //  * if phone number start with special symbol, we won't have it (`+`, `(`)
        //  * it might interfere with an ordered-list
        var start = -1
        var i = spanStart
        val length = input.length
        while (i < length) {
            if (Character.isLetter(input[i])) {
                start = i
                break
            }
            i++
        }
        var text = ""
        while (i < length) {
            if (input[i] == ']') break
            text += input[i]
            i++
        }
        editLinkSpan.text = text
        if (start > -1) {
            editable.setSpan(
                editLinkSpan,
                start,
                start + spanTextLength,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        var end = start
        while (i < length) {
            if (input[i] == ')') {
                end = ++i
                break
            }
            i++
        }
        if (end > start) {
            editLinkSpan.range = TextRange(spanStart, end)
        }
    }

    override fun markdownSpanType(): Class<LinkSpan> {
        return LinkSpan::class.java
    }

    internal class EditLinkSpan(private val onClick: OnClick) : ClickableSpan() {
        var link: String? = null
        var text: String? = null
        var range: TextRange? = null
        override fun onClick(widget: View) {
            val myLink = link
            val myText = text
            val myRange = range
            if (myLink != null && myText != null && myRange != null) {
                onClick.onClick(widget, myText, myLink, myRange)
            }
        }
    }
}
