package de.tosox.revanced.patches.kicker.pur

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val unlockPurPatch = bytecodePatch(
    name = "Unlock Pur",
    description = "Unlocks the Pur subscription",
) {
    // Tested with 7.9.2
    compatibleWith("com.netbiscuits.kicker")

    execute {
        getAboStateFingerprint.method.apply {
            // Get enum class descriptor
            val returnType = getAboStateFingerprint.method.returnType

            // Get enum constructor
            val aboPurEnum = fingerprint {
                strings(
                    "PUR"
                )
                custom { method, classDef ->
                    classDef.type == returnType && method.name == "<clinit>"
                }
            }

            // Find first instance of the searched for string
            val purStringIndex = aboPurEnum.stringMatches!!.first().index

            // Get instruction which loads the string into a variable
            val purVariableIndex = aboPurEnum.method.implementation!!.instructions
                .drop(purStringIndex)
                .indexOfFirst { instruction ->
                    instruction.opcode == Opcode.SPUT_OBJECT
                }.plus(purStringIndex)

            // Get the variable name
            val purVariableName = (aboPurEnum.method.getInstruction<ReferenceInstruction>(purVariableIndex)
                .reference as FieldReference).name

            // Return the wanted enum variable
            getAboStateFingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, $returnType->$purVariableName:$returnType
                    return-object v0
                """
            )
        }
    }
}
