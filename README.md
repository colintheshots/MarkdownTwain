# MarkdownTwain

MarkdownTwain is an open source Android software library that provides an easy-to-use syntax highlighting editor and viewer for Markdown text using Jetpack Compose for Android. This library is based upon the existing Markwon library for Android Views.

With MarkdownTwain, developers can easily add a Markdown editor and viewer to their Android applications, allowing users to easily format and style their text with Markdown syntax.

## Features

- Syntax highlighting for Markdown text
- Preview of formatted text in real-time
- Easy-to-use editor and viewer components
- Customizable styling options
- Based on the popular Markwon library for Android Views

## Demo

https://user-images.githubusercontent.com/626405/230183141-e41033f1-26ba-44bf-adcb-fac65ba649a3.mp4

## Usage

To use MarkdownTwain in your Android project, follow these steps:

1. Add the following dependency to your app's `build.gradle.kts` or `build.gradle` file:

<details open>
<summary>Kotlin</summary>

```kotlin
dependencies {
    implementation("com.meetup:twain:0.2.1")
}
```
</details>
<details>
<summary>Groovy</summary>

```groovy
dependencies {
    implementation 'com.meetup:twain:0.2.1'
}
```

</details>

2. Use the `MarkdownEditor()` or `MarkdownText()` Composables in your Jetpack Compose layouts. There are extra attributes available for customizing the display.

<details open>
<summary>MarkdownEditor</summary>

```kotlin
    val textFieldValue = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf("")
    }
    val charLimit = 2000
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        border = BorderStroke(1.dp, Color.Black),
        elevation = 0.dp
    ) {
        MarkdownEditor(
            value = textFieldValue.value,
            onValueChange = { value -> textFieldValue.value = value.copy(text = value.text) },
            modifier = Modifier.fillMaxWidth()
        )
    }
```

</details>

<details open>
<summary>MarkdownText</summary>

```kotlin
    MarkdownText(
        markdown = textFieldValue.value.text,
        modifier = Modifier.fillMaxWidth()
    )
```

</details>

## License
MarkdownTwain is licensed under the Apache 2.0 License. See the LICENSE file for details.

## Acknowledgments
MarkdownTwain is based upon the [Markwon](https://github.com/noties/Markwon) library for Android Views. Special thanks to [Dimitry](https://github.com/noties) and all contributors to the Markwon project.
