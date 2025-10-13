package de.tosox.revanced.patches.mega.ads

import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ads across the app",
) {
    // Tested with 15.18(252751615)(9425f68761)
    compatibleWith("mega.privacy.android.app")

    execute {
        showAdsFingerprint.method.returnEarly()
    }
}
