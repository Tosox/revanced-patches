package de.tosox.revanced.util

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

fun BytecodePatchContext.findEnumStaticField(
    enumString: String,
    enumType: String
): String? {
    // Find the enum <clinit> that contains the constant string and the sput to a static field
    val enumFingerprint = fingerprint {
        strings(enumString)
        custom { method, classDef ->
            classDef.type == enumType && method.name == "<clinit>"
        }
    }

    // Index of the const-string match in the method's instruction list
    val stringMatchIndex = enumFingerprint.stringMatches!!.first().index

    // Find the first SPUT_OBJECT instruction that stores that string into a static field
    val sputIndex = enumFingerprint.method
        .indexOfFirstInstruction(stringMatchIndex, Opcode.SPUT_OBJECT)
        .takeIf { it >= 0 } ?: return null

    // Read the field reference written by the SPUT
    val fieldName = enumFingerprint.method
        .getInstruction<ReferenceInstruction>(sputIndex)
        .getReference<FieldReference>()?.name ?: return null

    // Return full field ref
    return "$enumType->$fieldName:$enumType"
}

fun BytecodePatchContext.injectEnumReturnByString(
    targetMethod: MutableMethod,
    enumString: String,
    enumType: String? = null
) {
    val resolvedEnumType = enumType ?: targetMethod.returnType

    val fieldRef = findEnumStaticField(enumString, resolvedEnumType)
        ?: throw IllegalArgumentException("Enum static field for string '$enumString' in type '$resolvedEnumType' not found")

    // Inject sget-object at the start of the method
    targetMethod.addInstructions(
        0,
        """
            sget-object v0, $fieldRef
            return-object v0
        """
    )
}
