package com.bf410.goaldaylocal.ui.book

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

data class RichEditorCommand(
    val name: String,
    val value: String? = null,
    val nonce: Long = System.nanoTime(),
)

@Suppress("DEPRECATION")
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
    val onHtmlChangeState = rememberUpdatedState(onHtmlChange)

    fun handleEditorChange(value: String) {
        val sanitized = sanitizeRichHtml(value)
        lastAppliedHtml = sanitized
        onHtmlChangeState.value(sanitized)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = false
                settings.allowFileAccessFromFileURLs = false
                settings.allowUniversalAccessFromFileURLs = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    settings.safeBrowsingEnabled = true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }
                isVerticalScrollBarEnabled = false
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript("RE.setPlaceholder(${placeholder.asJsString()});", null)
                        view?.evaluateJavascript("RE.setHtml(${sanitizedHtml.asJsString()});", null)
                        lastAppliedHtml = sanitizedHtml
                        initialHtmlLoaded = true
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val uri = request?.url ?: return true
                        if (uri.scheme == EDITOR_CALLBACK_SCHEME) {
                            handleEditorChange(uri.getQueryParameter("html").orEmpty())
                            return true
                        }
                        return !isAllowedEditorAssetUrl(uri.toString())
                    }
                }
                loadUrl(EDITOR_ASSET_URL)
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

internal fun sanitizeRichHtml(raw: String): String {
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
        Regex("(?i)\\s+(href|src)\\s*=\\s*(\"[^\"]*(javascript|data):[^\"]*\"|'[^']*(javascript|data):[^']*'|(javascript|data):[^\\s>]+)"),
        "",
    )
    return html
}

private fun isAllowedEditorAssetUrl(url: String): Boolean =
    url == EDITOR_ASSET_URL ||
        url == "file:///android_asset/normalize.css" ||
        url == "file:///android_asset/style.css" ||
        url == "file:///android_asset/rich_editor.js"

private const val EDITOR_CALLBACK_SCHEME = "goalday-editor"
private const val EDITOR_ASSET_URL = "file:///android_asset/editor.html"
private const val MAX_RICH_HTML_LENGTH = 20_000
