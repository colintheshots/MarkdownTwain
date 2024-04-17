package com.colintheshots.twain

import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.colintheshots.twain.ui.theme.MarkdownTwainTheme

private const val CHAR_LIMIT = 2000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val exampleText = stringResource(R.string.example_markdown_text)
            val textFieldValue = rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(exampleText))
            }
            val counterValue = CHAR_LIMIT - textFieldValue.value.text.length
            val view = remember { mutableStateOf<EditText?>(null) }

            MarkdownTwainTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    Card(border = BorderStroke(1.dp, Color.Black)) {
                        Box {
                            MarkdownEditor(
                                value = textFieldValue.value.text,
                                onValueChange = { value ->
                                    textFieldValue.value = textFieldValue.value.copy(text = value)
                                },
                                charLimit = CHAR_LIMIT,
                                maxLines = 8,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                hint = R.string.markdown_entry_hint,
                                setView = { view.value = it }
                            )
                            Text(
                                text = counterValue.toString(),
                                style = if (counterValue < 1) MaterialTheme.typography.body1.copy(
                                    color = Color.Red
                                ) else MaterialTheme.typography.body1,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 8.dp, bottom = 2.dp)
                            )
                        }
                    }

                    Text(
                        stringResource(R.string.preview_markdown_heading),
                        style = MaterialTheme.typography.h3.copy(
                            color = MaterialTheme.colors.secondary
                        ),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    MarkdownText(
                        markdown = textFieldValue.value.text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .weight(1f)
                    )
                }
            }
        }
    }
}
