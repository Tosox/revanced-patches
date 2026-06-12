package de.tosox.revanced.patches.kicker

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.returnType
import de.tosox.revanced.util.injectEnumReturnByString

internal val BytecodePatchContext.getPlusAboStateFingerprint by gettingFirstMethodDeclaratively {
    returnType("KPlusAboState;")
    definingClass("KUserImpl;")
}

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks the Plus subscription",
) {
    // Tested with 7.12.1
    compatibleWith("com.netbiscuits.kicker")

    apply {
        injectEnumReturnByString(getPlusAboStateFingerprint, "PLUS")
    }
}
