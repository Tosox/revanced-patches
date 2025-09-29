package de.tosox.revanced.patches.mega.ads

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val showAdsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    custom { method, classDef ->
        if (!classDef.endsWith("/ManagerActivity;")) return@custom false

        method.implementation?.instructions?.any { instruction ->
            if (instruction.opcode != Opcode.NEW_INSTANCE) return@any false

            val reference = (instruction as ReferenceInstruction).reference as TypeReference
            reference.type.endsWith("/ManagerActivity\$checkForInAppAdvertisement\$1;")
        } == true
    }
}
