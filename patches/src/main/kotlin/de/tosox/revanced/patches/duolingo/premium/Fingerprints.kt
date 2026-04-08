package de.tosox.revanced.patches.duolingo.premium

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.Opcode

internal val BytecodePatchContext.userSyntheticConstructorFingerprint by composingFirstMethod {
    name("<init>")
    definingClass("User;")
    returnType("V")
    instructions(
        allOf(
            Opcode.IPUT_OBJECT(),
            field { type.endsWith("SubscriberLevel;") }
        ),
        method("getDescriptor")
    )
}

internal val BytecodePatchContext.userConstructorFingerprint by composingFirstMethod(
    "id",
    "betaStatus",
    "subscriberLevel"
) {
    name("<init>")
    definingClass("User;")
    returnType("V")
    instructions(
        allOf(
            Opcode.IPUT_OBJECT(),
            field { type.endsWith("SubscriberLevel;") }
        )
    )
}
