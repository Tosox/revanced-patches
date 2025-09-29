package de.tosox.revanced.patches.mega.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads Patch",
    description = "Hides ads across the MEGA app",
) {
    compatibleWith("mega.privacy.android.app")
    // Tested with 15.17(252540921)(aca7200e36)

    execute {
        showAdsFingerprint.method.addInstruction(
            0,
            """
                return-void
             """
        )
    }
}
