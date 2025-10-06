package de.tosox.revanced.patches.customstickermaker.verify

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

internal fun getSignatureCheckFingerprint() = fingerprint {
    strings(
        "this as java.lang.String...ing(startIndex, endIndex)",
        "this as java.lang.String).getBytes(charset)"
    )
    opcodes(
        Opcode.INVOKE_STATIC,
        Opcode.THROW,
        Opcode.INVOKE_VIRTUAL,
        Opcode.INVOKE_STATIC,
        Opcode.THROW
    )
}

@Suppress("unused")
val noIntegrityCheckPatch = bytecodePatch(
    name = "No Integrity Check",
    description = "Disables the integrity checks",
) {
    // Tested with 1.20.55
    compatibleWith("customstickermaker.whatsappstickers.personalstickersforwhatsapp")

    execute {
        exitProcessFingerprint.method.addInstructions(
            0,
            """
                new-instance p0, Ljava/lang/RuntimeException;
                invoke-direct { p0, p1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
                return-object p0
            """
        )

        // FIXME: The signature can be inserted multiple times per method

        classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                val signatureCheckFingerprint = getSignatureCheckFingerprint()
                val match = signatureCheckFingerprint.matchOrNull(method)
                if (match != null) {
                    val matchIndex = match.patternMatch!!.endIndex
                    match.method.replaceInstruction(
                        matchIndex, "nop"
                    )
                }
            }
        }
    }
}
