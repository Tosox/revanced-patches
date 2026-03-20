package de.tosox.revanced.patches.supermario.privacy

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.appInstallFingerprint by gettingFirstMethodDeclaratively {
    parameterTypes(
        "Landroid/content/Context",
        "Ljava/lang/String",
        "Ljava/lang/String"
    )
    name("appInstall")
    definingClass("FacebookHelper;")
}

internal val BytecodePatchContext.isNetworkConnectedFingerprint by gettingFirstMethodDeclaratively {
    name("isNetworkConnected")
    definingClass("Utils;")
}

internal val BytecodePatchContext.checkErrorFingerprint by composingFirstMethod {
    strings("false")
    name("checkError")
    definingClass("WebViewActivity;")
}

internal val BytecodePatchContext.onCreateFingerprint by composingFirstMethod {
    strings("false")
    name("onCreate")
    definingClass("WebViewActivity;")
}
