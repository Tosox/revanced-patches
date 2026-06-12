package de.tosox.revanced.patches.earphonealarm

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.returnEarly

internal val BytecodePatchContext.getPlanStatusFingerprint by gettingFirstMethodDeclaratively {
    name("getPlanStatus")
    definingClass("SharedPrefs;")
}

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks the Premium subscription",
) {
    // Tested with 2.2.4
    compatibleWith("com.wixsite.ut_app.utalarm")

    apply {
        getPlanStatusFingerprint.returnEarly(1)
    }
}
