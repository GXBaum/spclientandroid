package de.rafaelbeckmann.hvkclient.ui.vp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Base64
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.vp.VpStyle.encoded
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream

// TODO: improve this
// TODO: add caching

// TODO: bug explanation (my theory): Adding a vertically scrollable WebView into a Horizontal Pager makes scrolling almost impossible, because it doesn't realise the WebView needs the vertical scroll, and treats it as a non-scrollable element. It therefore interprets everything as an horizontal swipe. Adding the .verticalScroll Modifier is a band-aid fix, as it scrolls the entire WebView Component and does not use the internal WebView scroll. These two sometimes conflict, resulting in the bug where it's sometimes impossible to scroll to the absolute top of the website, it treats some random point before that as the top. This is why I ignore the scrolling inside it. That's my (propably wrong) theory.
// in this state, it is currently working as expected.
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
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val pageUrl = if (page == 0) url1 else url2

            Box (
                modifier = Modifier.fillMaxSize()
            ) {
                WebViewWithLoadingIndicator(
                    url = pageUrl,
                    //modifier = Modifier.matchParentSize()
                    modifier = Modifier.fillMaxSize()
                )
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorCode by remember { mutableStateOf<Int?>(null) }

    val themeBg = MaterialTheme.colorScheme.background
    val themeFg = MaterialTheme.colorScheme.onBackground
    val themeBgHighlight = MaterialTheme.colorScheme.surfaceVariant
    val themeBorder = MaterialTheme.colorScheme.outlineVariant

    // Build CSS from theme colors and encode
    val encodedCss by remember(themeBg, themeFg, themeBgHighlight, themeBorder) {
        mutableStateOf(encoded(themeFg, themeBg, themeBgHighlight, themeBorder))
    }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true // TODO: check maybe this is better
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.displayZoomControls = false

            setOnTouchListener { _, _ -> true } // FIXME TODO Consume touch events to prevent the scrolling bug from happening (i think this is what solved it)

            clearCache(true) // this should be unnecessary
        }
    }

    // Keep background in sync with theme changes
    DisposableEffect(themeBg) {
        webView.setBackgroundColor(themeBg.toArgb())
        onDispose { }
    }

    // Inject CSS helper // FIXME this is not needed in the current, better version
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
        //injectThemeCss() FIXME
        onDispose { }
    }

    DisposableEffect(url) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                isLoading = true
                errorMessage = null
                errorCode = null
            }

            // Use first-paint callback to avoid showing blank/white before content is drawn
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                //injectThemeCss() FIXME
                canGoBack = webView.canGoBack()
                isLoading = false
            }

            // TODO: this should be unnecessary
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.clearCache(true)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    errorMessage = error?.description.toString()
                    errorCode = error?.errorCode
                    isLoading = false
                }
            }



            // sollte Ladezeit verbessern, weil es nicht auf die Ressourcen warten muss, bin aber nicht sicher ob das was bringt.
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val requestUrl = request?.url?.toString() ?: return null

                if (requestUrl.endsWith("stupas.css") || requestUrl.endsWith("favicon.ico")) {
                    return WebResourceResponse(
                        "text/css",
                        "UTF-8",
                        null
                    )
                }


                // entfert autoscroll script und injected das CSS (alternative möglichkeit, wird gerade benutzt) so könnte man javaScriptEnabled wahrscheinlich deaktivieren
                if (requestUrl == url) {
                    try {
                        val document = Jsoup.connect(requestUrl).get()
                        val charset = document.charset()

                        // remove autoscroller script
                        document.getElementsByTag("script").remove()

                        // insert css
                        val css = String(Base64.decode(encodedCss, Base64.NO_WRAP), Charsets.UTF_8)
                        val styleElement = document.createElement("style")
                        styleElement.text(css)
                        document.head().appendChild(styleElement)



                        val stream = ByteArrayInputStream(document.html().toByteArray(charset))

                        return WebResourceResponse(
                            "text/html",
                            charset.name(),
                            stream
                        )
                    } catch (e: Exception) {
                        return null
                    }
                }

                return super.shouldInterceptRequest(view, request)
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

    Box(
        modifier.fillMaxSize(),
    ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = {
                webView.reload()
            },
            //modifier = Modifier.fillMaxSize(),
            modifier = Modifier.matchParentSize()

        ) {
            if (errorCode != null) {
                //if (errorCode == -6 || errorCode == -2) { // not sure if this works or if there are false-positives
                if (errorMessage == "net::ERR_INTERNET_DISCONNECTED") {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SignalWifiConnectedNoInternet4,
                                contentDescription = "No internet connection",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Kein Internet",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                text = "Überprüfen Sie Ihre Internetverbindung und versuchen Sie es erneut.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    ErrorCard(errorMessage!!)
                }
            } else {
                AndroidView(
                    modifier = Modifier.matchParentSize()
                        .verticalScroll(rememberScrollState()) // FIXME Allow scrolling the error view. this current method works but is really dumb. but this is due to a material bug, not mine.
                    ,
                    factory = { webView }
                )
            }
        }
    }
}
