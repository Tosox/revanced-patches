package de.tosox.revanced.util

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod
import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.firstClassDef
import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.strings
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/**
 * Resolve the descriptor of the static field that holds the [enumString] value of the enum
 * defined by [enumType] (e.g. `Lcom/x/AboState;->a:Lcom/x/AboState;`).
 *
 * The lookup is anchored on [enumType] via its defining class, so an unrelated enum that declares a
 * constant of the same name (e.g. androidx `BlendModeCompat.PLUS`) cannot be matched by mistake.
 *
 * @return The field descriptor, or null if the enum or the constant cannot be found.
 */
fun BytecodePatchContext.findEnumStaticField(
    enumString: String,
    enumType: String
): String? {
    val clinit = firstMethodComposite {
        definingClass(enumType)
        name("<clinit>")
        strings(enumString)
    }

    return clinit.method.enumFieldDescriptorAfter(clinit[0])
}

/**
 * Overrides [targetMethod] so it always returns the [enumString] value of an enum.
 *
 * @param enumType The enum type descriptor. Defaults to [targetMethod]'s return type.
 */
fun BytecodePatchContext.injectEnumReturnByString(
    targetMethod: MutableMethod,
    enumString: String,
    enumType: String? = null
) {
    val resolvedEnumType = enumType ?: targetMethod.returnType

    val fieldRef = findEnumStaticField(enumString, resolvedEnumType)
        ?: throw PatchException("Static field for constant '$enumString' in enum '$resolvedEnumType' not found")

    targetMethod.injectEnumFieldReturn(fieldRef)
}

/**
 * Identifies the enum declared by [enumConstants] (matched as a set, so a single ambiguous constant
 * name cannot select the wrong enum), then overrides the method of [classType] that returns that
 * enum so it always returns [returnConstant].
 *
 * @param classType The descriptor of the class whose member method should be overridden
 *  (e.g. `fingerprint.definingClass`).
 * @param returnConstant The enum constant to return; must be one of [enumConstants].
 * @param enumConstants The constants that together uniquely identify the enum type.
 */
fun BytecodePatchContext.injectEnumReturnByConstants(
    classType: String,
    returnConstant: String,
    vararg enumConstants: String,
) {
    // Identify the enum by the full set of its constants; the <clinit> match also doubles as the
    // source for the requested constant's static field, so it is only searched once.
    val clinit = firstMethodComposite {
        name("<clinit>")
        strings(*enumConstants)
    }.method
    val enumType = clinit.definingClass

    val constStringIndex = clinit.indexOfFirstInstruction {
        getReference<StringReference>()?.string == returnConstant
    }.takeIf { it >= 0 }
        ?: throw PatchException("Enum '$enumType' does not declare constant: $returnConstant")

    val fieldRef = clinit.enumFieldDescriptorAfter(constStringIndex)
        ?: throw PatchException("Could not resolve static field for constant: $returnConstant")

    val targetMethod = firstClassDef(classType).methods
        .firstOrNull { it.returnType == enumType }
        ?: throw PatchException("No method returning enum '$enumType' found in class: $classType")

    targetMethod.injectEnumFieldReturn(fieldRef)
}

/**
 * Reads the static enum field written by the first `SPUT_OBJECT` at or after [constStringIndex]
 * and returns its full descriptor, or null if none is found.
 */
private fun MutableMethod.enumFieldDescriptorAfter(constStringIndex: Int): String? {
    val sputIndex = indexOfFirstInstruction(constStringIndex, Opcode.SPUT_OBJECT)
        .takeIf { it >= 0 } ?: return null

    val field = getInstruction<ReferenceInstruction>(sputIndex)
        .getReference<FieldReference>() ?: return null

    return "${field.definingClass}->${field.name}:${field.type}"
}

/**
 * Overrides the method to immediately return the enum value held by [fieldDescriptor].
 */
private fun MutableMethod.injectEnumFieldReturn(fieldDescriptor: String) =
    addInstructions(
        0,
        """
            sget-object v0, $fieldDescriptor
            return-object v0
        """
    )
