package com.bf410.goaldaylocal.ui.book

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
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
    val nonce: Long = System.nanoTime(),
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
    val sanitizedHtml = remember(html) { sanitizeRichHtml(html) }
    var initialHtmlLoaded by remember { mutableStateOf(false) }
    var lastAppliedHtml by remember { mutableStateOf<String?>(null) }
    var appliedCommandCount by remember { mutableIntStateOf(0) }
    val bridge = remember {
        object {
            @JavascriptInterface
            fun onChange(value: String) {
                val sanitized = sanitizeRichHtml(value)
                lastAppliedHtml = sanitized
                onHtmlChange(sanitized)
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowContentAccess = false
                isVerticalScrollBarEnabled = false
                addJavascriptInterface(bridge, "AndroidEditor")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsString()});", null)
                        view?.evaluateJavascript("RE.setHtml(${sanitizedHtml.asJsString()});", null)
                        lastAppliedHtml = sanitizedHtml
                        initialHtmlLoaded = true
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
                        request?.url?.scheme != "file"
                }
                loadUrl("file:///android_asset/editor.html")
            }
        },
        update = { webView ->
            webView.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsString()});", null)

            if (initialHtmlLoaded && sanitizedHtml != lastAppliedHtml) {
                webView.evaluateJavascript("RE.setHtml(${sanitizedHtml.asJsString()});", null)
                lastAppliedHtml = sanitizedHtml
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

private fun sanitizeRichHtml(raw: String): String {
    var html = raw.take(MAX_RICH_HTML_LENGTH)
    html = html.replace(
        Regex("(?is)<\\s*(script|style|iframe|object|embed|link|meta)[^>]*>.*?<\\s*/\\s*\\1\\s*>"),
        "",
    )
    html = html.replace(
        Regex("(?is)<\\s*(script|style|iframe|object|embed|link|meta)[^>]*?/?>"),
        "",
    )
    html = html.replace(
        Regex("\\s+on[a-zA-Z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)"),
        "",
    )
    html = html.replace(
        Regex("(?i)\\s+(href|src)\\s*=\\s*(\"[^\"]*javascript:[^\"]*\"|'[^']*javascript:[^']*'|javascript:[^\\s>]+)"),
        "",
    )
    return html
}

private const val MAX_RICH_HTML_LENGTH = 20_000
