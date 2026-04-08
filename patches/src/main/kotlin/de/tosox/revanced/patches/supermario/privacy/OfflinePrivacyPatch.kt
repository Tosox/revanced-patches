package de.tosox.revanced.patches.supermario.privacy

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import de.tosox.revanced.util.removePermissionsPatch
import de.tosox.revanced.util.returnEarly

private val disableInternetPermissionPatch = removePermissionsPatch(
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE"
)

@Suppress("unused")
val offlinePrivacyPatch = bytecodePatch(
    name = "Offline Privacy",
    description = "Blocks ads and tracking by forcing the app to be offline"
) {
    // Tested with 1.0.1
    compatibleWith("superadventure.mario.classic.bros.retrogame")

    dependsOn(disableInternetPermissionPatch)

    apply {
        // Nullify method to stop crashes because of no internet access
        appInstallFingerprint.returnEarly()

        // Return false to stop crashes when the application is not signed with a test key
        isNetworkConnectedFingerprint.returnEarly(false)

        // TODO: Create shared prefs shared patch
        checkErrorFingerprint.method.apply {
            val targetStringIndex = checkErrorFingerprint[0]
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA
            replaceInstruction(
                targetStringIndex,
                "const-string v$targetStringRegister, \"true\"",
            )
        }

        onCreateFingerprint.method.apply {
            val targetStringIndex = onCreateFingerprint[0]
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA
            replaceInstruction(
                targetStringIndex,
                "const-string v$targetStringRegister, \"true\"",
            )
        }
    }
}
