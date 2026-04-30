package de.rafaelbeckmann.hvkclient.ui.vp

import android.util.Base64
import androidx.compose.ui.graphics.Color

object VpStyle {
    fun toRgba(color: Color): String {
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        val a = color.alpha
        return "rgba($r,$g,$b,$a)"
    }

    val courseMatchClass = "hvkClientCourseMatch"
    val courseMatchHighlightClass = "hvkClientCourseMatchHighlight"

    val highlightFadeOutAnimation = "highlightFade"

    fun encoded(
        themeFg: Color,
        themeBg: Color,
        themeBgHighlight: Color,
        themeBorder: Color,
        themeMatchBg: Color
    ): String {
        // TODO: test if just making p font size bigger is specific enough for "in Arbeit..."
        val css = """
            :is(html, body, body *) {
            text-align: center !important;
            color: ${toRgba(themeFg)} !important;
            background-color: ${toRgba(themeBg)} !important;
            font-size: 14px !important;
            }
            h3 { /* Heinrich-von-Kleist-Schule Eschborn und Vertetungsplan für... */
            font-size: 18px !important;
            }
            b { /* in Arbeit */
            font-size: 30px !important; /* for "In Arbeit..." */
            }
            big { /* Info oben */
            font-size: 20px !important;
            }
            table, th, td {
            margin: auto;
            border: 1px solid ${toRgba(themeBorder)} !important;
            border-collapse: collapse;
            }
            th, th > div {
            background-color: ${toRgba(themeBgHighlight)} !important;
            }
            th, td { padding: 2px !important; }
            br { display: inline !important; }
            body > br { display: none !important; } /* Padding oben */
            a { display: none !important; }

            .${courseMatchHighlightClass} {
            border-radius: 6px;
            animation-name: $highlightFadeOutAnimation;
            animation-duration: 1s; /* The duration of the animation */
            animation-fill-mode: forwards; /* Ensures it stays at the end state */
            }

            @keyframes $highlightFadeOutAnimation {
            0% {
            box-shadow: inset 0 0 0 0 ${toRgba(themeMatchBg)};
            }
            50% {
            box-shadow: inset 0 0 0 100px ${toRgba(themeMatchBg)};
            }
            100% {
            box-shadow: inset 0 0 0 0 ${toRgba(themeMatchBg)};
            }
            }
            """.trimIndent()

        return Base64.encodeToString(css.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun js(course: String): String { // FIXME HARD CODED COURSE
        return """
            document.addEventListener("DOMContentLoaded", (event) => {
                const searchValue = "$course";

                let walker = document.createTreeWalker(
                    document.body,
                    NodeFilter.SHOW_TEXT
                )

                const matches = [];
                let node;
                while (node = walker.nextNode()) {
                    if (node.nodeValue.includes(searchValue)) {
                        matches.push(node.parentElement)
                    }
                }

                if (matches.length > 0) {
                    const offsets = matches.map( match =>
                        window.pageYOffset + match.getBoundingClientRect().top
                    )

                    matches.forEach( match => {
                        match.classList.add("$courseMatchClass")
                    })

                    AndroidInterface.sendOffsets(offsets);
                }

                // Removes the highlight class when the animation finishes
                document.addEventListener("animationend", (e) => {
                    if (e.animationName === "$highlightFadeOutAnimation") {
                        e.target.classList.remove("$courseMatchHighlightClass");
                    }
                });
            });
        """.trimIndent()
    }
}