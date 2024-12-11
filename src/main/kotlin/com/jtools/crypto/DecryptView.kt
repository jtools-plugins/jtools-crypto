package com.jtools.crypto

import com.intellij.icons.AllIcons
import com.intellij.json.json5.Json5FileType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.actionSystem.impl.PresentationFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.lhstack.tools.plugins.Helper
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class DecryptView(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val disposer = Disposer.newDisposable()

    init {
        val output =
            MultiLanguageTextField(Json5FileType.INSTANCE, project, "", consumerSettings = {
                it.settings.isUseSoftWraps = true
            }).apply {
                Disposer.register(disposer, this)
            }

        val input =
            MultiLanguageTextField(PlainTextFileType.INSTANCE, project, "", consumerSettings = {
                it.settings.isUseSoftWraps = true
            }).apply {
                Disposer.register(disposer, this)
                this.document.addDocumentListener(object : DocumentListener {
                    override fun documentChanged(event: DocumentEvent) {
                        event.document.text.takeIf { it.isNotEmpty() }?.let {
                            decrypt(it, output)
                        }
                    }
                }, this)
            }


        val splitter = JBSplitter(true).apply {
            this.firstComponent = input
            this.secondComponent = output
        }
        this.add(JPanel(BorderLayout()).apply {
            this.add(JLabel("解密: "), BorderLayout.WEST)
            val decryptActionButton = object : AnAction({ "解密配置" }, AllIcons.Actions.EditSource) {
                override fun actionPerformed(e: AnActionEvent) {
                    DecryptEnvDialog(project).show()
                }

                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

            }
            val button = ActionButton(
                decryptActionButton,
                PresentationFactory().getPresentation(decryptActionButton),
                "JTools@DeCryptoMainView@DeCryptoActionButton",
                ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
            )
            this.add(button, BorderLayout.EAST)
        }, BorderLayout.NORTH)
        this.add(splitter, BorderLayout.CENTER)
    }

    private fun decrypt(text: String, output: MultiLanguageTextField) {
        val rawBytes:ByteArray?
        val isBase64 = try{
            text != Hex.encodeHexString(Hex.decodeHex(text))
        }catch (e:Throwable){
            true
        }
        rawBytes = if(isBase64){
            Base64.decodeBase64(text)
        }else {
            Hex.decodeHex(text)
        }
        var flag = false
        Global.getInstance(project).cryptoList().forEachIndexed { _, decrypt ->
            try{
                output.text = decrypt.decrypt(rawBytes)
                flag = true
            }catch (e:Throwable){
//                Helper.getSysLogger(project.locationHash).error("${decrypt.type}_${decrypt.name}\r\n" + e.toString() + "\r\n" + e.stackTrace.joinToString("\r\n") { it.toString() })
            }
        }
        if(!flag){
            output.text = "解密失败"
        }
    }

    override fun dispose() {
        Disposer.dispose(disposer)
    }
}