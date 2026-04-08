package de.tosox.revanced.patches.strong.pro

import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.injectEnumReturnByString

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro subscription",
) {
    // Tested with 6.2.1
    compatibleWith("io.strongapp.strong")

    apply {
        injectEnumReturnByString(getSubscriptionTypeFingerprint, "PRO_FOREVER")
    }
}
