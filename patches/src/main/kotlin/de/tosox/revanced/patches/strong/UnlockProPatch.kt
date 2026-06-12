package de.tosox.revanced.patches.strong

import app.revanced.patcher.accessFlags
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.strings
import com.android.tools.smali.dexlib2.AccessFlags
import de.tosox.revanced.util.injectEnumReturnByString

internal val BytecodePatchContext.getSubscriptionTypeFingerprint by gettingFirstMethodDeclaratively {
    strings(
        "PRO_FOREVER",
        "EARLY_ADOPTER",
        "ANDROID_EARLY_ADOPTER"
    )
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    parameterTypes()
}

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
