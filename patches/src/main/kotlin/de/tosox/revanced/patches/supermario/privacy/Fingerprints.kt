package de.tosox.revanced.patches.supermario.privacy

import app.revanced.patcher.fingerprint

internal val appInstallFingerprint = fingerprint {
    parameters(
        "Landroid/content/Context",
        "Ljava/lang/String",
        "Ljava/lang/String"
    )
    custom { method, classDef ->
        classDef.endsWith("/FacebookHelper;") && method.name == "appInstall"
    }
}

internal val isNetworkConnectedFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/Utils;") && method.name == "isNetworkConnected"
    }
}

internal val checkErrorFingerprint = fingerprint {
    strings("false")
    custom { method, classDef ->
        classDef.endsWith("/WebViewActivity;") && method.name == "checkError"
    }
}

internal val onCreateFingerprint = fingerprint {
    strings("false")
    custom { method, classDef ->
        classDef.endsWith("/WebViewActivity;") && method.name == "onCreate"
    }
}
