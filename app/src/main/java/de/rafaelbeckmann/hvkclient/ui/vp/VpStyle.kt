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

    fun encoded(themeFg: Color, themeBg: Color, themeBgHighlight: Color, themeBorder: Color): String {
        val css = """
            :is(html, body, body *) {
            text-align: center !important;
            color: ${toRgba(themeFg)} !important;
            background-color: ${toRgba(themeBg)} !important;            
            font-size: 14px !important;
            }
            h3 {
            font-size: 18px !important;
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
            body > br { display: none !important; }
            a { display: none !important; }
            """.trimIndent()

        return Base64.encodeToString(css.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

}