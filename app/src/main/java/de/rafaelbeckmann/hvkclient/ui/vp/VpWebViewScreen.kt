package de.rafaelbeckmann.hvkclient.ui.vp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPasteSearch
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import de.rafaelbeckmann.hvkclient.ui.common.ErrorCard
import de.rafaelbeckmann.hvkclient.ui.common.HapticPullToRefreshBox
import de.rafaelbeckmann.hvkclient.ui.vp.VpStyle.courseMatchClass
import de.rafaelbeckmann.hvkclient.ui.vp.VpStyle.courseMatchHighlightClass
import de.rafaelbeckmann.hvkclient.ui.vp.VpStyle.encoded
import de.rafaelbeckmann.hvkclient.ui.vp.VpStyle.js
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream

// TODO: improve this
// TODO: add caching

// TODO: bug explanation (my theory): Adding a vertically scrollable WebView into a Horizontal Pager makes scrolling almost impossible, because it doesn't realise the WebView needs the vertical scroll, and treats it as a non-scrollable element. It therefore interprets everything as an horizontal swipe. Adding the .verticalScroll Modifier is a band-aid fix, as it scrolls the entire WebView Component and does not use the internal WebView scroll. These two sometimes conflict, resulting in the bug where it's sometimes impossible to scroll to the absolute top of the website, it treats some random point before that as the top. This is why I ignore the scrolling inside it. That's my (propably wrong) theory.
// in this state, it is currently working as expected.
@Composable
fun VpWebViewScreen(
    modifier: Modifier = Modifier,
    course: String? = null,
    url1: String = "https://www.kleist-schule.de/vertretungsplan/schueler/aktuelle%20plaene/1/vp.html",
    url2: String = "https://www.kleist-schule.de/vertretungsplan/schueler/aktuelle%20plaene/2/vp.html",
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Heute", "Nächster Schultag")

    val webViewStates = remember {
        listOf(WebViewState(ScrollState(0)), WebViewState(ScrollState(0)))
    }

    var fabHeight by remember { mutableIntStateOf(0) }
    val fabHeightDp = with(LocalDensity.current) { fabHeight.toDp() }

    val activeState = webViewStates[pagerState.currentPage]


    Scaffold (
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (activeState.errorCode != null) return@Scaffold
            if (activeState.isLoading) return@Scaffold

            ExtendedFloatingActionButton(
                expanded = true,
                onClick = {
                    activeState.webView?.evaluateJavascript(
                        "Array.from(document.getElementsByClassName('$courseMatchClass')).forEach(el => el.classList.add('$courseMatchHighlightClass'))",
                        null
                    )
                    if (activeState.offsets.isEmpty()) return@ExtendedFloatingActionButton

                    val index = activeState.offsetCount // to prevent race condition with scope
                    scope.launch {
                        activeState.offsets[index]?.let { activeState.scrollState.animateScrollTo(it) }
                    }
                    if (activeState.offsetCount != activeState.offsets.count() -1) {
                        activeState.offsetCount ++
                    } else {
                        activeState.offsetCount = 0
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.ContentPasteSearch,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        if (activeState.offsets.isNotEmpty()) {
                            "Nach \"$course\" suchen (${activeState.offsets.size} ${ if(activeState.offsets.size == 1) "Ergebnis" else "Ergebnisse"})"
                        } else {
                            "\"$course\" nicht gefunden"
                        }
                    )
                },
                modifier = Modifier
                    .onGloballyPositioned { // calculate FAB height
                        fabHeight = it.size.height
                    }
                // .animateContentSize() // TODO: animation too slow and also clipped fab shadow
            )
        }
    ) { innerPadding ->
        HapticPullToRefreshBox(
            isRefreshing = activeState.isLoading,
            onRefresh = {
                // FIXME fab scroll glitches to wrong position after reload
                webViewStates.forEach {
                    it.webView?.reload()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                ,
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

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        WebViewWithLoadingIndicator(
                            url = pageUrl,
                            course = course,
                            //modifier = Modifier.matchParentSize()
                            state = webViewStates[page],
                            fabHeight = fabHeightDp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebViewWithLoadingIndicator(
    url: String,
    course: String? = null,
    state: WebViewState,
    fabHeight: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var canGoBack by remember { mutableStateOf(false) }

    val themeBg = MaterialTheme.colorScheme.background
    val themeFg = MaterialTheme.colorScheme.onBackground
    val themeBgHighlight = MaterialTheme.colorScheme.surfaceVariant
    val themeBorder = MaterialTheme.colorScheme.outlineVariant
    val themeMatchBg = MaterialTheme.colorScheme.inversePrimary

    // Build CSS from theme colors and encode
    val encodedCss by remember(themeBg, themeFg, themeBgHighlight, themeBorder, themeMatchBg) {
        mutableStateOf(encoded(themeFg, themeBg, themeBgHighlight, themeBorder, themeMatchBg))
    }


    class WebAppInterface(private val context: Context) {
        @JavascriptInterface
        fun showToast(message:String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun sendOffsets(jsOffsets: FloatArray) {
            if (jsOffsets.isNotEmpty()) {
                val padding = with(density) {64.dp.toPx()}

                state.offsets = jsOffsets.map {
                    (it * density.density - padding).toInt()
                }.toTypedArray()
            }
        }
    }

    val webView = remember {
        WebView(context).apply {
            @SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true // TODO: check maybe this is better
            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.displayZoomControls = false

            addJavascriptInterface(WebAppInterface(context), "AndroidInterface")

            @SuppressLint("ClickableViewAccessibility")
            setOnTouchListener { _, _ -> true } // FIXME TODO Consume touch events to prevent the scrolling bug from happening (i think this is what solved it)

            clearCache(true) // this should be unnecessary
        }
    }

    // Keep background in sync with theme changes
    DisposableEffect(themeBg) {
        webView.setBackgroundColor(themeBg.toArgb())
        onDispose { }
    }


    /*
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
    */

    DisposableEffect(url) {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                state.isLoading = true
                state.errorMessage = null
                state.errorCode = null
            }

            // Use first-paint callback to avoid showing blank/white before content is drawn
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                //injectThemeCss() FIXME
                canGoBack = webView.canGoBack()
                state.isLoading = false
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
                    state.errorMessage = error?.description.toString()
                    state.errorCode = error?.errorCode
                    state.isLoading = false
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


                // entfernt autoscroll script und injected das CSS (alternative Möglichkeit, wird gerade benutzt)
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

                        // insert js
                        if (course != null) {
                            val js = js(course)
                            val jsElement = document.createElement("script")
                            jsElement.text(js)
                            document.head().appendChild(jsElement)
                        }


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
            state.isLoading = true
            webView.loadUrl(url)
        }
        onDispose { }
    }

    DisposableEffect(webView) {
        state.webView = webView // sync local webView with state

        onDispose { webView.destroy() }
    }

    BackHandler(enabled = canGoBack) {
        if (webView.canGoBack()) {
            webView.goBack()
            canGoBack = webView.canGoBack()
        }
    }

    if (state.errorCode != null) {
        //if (errorCode == -6 || errorCode == -2) { // not sure if this works or if there are false-positives
        if (state.errorMessage == "net::ERR_INTERNET_DISCONNECTED") {
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
            ErrorCard(state.errorMessage!!) // FIXME cant pull to refresh
        }
    } else {
        AndroidView(
            modifier = Modifier//.matchParentSize()
                .verticalScroll(state.scrollState) // FIXME Allow scrolling the error view. this current method works but is really dumb. but i think this is due to a material bug, not mine.
                .padding(PaddingValues(bottom = fabHeight + 16.dp)) // FAB height + Android hard-coded FAB offset
            ,
            factory = { webView }
        )
    }
}

class WebViewState(
    var scrollState: ScrollState
) {
    var webView by mutableStateOf<WebView?>(null)
    var offsets by mutableStateOf<Array<Int?>>(emptyArray())
    var offsetCount by mutableIntStateOf(0)
    var errorCode by mutableStateOf<Int?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(true)
}