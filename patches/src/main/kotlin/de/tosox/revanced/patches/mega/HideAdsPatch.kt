package de.tosox.revanced.patches.mega

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import de.tosox.revanced.util.returnEarly

internal val BytecodePatchContext.showAdsFingerprint by gettingFirstMethodDeclaratively {
    definingClass("ManagerActivity;")
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returnType("V")
    parameterTypes("Z")
    instructions(
        allOf(
            Opcode.NEW_INSTANCE(),
            type("/ManagerActivity\$checkForInAppAdvertisement\$1;")
        )
    )
}

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ads across the app",
) {
    // Tested with 15.18(252751615)(9425f68761)
    compatibleWith("mega.privacy.android.app")

    apply {
        showAdsFingerprint.returnEarly()
    }
}
