package de.tosox.revanced.patches.earphonealarm.premium

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks the Premium plan",
) {
    // Tested with 2.2.4
    compatibleWith("com.wixsite.ut_app.utalarm")

    execute {
        getPlanStatusFingerprint.method.addInstruction(
            0,
            """
                const/4 v0, 0x1
                return v0
             """
        )
    }
}
