package com.jtools.crypto

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Disposer
import org.jetbrains.yaml.YAMLFileType
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.JComponent

class DecryptEnvDialog(private val project:Project):DialogWrapper(project) {

    private val textField = MultiLanguageTextField(YAMLFileType.YML,project,
        Global.getInstance(project).crypto
    ).let {
        it.document.addDocumentListener(object : DocumentListener{
            override fun documentChanged(event: DocumentEvent) {
                Global.getInstance(project).crypto = event.document.text
            }
        },it)
        Disposer.register(this.disposable,it)
        it
    }
    init {
        this.title = "解密配置"
        this.setSize(800,600)
        this.setOKButtonText("确认")
        this.setCancelButtonText("取消")
        this.init()
    }

    override fun createActions(): Array<Action> {
        return arrayOf(object: AbstractAction("模板") {
            override fun actionPerformed(e: ActionEvent) {
                val text = textField.text
                if(text.isNotEmpty()){
                    if(Messages.OK != Messages.showOkCancelDialog(project,"再次点击确认会覆盖已有配置","警告","确定","取消",AllIcons.General.WarningDialog)){
                        return
                    }
                }
                textField.text = """
                    - key: asdtixla146sxaz5
                      type: AES_CBC
                      iv: axksleitlglxlakz
                    - key: 126ax5f6g4564a2x
                      type: AES_ECB
                    - key: |
                        axksagfksldfnaskldgvncklsdaasd
                        asdfkjlxcvaksldfjosdigjasdgsdf
                      type: RSA_PUBLIC
                    - key: |
                        asdfkjlcvkmasdoifgjasdglkjsdl
                        asdfkjlkvasdklgjsdlagk
                      type: RSA_PRIVATE
                """.trimIndent()
            }
        },okAction)
    }
    override fun createCenterPanel(): JComponent = textField
}