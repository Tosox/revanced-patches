package de.tosox.revanced.patches.kicker.plus

import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.injectEnumReturnByString

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks the Plus subscription",
) {
    // Tested with 7.9.2
    compatibleWith("com.netbiscuits.kicker")

    execute {
        injectEnumReturnByString(getAboStatePlusFingerprint.method, "PLUS")
    }
}
