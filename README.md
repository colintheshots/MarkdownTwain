# MarkdownTwain

MarkdownTwain is an open source Android software library that provides an easy-to-use syntax highlighting editor and viewer for Markdown text using Jetpack Compose for Android. This library is based upon the existing Markwon library for Android Views.

With MarkdownTwain, developers can easily add a Markdown editor and viewer to their Android applications, allowing users to easily format and style their text with Markdown syntax.

Note: MarkdownTwain was created by Colin Lee while working at Meetup.com. The package was migrated to colintheshots.com along with the change in Meetup ownership. Maven package information has changed. Use the new com.colintheshots name to get the latest updates. Also, I also fixed issues preventing using older artifacts on Maven Central.

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
    implementation("com.colintheshots:twain:0.3.1")
}
```
</details>
<details>
<summary>Groovy</summary>

```groovy
dependencies {
    implementation 'com.colintheshots:twain:0.3.1'
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
    Card {
        MarkdownEditor(
            value = textFieldValue.value,
            onValueChange = { value ->
                textFieldValue.value = textFieldValue.value.copy(text = value)
            },
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
