package de.tosox.revanced.patches.kicktipp

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType

private val adBlockScript = """
    javascript:(function(){
        var s = document.createElement('style');
        s.textContent =
            '.adsbygoogle,' +
            '[id^=div-gpt-ad],' +
            '[class*=ad-slot],' +
            '[class*=ad-banner],' +
            '[class*=advertisement]' +
            '{display:none!important;min-height:0!important;}';
        (document.head || document.body || document.documentElement).appendChild(s);
    })();
""".trimIndent().replace("\n", "").replace("  ", "")

internal val BytecodePatchContext.onPageFinishedFingerprint by gettingFirstMethodDeclaratively {
    name("onPageFinished")
    returnType("V")
    parameterTypes("Landroid/webkit/WebView;", "Ljava/lang/String;")
}

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ads across the app",
) {
    // Tested with 1.67
    compatibleWith("de.kicktipp.mbookmark")

    apply {
        onPageFinishedFingerprint.addInstructions(
            0,
            """
                const-string v0, "$adBlockScript"
                invoke-virtual {p1, v0}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V
            """
        )
    }
}
