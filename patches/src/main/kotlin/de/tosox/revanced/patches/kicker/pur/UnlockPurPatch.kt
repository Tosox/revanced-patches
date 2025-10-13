package de.tosox.revanced.patches.kicker.pur

import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.patches.kicker.common.injectEnumReturn

@Suppress("unused")
val unlockPurPatch = bytecodePatch(
    name = "Unlock Pur",
    description = "Unlocks the Pur subscription",
) {
    // Tested with 7.9.2
    compatibleWith("com.netbiscuits.kicker")

    execute {
        injectEnumReturn(getAboStateFingerprint.method, "PUR")
    }
}
