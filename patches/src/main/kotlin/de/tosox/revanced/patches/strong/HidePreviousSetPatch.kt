package de.tosox.revanced.patches.strong

import app.revanced.com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable
import app.revanced.patcher.*
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import de.tosox.revanced.util.getReference
import de.tosox.revanced.util.indexOfFirstInstructionOrThrow
import de.tosox.revanced.util.indexOfFirstInstructionReversedOrThrow

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lde/tosox/revanced/extension/strong/HidePreviousSetPatch;"
private const val SETTINGS_CLASS_DESCRIPTOR =
    "Lde/tosox/revanced/extension/strong/StrongSettings;"
private const val SETTINGS_SCREEN_CLASS_DESCRIPTOR =
    "Lde/tosox/revanced/extension/strong/StrongSettingsScreen;"

// Only stable (non-obfuscated) names are referenced directly; everything obfuscated is resolved
// from these anchors at patch time, so a Strong update that re-obfuscates does not break the patch.
private const val SET_VIEW_HOLDER_CLASS = "Lio/strongapp/strong/ui/log_workout/holders2/SetViewHolder2;"
private const val TEXT_FIELD_VIEW_CLASS = "Lio/strongapp/strong/ui/log_workout/TextFieldView;"
private const val STRONG_CHECKBOX_CLASS = "Lio/strongapp/strong/common/StrongCheckbox;"
private const val LOG_WORKOUT_PACKAGE = "Lio/strongapp/strong/ui/log_workout/"

private const val MASK_HELPER_NAME = "revancedMaskPreviousValue"

// StrongApplication#onCreate – earliest reliable hook to initialise the settings store.
internal val BytecodePatchContext.applicationOnCreateFingerprint by gettingFirstMethodDeclaratively {
    definingClass("Lio/strongapp/strong/StrongApplication;")
    name("onCreate")
    parameterTypes()
    returnType("V")
}

// SettingsActivity#onCreate – inflates the (static) settings layout we append our section to.
// Fully qualified: another class (HealthConnectSettingsActivity) also ends in "SettingsActivity".
internal val BytecodePatchContext.settingsActivityOnCreateFingerprint by gettingFirstMethodDeclaratively {
    definingClass("Lio/strongapp/strong/ui/settings/SettingsActivity;")
    name("onCreate")
    parameterTypes("Landroid/os/Bundle;")
    returnType("V")
}

