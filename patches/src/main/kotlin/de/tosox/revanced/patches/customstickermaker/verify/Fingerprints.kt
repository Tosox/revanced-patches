package de.tosox.revanced.patches.customstickermaker.verify

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val exitProcessFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Ljava/lang/RuntimeException;")
    parameters(
        "I",
        "Ljava/lang/String;"
    )
}
