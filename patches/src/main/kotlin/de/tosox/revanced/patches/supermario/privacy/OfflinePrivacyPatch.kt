package de.tosox.revanced.patches.supermario.privacy

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import de.tosox.revanced.util.childElementsSequence
import de.tosox.revanced.util.getNode
import de.tosox.revanced.util.returnEarly

// TODO: Add to shared patches
private val disableInternetPermissionPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            document.getNode("manifest").apply {
                removeChild(
                    childElementsSequence().first {
                        it.attributes.getNamedItem("android:name")
                            ?.nodeValue?.equals("android.permission.INTERNET") == true
                    }
                )
            }
        }
    }
}

@Suppress("unused")
val offlinePrivacyPatch = bytecodePatch(
    name = "Offline Privacy",
    description = "Blocks ads and tracking by forcing the app to be offline"
) {
    // Tested with 1.0.1
    compatibleWith("superadventure.mario.classic.bros.retrogame")

    dependsOn(disableInternetPermissionPatch)

    execute {
        // Nullify method to disable crashes because of no internet access
        appInstallFingerprint.method.returnEarly()

        // TODO: Create shared prefs shared patch
        checkErrorFingerprint.method.apply {
            val targetStringIndex = checkErrorFingerprint.stringMatches!!.first().index
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA
            replaceInstruction(
                targetStringIndex,
                "const-string v$targetStringRegister, \"true\"",
            )
        }

        onCreateFingerprint.method.apply {
            val targetStringIndex = onCreateFingerprint.stringMatches!!.first().index
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA
            replaceInstruction(
                targetStringIndex,
                "const-string v$targetStringRegister, \"true\"",
            )
        }
    }
}
