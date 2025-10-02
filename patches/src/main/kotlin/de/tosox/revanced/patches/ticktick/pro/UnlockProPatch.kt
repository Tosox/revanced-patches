package de.tosox.revanced.patches.ticktick.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.patches.ticktick.verify.noIntegrityCheckPatch

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lde/tosox/revanced/extension/ticktick/pro/UnlockProPatch;"

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro plan",
) {
    // Tested with 7.6.9.1
    compatibleWith("com.ticktick.task")

    execute {
        isProFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->shouldBePro(Lcom/ticktick/task/data/User;)Z
                move-result v0
                return v0
            """
        )

        getProTypeForFakeFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
