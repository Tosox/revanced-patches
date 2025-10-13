package de.tosox.revanced.patches.kicker.common

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import de.tosox.revanced.util.getReference
import de.tosox.revanced.util.indexOfFirstInstruction

fun BytecodePatchContext.injectEnumReturn(
    targetMethod: MutableMethod,
    enumString: String
) {
    targetMethod.apply {
        // Get enum constructor
        val enumFingerprint = fingerprint {
            strings(enumString)
            custom { method, classDef ->
                classDef.type == returnType && method.name == "<clinit>"
            }
        }

        // Find first instance of the searched for string
        val stringMatchIndex = enumFingerprint.stringMatches!!.first().index

        // Get instruction which loads the string into a variable
        val sputIndex = enumFingerprint.method
            .indexOfFirstInstruction(stringMatchIndex, Opcode.SPUT_OBJECT)

        // Get the variable name
        val plusVariableName = enumFingerprint.method
            .getInstruction<ReferenceInstruction>(sputIndex)
            .getReference<FieldReference>()!!
            .name

        // Return the wanted enum variable
        addInstructions(
            0,
            """
                    sget-object v0, $returnType->$plusVariableName:$returnType
                    return-object v0
                """
        )
    }
}
