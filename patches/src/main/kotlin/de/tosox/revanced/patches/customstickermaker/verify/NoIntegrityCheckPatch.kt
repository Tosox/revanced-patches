package de.tosox.revanced.patches.customstickermaker.verify

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import de.tosox.revanced.util.findMutableMethodOf

@Suppress("unused")
val noIntegrityCheckPatch = bytecodePatch(
    name = "No Integrity Check",
    description = "Disables the integrity checks",
) {
    // Tested with 1.20.55
    compatibleWith("customstickermaker.whatsappstickers.personalstickersforwhatsapp")

    execute {
        exitProcessFingerprint.method.addInstructions(
            0,
            """
                new-instance p0, Ljava/lang/RuntimeException;
                invoke-direct { p0, p1 }, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V
                return-object p0
            """
        )

        // Nop all 'throw null's in signature checks
        val opcodePattern = listOf(
            Opcode.INVOKE_STATIC,
            Opcode.THROW,
            Opcode.INVOKE_VIRTUAL,
            Opcode.INVOKE_STATIC,
            Opcode.THROW
        )

        val patches = buildMap {
            classes.forEach { classDef ->
                val methodMap = buildMap<Method, ArrayDeque<Int>> {
                    classDef.methods.forEach { method ->
                        val instructions = method.implementation?.instructions?.toList() ?: return@forEach
                        val indices = ArrayDeque<Int>()

                        for (i in 0..instructions.size - opcodePattern.size) {
                            var match = true
                            for (j in opcodePattern.indices) {
                                if (instructions[i + j].opcode != opcodePattern[j]) {
                                    match = false
                                    break
                                }
                            }
                            if (match) {
                                indices.add(i + opcodePattern.size - 1)
                            }
                        }

                        if (indices.isNotEmpty()) put(method, indices)
                    }
                }
                if (methodMap.isNotEmpty()) put(classDef, methodMap)
            }
        }

        patches.forEach { (classDef, methods) ->
            val mutableClass = proxy(classDef).mutableClass
            methods.forEach { (method, indices) ->
                val mutableMethod = mutableClass.findMutableMethodOf(method)
                while (indices.isNotEmpty()) {
                    val index = indices.removeLast()
                    mutableMethod.replaceInstruction(index, "nop")
                }
            }
        }
    }
}
