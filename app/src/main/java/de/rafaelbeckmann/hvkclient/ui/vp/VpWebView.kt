package de.rafaelbeckmann.hvkclient.ui.vp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Base64
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

// TODO: improve this
// TODO: add caching
@Composable
fun VpWebView(
    modifier: Modifier = Modifier,
    url1: String = "https://www.kleist-schule.de/vertretungsplan/schueler/aktuelle%20plaene/1/vp.html",
    url2: String = "https://www.kleist-schule.de/vertretungsplan/schueler/aktuelle%20plaene/2/vp.html",
    ) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Heute", "Nächster Schultag")

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        ) {
        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
        ) { page ->
            val pageUrl = if (page == 0) url1 else url2

            // LazyColumn necessary to fix vertical scroll bug (will always scroll pages and not lock in the vertical direction)
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    WebViewWithLoadingIndicator(
                        url = pageUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebViewWithLoadingIndicator(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }

    val themeBg = MaterialTheme.colorScheme.background
    val themeFg = MaterialTheme.colorScheme.onBackground
    val themeBgHighlight = MaterialTheme.colorScheme.surfaceVariant

    // Build CSS from theme colors and encode
    val encodedCss by remember(themeBg, themeFg, themeBgHighlight) {
        fun toRgba(color: Color): String {
            val r = (color.red * 255).toInt()
            val g = (color.green * 255).toInt()
            val b = (color.blue * 255).toInt()
            val a = color.alpha
            return "rgba($r,$g,$b,$a)"
        }

        val css = """
            html, body {
              padding-top: 10px !important;
              padding-bottom: 10 !important;
              background-color: ${toRgba(themeBg)} !important;
              text-align: center !important;
            }
            table, th, td {
              margin: auto;
              border: 1px solid;
              border-collapse: collapse;
              text-align: center !important;
            }
            th {
              background-color: ${toRgba(themeBgHighlight)} !important;
            }
            th, td {
              padding: 2px !important;
            }
            html, body, table, td, th, div, span, p, a {
              color: ${toRgba(themeFg)} !important;
            }
            a { display: none !important; }
            br {
                display: none;
            }
        """.trimIndent()

        mutableStateOf(Base64.encodeToString(css.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
    }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.displayZoomControls = false

            clearCache(true) // this should be unnecessary
        }
    }

    // Keep background in sync with theme changes
    DisposableEffect(themeBg) {
        webView.setBackgroundColor(themeBg.toArgb())
        onDispose { }
    }

    // Inject CSS helper
    fun injectThemeCss() {
        val js = """
            (function() {
              try {
                let style = document.getElementById('vp-css');
                if (!style) {
                  style = document.createElement('style');
                  style.id = 'vp-css';
                  style.type = 'text/css';
                  document.head.appendChild(style);
                }
                style.textContent = atob('$encodedCss');
              } catch (e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    // Update CSS on theme change if a page is already loaded
    DisposableEffect(encodedCss) {
        injectThemeCss()
        onDispose { }
    }

    DisposableEffect(url) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                isLoading = true
            }
            // Use first-paint callback to avoid showing blank/white before content is drawn
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                injectThemeCss()
                canGoBack = webView.canGoBack()
                isLoading = false
            }
            // TODO: this should be unnecessary
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.clearCache(true)
            }
        }
        if (webView.url != url) {
            isLoading = true
            webView.loadUrl(url)
        }
        onDispose { }
    }

    DisposableEffect(webView) {
        onDispose { webView.destroy() }
    }

    BackHandler(enabled = canGoBack) {
        if (webView.canGoBack()) {
            webView.goBack()
            canGoBack = webView.canGoBack()
        }
    }

    Box(modifier.fillMaxSize()) {

        // TODO: implement PullToRefresh
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { }
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { webView }
            )
        }
    }
}
