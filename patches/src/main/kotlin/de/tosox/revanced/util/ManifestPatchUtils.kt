package de.tosox.revanced.util

import app.revanced.patcher.patch.Patch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.resourcePatch

internal fun removePermissionsPatch(vararg permissions: String): Patch = resourcePatch(
    name = "Remove permissions",
    description = "Removes permissions from the Android manifest",
) {
    apply {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getNode("manifest")

            permissions.forEach { permission ->
                val matchingNodes = manifest.childElementsSequence()
                    .filter { it.tagName.startsWith("uses-permission") }
                    .filter { it.getAttribute("android:name") == permission }
                    .toList()

                if (matchingNodes.isEmpty()) {
                    throw PatchException("Could not find permission: $permission")
                }

                matchingNodes.forEach {
                    it.removeFromParent()
                }
            }
        }
    }
}
