package de.tosox.revanced.patches.mega.ads

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

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
