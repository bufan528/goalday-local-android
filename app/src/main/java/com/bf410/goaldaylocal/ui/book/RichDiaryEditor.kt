package com.bf410.goaldaylocal.ui.book

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

data class RichEditorCommand(
    val name: String,
    val value: String? = null,
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RichDiaryEditor(
    html: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    pendingCommand: RichEditorCommand? = null,
    onHtmlChange: (String) -> Unit,
) {
    var initialHtmlLoaded by remember { mutableStateOf(false) }
    var lastAppliedHtml by remember { mutableStateOf<String?>(null) }
    var appliedCommandCount by remember { mutableIntStateOf(0) }
    val bridge = remember {
        object {
            @JavascriptInterface
            fun onChange(value: String) {
                lastAppliedHtml = value
                onHtmlChange(value)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                isVerticalScrollBarEnabled = false
                addJavascriptInterface(bridge, "AndroidEditor")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsString()});", null)
                        view?.evaluateJavascript("RE.setHtml(${html.asJsString()});", null)
                        lastAppliedHtml = html
                        initialHtmlLoaded = true
                    }
                }
                loadUrl("file:///android_asset/editor.html")
            }
        },
        update = { webView ->
            webView.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsString()});", null)

            if (initialHtmlLoaded && html != lastAppliedHtml) {
                webView.evaluateJavascript("RE.setHtml(${html.asJsString()});", null)
                lastAppliedHtml = html
            }

            if (pendingCommand != null) {
                val commandKey = pendingCommand.hashCode()
                if (appliedCommandCount != commandKey) {
                    val value = pendingCommand.value?.asJsString() ?: "null"
                    webView.evaluateJavascript("RE.command(${pendingCommand.name.asJsString()}, $value);", null)
                    appliedCommandCount = commandKey
                }
            }
        },
    )
}

private fun String.asJsString(): String =
    "'" + replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "") + "'"
