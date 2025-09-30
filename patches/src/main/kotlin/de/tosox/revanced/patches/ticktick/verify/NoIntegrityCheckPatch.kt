package de.tosox.revanced.patches.ticktick.verify

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val noIntegrityCheckPatch = bytecodePatch(
    name = "No Integrity Check",
    description = "Disables the integrity checks",
) {
    // Tested with 7.6.9.1
    compatibleWith("com.ticktick.task")

    execute {
        verifyJobFingerprint.method.apply {
            val mvIndex = verifyJobFingerprint.patternMatch!!.startIndex
            val mvRegister = getInstruction<OneRegisterInstruction>(mvIndex).registerA

            verifyJobFingerprint.method.replaceInstruction(
                mvIndex,
                """
                    const/4 v$mvRegister, 0x0
                """
            )
        }
    }
}
