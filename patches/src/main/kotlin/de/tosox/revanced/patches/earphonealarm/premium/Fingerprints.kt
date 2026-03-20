package de.tosox.revanced.patches.earphonealarm.premium

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getPlanStatusFingerprint by gettingFirstMethodDeclaratively {
    name("getPlanStatus")
    definingClass("SharedPrefs;")
}
