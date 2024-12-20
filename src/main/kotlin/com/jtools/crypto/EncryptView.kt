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
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.intellij.ui.awt.RelativePoint
import com.lhstack.tools.plugins.Logger
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class EncryptView(private val project: Project, logger: Logger) : JPanel(BorderLayout()), Disposable {

    private val disposer = Disposer.newDisposable()

    init {

        //输出数据
        val output = MultiLanguageTextField(PlainTextFileType.INSTANCE, project, "", consumerSettings = {
            it.settings.isUseSoftWraps = true
        }).apply {
            Disposer.register(disposer, this)
        }
        //输入数据
        val input = MultiLanguageTextField(Json5FileType.INSTANCE, project, "").apply {
            Disposer.register(disposer, this)

            this.document.addDocumentListener(object :DocumentListener{
                override fun documentChanged(event: DocumentEvent) {
                    Global.getInstance(project).selectCryptoInstance()?.let {crypto ->
                        document.text.takeIf { it.isNotEmpty() }?.let {
                            output.text = crypto.encrypt(document.text.toByteArray(StandardCharsets.UTF_8))
                        }
                    }
                }
            },this)
        }

        val currLabel = JLabel(Global.getInstance(project).selectCryptoInstance()?.let {
            "(${it.name})${it.type}"
        }?:"无").apply {
            val that = this
            this.toolTipText = "上次选择的加密方式"
            this.addMouseListener(object:MouseAdapter(){
                override fun mouseClicked(e: MouseEvent) {
                    if(SwingUtilities.isLeftMouseButton(e)){
                        val popup = JBPopupFactory.getInstance().createListPopup(object :
                            BaseListPopupStep<Crypto>("选择加密使用的项", Global.getInstance(project).cryptoList()) {
                            override fun getTextFor(value: Crypto): String {
                                return "(${value.name})${value.type}"
                            }

                            override fun onChosen(crypto: Crypto, finalChoice: Boolean): PopupStep<*>? {
                                Global.getInstance(project).selectCrypto = crypto.toJson()
                                that.text = "(${crypto.name})${crypto.type}"
                                return super.onChosen(crypto, finalChoice)
                            }
                        })
                        popup.size = Dimension(300,200)
                        popup.show(RelativePoint(e.component, Point(e.point.x,e.point.y + 10)))
                    }
                }
            })
        }

        this.add(JPanel(BorderLayout()).apply {
            this.add(JLabel("加密: "), BorderLayout.WEST)

            val keyGenerateActionButton = object:AnAction({"密钥生成"},AllIcons.Actions.Dump){
                override fun actionPerformed(e: AnActionEvent) {
                    val popup = JBPopupFactory.getInstance().createListPopup(object :
                        BaseListPopupStep<Map.Entry<String,Consumer<Logger>>>("选择生成密钥的方式",generators.entries.toList()) {
                        override fun getTextFor(value: Map.Entry<String,Consumer<Logger>>): String {
                            return "(${value.key})"
                        }

                        override fun onChosen(value: Map.Entry<String,Consumer<Logger>>, finalChoice: Boolean): PopupStep<*>? {
                            value.value.accept(logger)
                            return super.onChosen(value, finalChoice)
                        }
                    })
                    popup.size = Dimension(300,200)
                    popup.showInBestPositionFor(e.dataContext)
                }

            }


            val fastEncryptActionButton = object : AnAction({ "快速加密" }, AllIcons.Actions.Execute) {
                override fun actionPerformed(e: AnActionEvent) {
                    val crypto = Global.getInstance(project).selectCryptoInstance()
                    if (crypto == null) {
                        output.text = "当前未存在缓存的加密项"
                    } else {
                        crypto.let {
                            if(input.text.isNotEmpty()){
                                output.text = it.encrypt(input.text.toByteArray(StandardCharsets.UTF_8))
                            }else {
                                output.text = "请输入要加密的内容"
                            }
                        }
                    }

                }

                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

            }

            val encryptActionButton = object : AnAction({ "选择加密方式加密" }, AllIcons.Actions.Rerun) {
                override fun actionPerformed(e: AnActionEvent) {
                    val popup = JBPopupFactory.getInstance().createListPopup(object :
                        BaseListPopupStep<Crypto>("选择加密使用的项", Global.getInstance(project).cryptoList()) {
                        override fun getTextFor(value: Crypto): String {
                            return "(${value.name})${value.type}"
                        }

                        override fun onChosen(crypto: Crypto, finalChoice: Boolean): PopupStep<*>? {
                            Global.getInstance(project).selectCrypto = crypto.toJson()
                            currLabel.text = "(${crypto.name})${crypto.type}"
                            if (input.text.isNotEmpty()) {
                                try {
                                    val rawBytes = input.text.toByteArray(StandardCharsets.UTF_8)
                                    output.text = crypto.encrypt(rawBytes)
                                } catch (e: Throwable) {
                                    output.text =
                                        e.toString() + "\r\n" + e.stackTrace.joinToString("\r\n") { it.toString() }
                                }
                            } else {
                                output.text = "请输入要加密的内容"
                            }
                            return super.onChosen(crypto, finalChoice)
                        }
                    })
                    popup.size = Dimension(300,200)
                    popup.showInBestPositionFor(e.dataContext)
                }

                override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

            }

            val keyGenerateButton = ActionButton(keyGenerateActionButton,
                PresentationFactory().getPresentation(keyGenerateActionButton),
                "JTools@DeCryptoMainView@KeyGenerateButton",
                ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)

            val encryptButton = ActionButton(
                encryptActionButton,
                PresentationFactory().getPresentation(encryptActionButton),
                "JTools@DeCryptoMainView@EncryptActionButton",
                ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
            )

            val fastEncryptButton = ActionButton(
                fastEncryptActionButton,
                PresentationFactory().getPresentation(fastEncryptActionButton),
                "JTools@DeCryptoMainView@FastEncryptActionButton",
                ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
            )

            this.add(JPanel().apply {
                this.layout = BoxLayout(this, BoxLayout.X_AXIS)
                this.add(currLabel)
                this.add(keyGenerateButton)
                this.add(encryptButton)
                this.add(fastEncryptButton)
            }, BorderLayout.EAST)
        }, BorderLayout.NORTH)




        this.add(JPanel(BorderLayout()).apply {
            this.add(JBSplitter(true).apply {
                this.firstComponent = input
                this.secondComponent = output
            })
        }, BorderLayout.CENTER)
    }

    override fun dispose() {
        Disposer.dispose(disposer)
    }
}