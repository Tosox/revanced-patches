package de.tosox.revanced.patches.mega.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import de.tosox.revanced.util.getReference
import de.tosox.revanced.util.indexOfFirstInstruction

internal val showAdsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    custom { method, classDef ->
        if (!classDef.endsWith("/ManagerActivity;")) return@custom false

        method.indexOfFirstInstruction {
            opcode == Opcode.NEW_INSTANCE &&
            getReference<TypeReference>()?.type
                ?.endsWith("/ManagerActivity\$checkForInAppAdvertisement\$1;") == true
        } >= 0
    }
}
