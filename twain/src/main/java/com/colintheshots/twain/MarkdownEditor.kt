package com.colintheshots.twain

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.VibrationEffect.createOneShot
import android.os.VibrationEffect.createWaveform
import android.os.Vibrator
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.ContentInfoCompat
import androidx.core.view.OnReceiveContentListener
import androidx.core.view.ViewCompat
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.colintheshots.twain.handler.BlockQuoteEditHandler
import com.colintheshots.twain.handler.CodeEditHandler
import com.colintheshots.twain.handler.HeadingEditHandler
import com.colintheshots.twain.handler.LinkEditHandler
import com.colintheshots.twain.handler.LinkEditHandler.OnClick
import com.colintheshots.twain.handler.StrikethroughEditHandler
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.editor.MarkwonEditor
import io.noties.markwon.editor.MarkwonEditorTextWatcher
import io.noties.markwon.editor.handler.EmphasisEditHandler
import io.noties.markwon.editor.handler.StrongEmphasisEditHandler
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import java.lang.reflect.Field
import java.util.concurrent.Executors

/**
 * A custom Markwon Editor view for editing Markdown text with syntax highlighting.
 *
 * @param value markdown text
 * @param onValueChange callback to set the markdown text in Compose
 * @param modifier modifiers for Jetpack Compose view
 * @param charLimit maximum number of character to allow
 * @param maxLines maximum number of lines to display
 * @param inputType InputType to use for the EditText
 * @param hint string resource for the hint to show
 * @param setView callback to provide a reference to the underlying View
 * @param onLinkClick callback to handle clicks on links in the text
 */
@Composable
@NonRestartableComposable
fun MarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    charLimit: Int? = null,
    maxLines: Int? = null,
    inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
    @StringRes hint: Int? = null,
    setView: (EditText) -> Unit = {},
    onLinkClick: (String, String, TextRange) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION") val vibrator =
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val (lastCounter, setLastCounter) = rememberSaveable { mutableIntStateOf(value.length) }

    AndroidView(modifier = modifier, factory = { ctx ->
        createEditor(
            context = ctx,
            value = value,
            onValueChange = onValueChange,
            maxLines = maxLines,
            inputType = inputType,
            charLimit = charLimit,
            hint = hint,
            onLinkClick = onLinkClick
        )
    }, update = { editText ->
        setView(editText)
        if (maxLines != null) editText.maxLines = maxLines
        if (value != editText.text.toString()) {
            editText.setText(value)
        }
        updateCounterRemaining(
            value.length,
            lastCounter,
            vibrator,
            editText,
            setLastCounter
        )
    })
}

@Suppress("MagicNumber")
private fun updateCounterRemaining(
    counterValue: Int,
    lastCounter: Int,
    vibrator: Vibrator,
    editText: EditText,
    setLastCounter: (Int) -> Unit
) {
    if (counterValue != lastCounter) {
        when {
            counterValue in 1..10 -> {
                if (lastCounter > 10 && SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(createOneShot(150, 96))
                }
                editText.makeCursorColor(Color.Red.toArgb())
            }

            counterValue in -9..0 -> {
                if (lastCounter > 0 && SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        createWaveform(
                            arrayOf(150L, 200L).toLongArray(),
                            arrayOf(255, 255).toIntArray(),
                            -1
                        )
                    )
                }
                editText.makeCursorColor(Color.Red.toArgb())
            }

            counterValue <= -10 -> {
                if (lastCounter > -10 && SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        createWaveform(
                            arrayOf(150L, 200L).toLongArray(),
                            arrayOf(255, 255).toIntArray(),
                            -1
                        )
                    )
                }
                editText.makeCursorColor(Color.Red.toArgb())
            }

            else -> editText.makeCursorColor(editText.currentTextColor)
        }
        setLastCounter(counterValue)
    }
}

@SuppressLint(
    "DiscouragedPrivateApi",
    "PrivateApi"
) // we need reflection to change the cursor color pre-API 29
private fun EditText.makeCursorColor(argb: Int) {
    if (SDK_INT >= Build.VERSION_CODES.Q) {
        val wrapped = DrawableCompat.wrap(textCursorDrawable!!).mutate()
        DrawableCompat.setTint(wrapped, argb)
        wrapped.setBounds(0, 0, wrapped.intrinsicWidth, wrapped.intrinsicHeight)
        textCursorDrawable = wrapped
    } else {
        try {
            // Get the cursor resource id
            var field: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId: Int = field.getInt(this)

            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor: Any = field.get(this)!!

            // Get the drawable and set a color filter
            val drawable: Drawable = ContextCompat.getDrawable(context, drawableResId)!!
            @Suppress("DEPRECATION") // non-deprecated version is API 29+
            drawable.setColorFilter(argb, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf(drawable, drawable)

            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)
        } catch (ignored: Exception) {
        }
    }
}

