package de.tosox.revanced.patches.ticktick.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import de.tosox.revanced.patches.ticktick.verify.noIntegrityCheckPatch

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lde/tosox/revanced/extension/ticktick/pro/UnlockProPatch;"
internal const val EXTENSION_METHOD_NAME = "shouldBePro"

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro plan",
) {
    // Tested with 7.6.9.1
    compatibleWith("com.ticktick.task")

    dependsOn(noIntegrityCheckPatch)

    execute {
        isProFingerprint.method.addInstruction(
            0,
            """
                invoke-static { p0 }, $EXTENSION_CLASS_DESCRIPTOR->$EXTENSION_METHOD_NAME(Lcom/ticktick/task/data/User;)Z
                move-result v0
                return v0
            """
            /*
            """
                iget-object v0, p0, Lcom/ticktick/task/data/User;->username:Ljava/lang/String;
                
                if-eqz v0, :cond_0
                
                const/4 v0, 0x1
                return v0
                
                :cond_0
                const/4 v0, 0x0
                return v0
             """
             */
        )

        getProTypeForFakeFingerprint.method.addInstruction(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
