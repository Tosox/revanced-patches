package de.tosox.revanced.patches.kicker.plus

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks the Plus subscription",
) {
    // Tested with 7.9.2
    compatibleWith("com.netbiscuits.kicker")

    execute {
        getAboStatePlusFingerprint.method.apply {
            // Get enum class descriptor
            val returnType = getAboStatePlusFingerprint.method.returnType

            // Get enum constructor
            val aboPlusEnum = fingerprint {
                strings(
                    "PLUS"
                )
                custom { method, classDef ->
                    classDef.type == returnType && method.name == "<clinit>"
                }
            }

            // Find first instance of the searched for string
            val plusStringIndex = aboPlusEnum.stringMatches!!.first().index

            // Get instruction which loads the string into a variable
            val plusVariableIndex = aboPlusEnum.method.implementation!!.instructions
                .drop(plusStringIndex)
                .indexOfFirst { instruction ->
                    instruction.opcode == Opcode.SPUT_OBJECT
                }.plus(plusStringIndex)

            // Get the variable name
            val plusVariableName = (aboPlusEnum.method.getInstruction<ReferenceInstruction>(plusVariableIndex)
                .reference as FieldReference).name

            // Return the wanted enum variable
            getAboStatePlusFingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, $returnType->$plusVariableName:$returnType
                    return-object v0
                """
            )
        }
    }
}