private const val IMAGE_MEMORY_PERCENTAGE = 0.5

private fun imageKeyboardEditText(context: Context): EditText = object : EditText(context) {
    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic: InputConnection? = super.onCreateInputConnection(editorInfo)
        val allowedMimeTypes = arrayOf("image/gif", "image/jpeg", "image/png")
        EditorInfoCompat.setContentMimeTypes(editorInfo, allowedMimeTypes)

        ViewCompat.setOnReceiveContentListener(
            this,
            allowedMimeTypes,
            object : OnReceiveContentListener {
                override fun onReceiveContent(
                    view: View,
                    payload: ContentInfoCompat
                ): ContentInfoCompat? {
                    val (content, remaining) = payload.partition { item -> item.uri != null }
                    if (content == null) return remaining

                    val uri = content.linkUri
                    val description = content.clip.description
                    val newText = "![${description.label}]($uri)"
                    ic?.commitText(newText, (view as EditText).selectionEnd + newText.length)
                    return remaining
                }
            })
        return InputConnectionCompat.createWrapper(this, ic!!, editorInfo)
    }
}

@Suppress("LongParameterList")
private fun createEditor(
    context: Context,
    value: String,
    onValueChange: (String) -> Unit,
    maxLines: Int?,
    inputType: Int,
    charLimit: Int?,
    hint: Int?,
    onLinkClick: (String, String, TextRange) -> Unit
): EditText {
    val markwon = createMarkdownRender(context)
    val editor = MarkwonEditor.builder(markwon)
        .useEditHandler(EmphasisEditHandler())
        .useEditHandler(StrongEmphasisEditHandler())
        .useEditHandler(HeadingEditHandler())
        .useEditHandler(StrikethroughEditHandler())
        .useEditHandler(CodeEditHandler())
        .useEditHandler(BlockQuoteEditHandler())
        .useEditHandler(
            LinkEditHandler(object : OnClick {
                override fun onClick(widget: View, text: String, link: String, range: TextRange) {
                    onLinkClick(text, link, range)
                }
            })
        )
        .build()
    return imageKeyboardEditText(context).apply {
        movementMethod = LinksPlusArrowKeysMovementMethod.instance
        setBackgroundResource(android.R.color.transparent) // removes EditText underbar
        hint?.let { setHint(it) }
        maxLines?.let { setMaxLines(it) }
        setInputType(inputType)
        addTextChangedListener(
            MarkwonEditorTextWatcher.withPreRender(
                editor,
                Executors.newCachedThreadPool(),
                this
            )
        )
        setText(value)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                editable?.let { s ->
                    if (charLimit != null && s.length > charLimit) {
                        s.setSpan(
                            BackgroundColorSpan(OVERLIMIT_TEXT_COLOR),
                            charLimit,
                            s.length - 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        })
        accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
                    onValueChange(text.toString())
                }
            }
        }
    }
}

internal fun createMarkdownRender(context: Context): Markwon {
    val imageLoader = ImageLoader.Builder(context)
        .apply {
            memoryCache {
                MemoryCache.Builder(context).maxSizePercent(IMAGE_MEMORY_PERCENTAGE).build()
            }
            diskCache {
                DiskCache.Builder().directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(IMAGE_MEMORY_PERCENTAGE).build()
            }
            crossfade(true)
            components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory(enforceMinimumFrameDelay = true))
                } else {
                    add(GifDecoder.Factory(enforceMinimumFrameDelay = true))
                }
            }
            logger(DebugLogger())
        }.build()

    return Markwon.builder(context)
        .usePlugin(SoftBreakAddsNewLinePlugin.create())
        .usePlugin(HtmlPlugin.create())
        .usePlugin(CoilImagesPlugin.create(context, imageLoader))
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TablePlugin.create(context))
        .build()
}

const val OVERLIMIT_TEXT_COLOR = 0x33C80000
