package de.tosox.revanced.patches.ticktick.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.patches.ticktick.verify.noIntegrityCheckPatch
import de.tosox.revanced.util.returnEarly

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lde/tosox/revanced/extension/ticktick/pro/UnlockProPatch;"

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro subscription",
) {
    // Tested with 7.6.9.1
    compatibleWith("com.ticktick.task")

    extendWith("extensions/ticktick.rve")

    dependsOn(noIntegrityCheckPatch)

    execute {
        isProFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->shouldBePro(Lcom/ticktick/task/data/User;)Z
                move-result v0
                return v0
            """
        )

        getProTypeForFakeFingerprint.method.returnEarly(1)
    }
}
