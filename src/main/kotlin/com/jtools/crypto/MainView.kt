package com.jtools.crypto

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.lhstack.tools.plugins.Logger
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class MainView(private val project: Project, val logger: Logger) : SimpleToolWindowPanel(false), Disposable {

    private val disposer = Disposer.newDisposable()

    init {
        toolbar = createActionToolbar()
        setContent(createContent())
    }

    private fun createContent(): JComponent = JPanel(BorderLayout()).apply {
        val splitter = JBSplitter().apply {
            this.firstComponent = DecryptView(project).apply {
                Disposer.register(disposer, this)
            }

            this.secondComponent = EncryptView(project,logger).apply {
                Disposer.register(disposer, this)
            }
        }
        this.add(splitter, BorderLayout.CENTER)
    }

    private fun createActionToolbar(): JComponent = DefaultActionGroup().let { group ->
        ActionManager.getInstance().createActionToolbar("JTools@Crypto@MainView", group, true).let {
            it.targetComponent = this
            it.component
        }
    }

    override fun dispose() {
        Disposer.dispose(disposer)
    }
}