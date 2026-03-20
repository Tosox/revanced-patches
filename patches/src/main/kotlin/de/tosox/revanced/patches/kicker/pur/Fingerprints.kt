package de.tosox.revanced.patches.kicker.pur

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getAboStateFingerprint by gettingFirstMethodDeclaratively {
    name("getAboState")
    definingClass("KUserImpl;")
}
