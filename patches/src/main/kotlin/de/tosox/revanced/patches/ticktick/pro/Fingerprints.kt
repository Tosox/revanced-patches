package de.tosox.revanced.patches.ticktick.pro

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isProFingerprint by gettingFirstMethodDeclaratively {
    name("isPro")
    definingClass("User;")
}

internal val BytecodePatchContext.getProTypeForFakeFingerprint by gettingFirstMethodDeclaratively {
    name("isProTypeForFake")
    definingClass("User;")
}
