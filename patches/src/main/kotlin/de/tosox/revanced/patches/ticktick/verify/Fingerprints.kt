package de.tosox.revanced.patches.ticktick.verify

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.Opcode

internal val verifyJobFingerprint = fingerprint {
    opcodes(
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
    )
    custom { method, classDef ->
        classDef.endsWith("/VerifyJob;") && method.name == "onRun"
    }
}
