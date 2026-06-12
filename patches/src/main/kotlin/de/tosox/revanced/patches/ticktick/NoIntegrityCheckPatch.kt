package de.tosox.revanced.patches.ticktick

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.name
import app.revanced.patcher.opcodes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal val BytecodePatchContext.verifyJobFingerprint by composingFirstMethod {
    name("onRun")
    definingClass("VerifyJob;")
    opcodes(
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
    )
}

@Suppress("unused")
val noIntegrityCheckPatch = bytecodePatch(
    name = "No Integrity Check",
    description = "Disables the integrity checks",
) {
    // Tested with 7.6.9.1
    compatibleWith("com.ticktick.task")

    apply {
        verifyJobFingerprint.method.apply {
            val mvIndex = verifyJobFingerprint[0]
            val mvRegister = getInstruction<OneRegisterInstruction>(mvIndex).registerA

            replaceInstruction(
                mvIndex,
                "const/4 v$mvRegister, 0x0"
            )
        }
    }
}
