package de.tosox.revanced.patches.strong.pro

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.strings
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.getSubscriptionTypeFingerprint by gettingFirstMethodDeclaratively {
    strings(
        "PRO_FOREVER",
        "EARLY_ADOPTER",
        "ANDROID_EARLY_ADOPTER"
    )
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
}
