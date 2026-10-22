import SwiftUI
import WebKit

struct WebViewDay: Identifiable {
    let id: Int
    let title: String
    let url: URL
}

struct VpWebView: View {
    private let pages = [
        WebViewDay(id: 0, title: "Heute", url: URL(string: "https://www.kleist-schule.de/vertretungsplan/schueler/aktuelle%20plaene/1/vp.html")!),
        WebViewDay(id: 1, title: "Nächster Schultag", url: URL(string: "https://www.kleist-schule.de/vertretungsplan/schueler/aktuelle%20plaene/2/vp.html")!)
    ]
    
    @State private var selectedTabIndex: Int? = 0
    
    var body: some View {
        Group {
            if #available(iOS 17.0, *) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 0) {
                        ForEach(pages) { page in
                            VpWebContentView(url: page.url)
                                .containerRelativeFrame(.horizontal)
                                .id(page.id)
                        }
                    }
                    .scrollTargetLayout()
                }
                .scrollTargetBehavior(.paging)
                .scrollPosition(id: $selectedTabIndex)
            } else {
                TabView(selection: $selectedTabIndex) {
                    ForEach(pages) { page in
                        VpWebContentView(url: page.url)
                            .tag(page.id)
                    }
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
            }
        }
        .toolbar {
            ToolbarItem(placement: .principal) {
                Picker("Tag", selection: $selectedTabIndex.animation(.snappy(duration: 0.35))) {
                    
                    ForEach(pages) { page in
                        Text(page.title).tag(page.id)
                    }
                }
                .pickerStyle(.segmented)
            }
        }
    }
    
    struct VpWebContentView: View {
        let url: URL
        
        var body: some View {
            if #available(iOS 26.0, *) {
                NativeWebView(url: url)
            } else {
                FallbackWebView(url: url)
                //.navigationBarTitleDisplayMode(.inline)
                //.ignoresSafeArea(edges: .bottom)
            }
        }
    }
}

@available(iOS 26.0, *)
struct NativeWebView: View {
    var url: URL
    
    @State var page: WebPage
    
    init(url: URL) {
        self.url = url
        
        let configuration = WebPage.Configuration()
        configuration.userContentController.addUserScript(makeUserScript())
        
        page = WebPage(configuration: configuration)
    }
    
    var body: some View {
        WebView(page)
            .task {
                page.load(url)
            }
            .webViewContentBackground(.hidden) // flashes white before content otherwise
    }
}


struct FallbackWebView: UIViewRepresentable {
    let url: URL
    
    func makeUIView(context: Context) -> WKWebView {
        let configuration = WKWebViewConfiguration()
        configuration.userContentController.addUserScript(makeUserScript())
        
        let webView = WKWebView(frame: .zero, configuration: configuration)
        webView.load(URLRequest(url: url))
        
        webView.isOpaque = false // flashes white before content otherwise
        
        return webView
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
    }
}


func makeUserScript() -> WKUserScript {
    let themeFg = "#000000"
    let themeBg = "#FFFFFF"
    let themeBorder = "#CCCCCC"
    let themeBgHighlight = "#DDDDDD"
    let themeMatchBg = "#000000"
    
    let themeFgDark = "#FFFFFF"
    let themeBgDark = "#000000"
    let themeBorderDark = "#333333"
    let themeBgHighlightDark = "#222222"
    let themeMatchBgDark = "#FFFFFF"
    
    let courseMatchClass = "hvkClientCourseMatch"
    let courseMatchHighlightClass = "hvkClientCourseMatchHighlight"
    let highlightFadeOutAnimation = "highlightFade"

    
    let css = """
        :is(html, body, body *) {
        text-align: center !important;
        color: \(themeFg) !important;
        background-color: \(themeBg) !important;
        font-size: 14px !important;
        font-family: system-ui !important; /* renders as SF Pro on Apple Platforms */
        }
        h3 { /* Heinrich-von-Kleist-Schule Eschborn und Vertetungsplan für... */
        font-size: 18px !important;
        display: none !important;
        }
        b { /* in Arbeit */
        font-size: 30px !important; /* for "In Arbeit..." */
        }
        big { /* Info oben */
        font-size: 20px !important;
        }
        table, th, td {
        margin: auto;
        border: 1px solid \(themeBorder) !important;
        border-collapse: collapse;
        }
        th, th > div {
        background-color: \(themeBgHighlight) !important;
        }
        th, td { padding: 2px !important; }
        br { display: inline !important; }
        body > br { display: none !important; } /* Padding oben */
        a { display: none !important; }

        .\(courseMatchHighlightClass) {
        border-radius: 6px;
        animation-name: \(highlightFadeOutAnimation);
        animation-duration: 1s; /* The duration of the animation */
        animation-fill-mode: forwards; /* Ensures it stays at the end state */
        }

        @keyframes \(highlightFadeOutAnimation) {
        0% {
        box-shadow: inset 0 0 0 0 \(themeMatchBg);
        }
        50% {
        box-shadow: inset 0 0 0 100px \(themeMatchBg);
        }
        100% {
        box-shadow: inset 0 0 0 0 \(themeMatchBg);
        }
        }
        
        
        /* dark mode */
        @media(prefers-color-scheme: dark) {
            :is(html, body, body *) {
            color: \(themeFgDark) !important;
            background-color: \(themeBgDark) !important;
            }
            table, th, td {
            border: 1px solid \(themeBorderDark) !important;
            }
            th, th > div {
            background-color: \(themeBgHighlightDark) !important;
            }
            .\(courseMatchHighlightClass) {
            animation-name: \(highlightFadeOutAnimation);
            }

            @keyframes \(highlightFadeOutAnimation) {
            0% {
            box-shadow: inset 0 0 0 0 \(themeMatchBgDark);
            }
            50% {
            box-shadow: inset 0 0 0 100px \(themeMatchBgDark);
            }
            100% {
            box-shadow: inset 0 0 0 0 \(themeMatchBgDark);
            }
            }
        }
        
        """
    
    let js = """
    // Disable built-in autoscroll and auto-reload script (<body onload="windowSize()">)
    window.windowSize = () => {};
    window.auto = () => {};
    window.scrollen = () => {};
    window.laden = () => {};
    if (document.body) {
        document.body.onload = null;
    }

    const style = document.createElement('style');
    style.innerHTML = `\(css)`;
    document.head.appendChild(style);
    
    
    // fix viewport to not appear zoomed out
    var meta = document.createElement('meta');
    meta.name = 'viewport';
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
    document.head.appendChild(meta);
    """
    
    return WKUserScript(
        source: js,
        injectionTime: WKUserScriptInjectionTime.atDocumentEnd,
        forMainFrameOnly: true
    )
}
