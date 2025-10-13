package de.tosox.revanced.patches.customstickermaker.pro

import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.patches.customstickermaker.verify.noIntegrityCheckPatch
import de.tosox.revanced.util.returnEarly

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro subscription",
) {
    // Tested with 1.20.55
    compatibleWith("customstickermaker.whatsappstickers.personalstickersforwhatsapp")

    dependsOn(noIntegrityCheckPatch)

    execute {
        isProFingerprint.method.returnEarly(true)
    }
}