@Suppress("unused")
val hidePreviousSetPatch = bytecodePatch(
    name = "Hide previous set values",
    description = "Hides the weight, reps, duration and/or distance of the previous set until the " +
        "current set has been completed. Each part can be toggled in the new ReVanced section of " +
        "the app settings.",
) {
    // Tested with 6.2.1
    compatibleWith("io.strongapp.strong")

    extendWith("extensions/strong.rve")

    apply {
        // Initialise the settings store as early as possible so the saved toggle value is honored
        // even when the settings screen has not been opened yet.
        applicationOnCreateFingerprint.addInstructions(
            0,
            "invoke-static { p0 }, $SETTINGS_CLASS_DESCRIPTOR->init(Landroid/content/Context;)V",
        )

        // Append the "ReVanced" section with the toggles to the settings screen.
        val settingsOnCreate = settingsActivityOnCreateFingerprint
        settingsOnCreate.addInstructions(
            settingsOnCreate.indexOfFirstInstructionReversedOrThrow(Opcode.RETURN_VOID),
            "invoke-static { p0 }, $SETTINGS_SCREEN_CLASS_DESCRIPTOR->addRevancedSettings(Landroid/app/Activity;)V",
        )

        val holder = firstClassDef(SET_VIEW_HOLDER_CLASS)

        // The bind method is the public final void method taking the single (obfuscated) set-item
        // model. It is told apart from the other such method (which takes a TextFieldView) by
        // excluding the stable TextFieldView type; the item type is then read off its parameter.
        val bind = holder.methods.first { candidate ->
            AccessFlags.PUBLIC.isSet(candidate.accessFlags) &&
                AccessFlags.FINAL.isSet(candidate.accessFlags) &&
                candidate.returnType == "V" &&
                candidate.parameterTypes.size == 1 &&
                candidate.parameterTypes.first().toString().let {
                    it != TEXT_FIELD_VIEW_CLASS && it.startsWith(LOG_WORKOUT_PACKAGE)
                }
        }
        val setItemClass = bind.parameterTypes.first().toString()

        // Resolve the "completed" getter by anchoring on the StrongCheckbox.r(ZZ)V call it feeds.
        val checkboxIndex = bind.indexOfFirstInstructionOrThrow {
            val reference = getReference<MethodReference>()
            opcode == Opcode.INVOKE_VIRTUAL &&
                reference?.definingClass == STRONG_CHECKBOX_CLASS &&
                reference.parameterTypes.map { it.toString() } == listOf("Z", "Z")
        }
        val completedRegister = bind.getInstruction<FiveRegisterInstruction>(checkboxIndex).registerD
        val completedMoveResult = bind.indexOfFirstInstructionReversedOrThrow(checkboxIndex) {
            opcode == Opcode.MOVE_RESULT &&
                (this as OneRegisterInstruction).registerA == completedRegister
        }
        val completedMethod = bind.getInstruction(completedMoveResult - 1)
            .getReference<MethodReference>()!!.name

        // Resolve the item field and the input-fields array on the holder by their (stable) types.
        val inputFieldsArrayType = "[$TEXT_FIELD_VIEW_CLASS"
        val itemField = holder.fields.first {
            it.type == setItemClass && !AccessFlags.STATIC.isSet(it.accessFlags)
        }.name
        val inputFieldsField = holder.fields.first { it.type == inputFieldsArrayType }.name

        // Resolve the CellType class and its integer id field. The factory method that maps cell
        // type names to instances contains all the (stable) type-name strings; its return type is
        // the CellType base, whose only instance int field is the stable type id.
        val cellTypeClass = firstMethodComposite {
            strings("BARBELL_WEIGHT", "WEIGHT", "REPS", "DURATION", "DISTANCE")
        }.method.returnType
        val cellTypeIdField = firstClassDef(cellTypeClass).fields.first {
            it.type == "I" && !AccessFlags.STATIC.isSet(it.accessFlags)
        }.name

        // The masking lives entirely in the extension; the bytecode only has to hand over the
        // obfuscated member names resolved above. They are passed as one comma-separated string
        // (item field, completed getter, input-fields field, cell-type id field) so the helper needs
        // a single scratch register, and the extension reflects over them at runtime.
        val memberNames = "$itemField,$completedMethod,$inputFieldsField,$cellTypeIdField"

        // Add a tiny helper that forwards the holder, the previous-value text and the member names
        // to the extension. A generated method gives it a clean register space, so nothing in the
        // (register-constrained) bind method has to be clobbered.
        holder.methods.add(
            ImmutableMethod(
                SET_VIEW_HOLDER_CLASS,
                MASK_HELPER_NAME,
                listOf(ImmutableMethodParameter("Ljava/lang/CharSequence;", null, null)),
                "Ljava/lang/CharSequence;",
                AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
                null,
                null,
                ImmutableMethodImplementation(3, emptyList(), emptyList(), emptyList()),
            ).toMutable().apply {
                addInstructions(
                    0,
                    """
                        const-string v0, "$memberNames"
                        invoke-static { p0, p1, v0 }, $EXTENSION_CLASS_DESCRIPTOR->maskPreviousValue(Ljava/lang/Object;Ljava/lang/CharSequence;Ljava/lang/String;)Ljava/lang/CharSequence;
                        move-result-object v0
                        return-object v0
                    """,
                )
            },
        )

        // Route the previous_value text (first setText in the bind method) through the helper.
        val setTextIndex = bind.indexOfFirstInstructionOrThrow {
            opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.name == "setText"
        }
        val textRegister = bind.getInstruction<FiveRegisterInstruction>(setTextIndex).registerD
        bind.addInstructions(
            setTextIndex,
            """
                invoke-virtual { p0, v$textRegister }, $SET_VIEW_HOLDER_CLASS->$MASK_HELPER_NAME(Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                move-result-object v$textRegister
            """,
        )
    }
}
