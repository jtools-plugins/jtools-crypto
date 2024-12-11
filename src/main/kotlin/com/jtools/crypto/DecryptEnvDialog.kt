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
                      name: 测试AES_CBC
                      encryptType: base64
                    - key: 126ax5f6g4564a2x
                      type: AES_ECB
                      name: 测试AES_ECB
                      encryptType: hex
                    - key: |
                        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo7LwQlH3ErS3ejLS3cFv9KkLVYsRjLTx
                        Axf36d4qUBc4mw+CRi0HDJvcTYpY82tXo/U8tSJSEV9BGSshPUuj7ZVwBS3GvBo1gqcliME526eJ
                        m4Ss24AUp53HnahnBJUY1YpDXs6pbAyJfjITAAdmf3JZ7wPqgYuM1yGjCF6TYGZbmbybeJA18It0
                        K2WWCbRbq3Qy/Oy9cwisXB8sz9teXka41uJAVN5yv4tRpLoPlQDX/Pa9jmfj0bxunlpgvJMTcTxP
                        z5WI4wSCcotkolLP1KZUZtXb+4jHR7qhV7B/XmcmNPIWxZmaj9dQCbsMTXD5mYCRdD7+cdUjliRM
                        L73IqQIDAQAB
                      type: RSA_PUBLIC
                      name: 测试_RSA_PUBLIC
                      encryptType: base64
                    - key: |
                        MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCjsvBCUfcStLd6MtLdwW/0qQtV
                        ixGMtPEDF/fp3ipQFzibD4JGLQcMm9xNiljza1ej9Ty1IlIRX0EZKyE9S6PtlXAFLca8GjWCpyWI
                        wTnbp4mbhKzbgBSnncedqGcElRjVikNezqlsDIl+MhMAB2Z/clnvA+qBi4zXIaMIXpNgZluZvJt4
                        kDXwi3QrZZYJtFurdDL87L1zCKxcHyzP215eRrjW4kBU3nK/i1Gkug+VANf89r2OZ+PRvG6eWmC8
                        kxNxPE/PlYjjBIJyi2SiUs/UplRm1dv7iMdHuqFXsH9eZyY08hbFmZqP11AJuwxNcPmZgJF0Pv5x
                        1SOWJEwvvcipAgMBAAECggEADL9MhweRszjUIE2IntjXNmC7B5dnn5LAAVcKmMEyf4/dWjLKGNud
                        BMn2zOl47YSme5enWW4RoLVvkO2LPjYPjvrP8TFMPF3YnKLWMQjl7+FDHZOGb+XQhfBrCM+CLSjM
                        mSVWA3IZnpa31rZAxUPuQv6lOu/twRZ8Okzt4IMX3B3ge7TjXHA3prERaXUtgu0B6HKRYFqK5AQ4
                        cSHVRR7e6tHjmEzY3Qbo9CYy3FJfNtLL1x0kPwNoPkkXbdwRj4yLNa6PK4HvACYvBGyF8412NtzF
                        SElFlH7YjKk+cBLc/sSwf8doWgg6/taLVXi9H8qKrLLHvowIgYgCD27V/kLIgQKBgQDEf65vt+uE
                        b2tbgkNWVGamt723n8Zq34fLQM88Z6EVuYLod38srd3QjLeWXDoJk4X7VhZ8jgQnX0b1w3Sv7WL9
                        NiygaM0hutNSirLux0QqrU/0q7c8a5T7Sf67+Qw56hVxf0tW4Yw9DiTN0MmWm1oJ51KUw5dZs7W5
                        PZu4HOFJKQKBgQDVRKmig/x3uKhAg4g+fI/mGfHFTTbqCkCQ+pee2DjG6BSmQfT0a3OiCe/Ke0Wm
                        n7FxhvrFow9kpwNFlBlEs9GZLQVfH/9wABV9Jq3FkSWEwEDgz0nlPnQy8zqb1XMtXQJdvCiNUS8H
                        iVuRWh20cqtLdPuyBMuh+wA9qPGMRlbzgQKBgGICDU9LvPzhVfrejheRIWImY0ojNyQ3OdP3D2Zh
                        icb8MH8imxv93JUYmrk0Zv4/xqQF3FFjxE6fBxWt15WoIaeKTdf0bdxVAvI4m+bdHPLsXuT6gPpx
                        7m0oCxZWsfJw/yENDNbYyMnrNBA0A1hLRC2MY3Qv2l2zdQBf6jt6rCOBAoGAfQ3nHomgWB9qYXH/
                        SQzHYFjkQRpW0gOy+pBGqfkFyfS6bwcL+OqtAl4rQqLoI51OclCA6bUEyLN6IqmF65g5lkUk+jOM
                        GAOklh9BFDFZO690G57RKOBnSJ0BR/FwuS/pvEchzEPHfPbZP7EE7BO1R+jP0gIn2dKRwJY5FVUF
                        lYECgYBTqE9msqOFEzlzXiAWxi+ACLUuesqopn1RPAIhTjm+W5TGou9WwU9QN6MM981cQVq60a7O
                        xEqaNkfHs0ETJZADUeFMilL6io4PQunQoX3NU3EOvFNeoKoq3vzhqZ69Ljw+EII21YGqhJViLxZd
                        mIQc9XfeBh7PXvDJFH+wi9ijDw==
                      type: RSA_PRIVATE
                      name: 测试_RSA_PRIVATE
                      encryptType: hex
                """.trimIndent()
            }
        },okAction)
    }
    override fun createCenterPanel(): JComponent = textField
}