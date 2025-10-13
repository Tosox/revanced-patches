package de.tosox.revanced.patches.earphonealarm.premium

import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.returnEarly

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks the Premium subscription",
) {
    // Tested with 2.2.4
    compatibleWith("com.wixsite.ut_app.utalarm")

    execute {
        getPlanStatusFingerprint.method.returnEarly(1)
    }
}
