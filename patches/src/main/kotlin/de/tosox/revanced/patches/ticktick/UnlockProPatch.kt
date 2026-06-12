package de.tosox.revanced.patches.ticktick

import app.revanced.patcher.definingClass
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.util.returnEarly

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lde/tosox/revanced/extension/ticktick/UnlockProPatch;"

internal val BytecodePatchContext.isProFingerprint by gettingFirstMethodDeclaratively {
    name("isPro")
    definingClass("User;")
}

internal val BytecodePatchContext.getProTypeForFakeFingerprint by gettingFirstMethodDeclaratively {
    name("getProTypeForFake")
    definingClass("User;")
}

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro subscription",
) {
    // Tested with 7.6.9.1
    compatibleWith("com.ticktick.task")

    extendWith("extensions/ticktick.rve")

    dependsOn(noIntegrityCheckPatch)

    apply {
        isProFingerprint.addInstructions(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->shouldBePro(Lcom/ticktick/task/data/User;)Z
                move-result v0
                return v0
            """
        )

        getProTypeForFakeFingerprint.returnEarly(1)
    }
}
