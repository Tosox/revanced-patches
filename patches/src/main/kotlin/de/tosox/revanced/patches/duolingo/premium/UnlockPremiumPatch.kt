package de.tosox.revanced.patches.duolingo.premium

import app.revanced.patcher.CompositeMatch
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import de.tosox.revanced.util.findEnumStaticField

@Suppress("unused")
val patch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks the Premium subscription",
) {
    // Tested with 6.73.3
    compatibleWith("com.duolingo")

    apply {
        fun injectGold(match: CompositeMatch) {
            match.method.apply {
                val iputIndex = match[0]
                val sourceRegister = getInstruction<TwoRegisterInstruction>(iputIndex).registerA
                val fieldType = getInstruction<ReferenceInstruction>(iputIndex).fieldReference!!.type
                val injectedField = findEnumStaticField("GOLD", fieldType)

                addInstruction(
                    iputIndex,
                    "sget-object v$sourceRegister, $injectedField"
                )
            }
        }

        injectGold(userSyntheticConstructorFingerprint)
        injectGold(userConstructorFingerprint)
    }
}
