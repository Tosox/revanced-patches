package de.tosox.revanced.patches.kicker.plus

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getAboStatePlusFingerprint by gettingFirstMethodDeclaratively {
    name("getAboStatePlus")
    definingClass("KUserImpl;")
}
