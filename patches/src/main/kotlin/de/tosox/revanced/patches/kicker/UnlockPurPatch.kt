package de.tosox.revanced.patches.kicker

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.injectEnumReturnByString

internal val BytecodePatchContext.getAboStateFingerprint by gettingFirstMethodDeclaratively {
    name("getAboState")
    definingClass("KUserImpl;")
}

@Suppress("unused")
val unlockPurPatch = bytecodePatch(
    name = "Unlock Pur",
    description = "Unlocks the Pur subscription",
) {
    // Tested with 7.9.2
    compatibleWith("com.netbiscuits.kicker")

    apply {
        injectEnumReturnByString(getAboStateFingerprint, "PUR")
    }
}
