package com.jtools.crypto

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.lhstack.tools.plugins.Helper
import com.lhstack.tools.plugins.IPlugin
import com.lhstack.tools.plugins.Logger
import java.awt.BorderLayout
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel

class CryptoPluginImpl: IPlugin {

    companion object{
        val cache = hashMapOf<String,Disposable>()
        val loggerCache = hashMapOf<String,Logger>()
    }

    override fun createPanel(project: Project): JComponent {
        return cache.computeIfAbsent(project.locationHash){
            MainView(project, loggerCache[project.locationHash]!!)
        } as JComponent
    }

    override fun openProject(project: Project, logger: Logger, openThisPage: Runnable?) {
        super.openProject(project, logger, openThisPage)
        loggerCache[project.locationHash] = logger
    }

    override fun installRestart(): Boolean = true


    override fun closeProject(project: Project) {
        cache.remove(project.locationHash)?.let { Disposer.dispose(it) }
    }

    override fun pluginIcon(): Icon = Helper.findIcon("icons/logo.svg",CryptoPluginImpl::class.java)

    override fun pluginTabIcon(): Icon = Helper.findIcon("icons/logo_tab.svg",CryptoPluginImpl::class.java)

    override fun pluginName(): String = "Crypto"

    override fun pluginDesc(): String = "加解密插件"

    override fun pluginVersion(): String = "0.0.1"
}