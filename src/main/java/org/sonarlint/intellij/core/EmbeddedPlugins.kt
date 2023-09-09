/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2023 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.core

import org.sonarlint.intellij.SonarLintPlugin
import org.sonarlint.intellij.common.LanguageActivator
import org.sonarlint.intellij.common.util.SonarLintUtils
import org.sonarlint.intellij.util.GlobalLogOutput
import org.sonarsource.sonarlint.core.commons.Language
import org.sonarsource.sonarlint.core.commons.log.ClientLogOutput
import org.sonarsource.sonarlint.core.commons.log.SonarLintLogger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.EnumSet
import java.util.function.BiConsumer
import java.util.function.Consumer

object EmbeddedPlugins {

    private val log = SonarLintLogger.get()

    private val STANDALONE_LANGUAGES: Set<Language> = EnumSet.of(
        Language.HTML,
        Language.XML,
        Language.JS,
        Language.CSS,
        Language.KOTLIN,
        Language.PHP,
        Language.PYTHON,
        Language.RUBY,
        Language.SECRETS,
        Language.TS,
        Language.YAML
        // Language.PMD // ???
        // Language.JAVA // ???
    )

    private val CONNECTED_ADDITIONAL_LANGUAGES: Set<Language> = EnumSet.of(
        Language.SCALA,
        Language.SWIFT
    )

    private val EMBEDDED_PLUGINS = listOf(
        // EmbeddedPlugin(Language.PMD, "Pmd", "sonar-pmd-plugin-*.jar"), // ???
        EmbeddedPlugin(Language.CPP, "CFamily", "sonar-cfamily-plugin-*.jar"),
        EmbeddedPlugin(Language.CS, "CSharp", "sonarlint-omnisharp-plugin-*.jar"),
        EmbeddedPlugin(Language.HTML, "HTML", "sonar-html-plugin-*.jar"),
        EmbeddedPlugin(Language.JS, "JavaScript/TypeScript", "sonar-javascript-plugin-*.jar"),
        EmbeddedPlugin(Language.KOTLIN, "Kotlin", "sonar-kotlin-plugin-*.jar"),
        EmbeddedPlugin(Language.RUBY, "Ruby", "sonar-ruby-plugin-*.jar"),
        EmbeddedPlugin(Language.XML, "XML", "sonar-xml-plugin-*.jar")
    )

    private val EXTRA_PLUGINS = listOf(
        EmbeddedPlugin(Language.SECRETS, "Secrets detection", "sonar-secrets-plugin-*.jar")
    )

    @JvmStatic
    fun getEmbeddedPluginsForConnectedMode(): Map<String, Path> {
        return EMBEDDED_PLUGINS.mapNotNull {
            val path = findEmbeddedPlugin(getPluginsDir(), it);
            if (path != null) {
                it.pluginKey to path
            } else {
                null
            }
        }.toMap()
    }

    @JvmStatic
    fun getExtraPluginsForConnectedMode(): Map<String, Path> {
        return EXTRA_PLUGINS.mapNotNull {
            val path = findEmbeddedPlugin(getPluginsDir(), it);
            if (path != null) {
                it.pluginKey to path
            } else {
                null
            }
        }.toMap()
    }

    @JvmStatic
    val enabledLanguagesInConnectedMode: Set<Language>
        get() {
            val enabledLanguages = EnumSet.copyOf(STANDALONE_LANGUAGES)
            enabledLanguages.addAll(CONNECTED_ADDITIONAL_LANGUAGES)
            amendEnabledLanguages(enabledLanguages) // 与ide交互获取工程主语言
            return enabledLanguages
        }

    @JvmStatic
    val enabledLanguagesInStandaloneMode: Set<Language>
        get() {
            val enabledLanguages = EnumSet.copyOf(STANDALONE_LANGUAGES)
            amendEnabledLanguages(enabledLanguages)
            return enabledLanguages
        }

    private fun amendEnabledLanguages(enabledLanguages: Set<Language>) {
        val languageActivator = LanguageActivator.EP_NAME.extensionList
        languageActivator.forEach(
            Consumer { l: LanguageActivator -> l.amendLanguages(enabledLanguages) }
        )
    }

    @JvmStatic
    @Throws(IOException::class)
    fun findEmbeddedPlugins(): Set<Path> {
        val lst = getPluginsUrls(getPluginsDir())
        for (e in lst) log.info("plugin --- $e")
        return lst
    }

    private fun getPluginsDir(): Path {
        val plugin = SonarLintUtils.getService(SonarLintPlugin::class.java)
        return plugin.path.resolve("plugins")
    }

    private fun findEmbeddedPlugin(pluginsDir: Path, embeddedPlugin: EmbeddedPlugin): Path? {
        return try {
            val pluginsPaths = findFilesInDir(
                pluginsDir,
                embeddedPlugin.jarFilePattern, "Found " + embeddedPlugin.name + " plugin: "
            )
            check(pluginsPaths.size <= 1) { "Multiple plugins found" }
            if (pluginsPaths.size == 1) pluginsPaths.first() else null
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    @Throws(IOException::class)
    private fun getPluginsUrls(pluginsDir: Path): Set<Path> {
        return findFilesInDir(pluginsDir, "*.jar", "Found plugin: ")
    }

    @Throws(IOException::class)
    private fun findFilesInDir(pluginsDir: Path, pattern: String, logPrefix: String): Set<Path> {
        val pluginsPaths = HashSet<Path>()
        if (Files.isDirectory(pluginsDir)) {
            Files.newDirectoryStream(pluginsDir, pattern).use { directoryStream ->
                val globalLogOutput = SonarLintUtils.getService(GlobalLogOutput::class.java)
                for (path in directoryStream) {
                    globalLogOutput.log(logPrefix + path.fileName.toString(), ClientLogOutput.Level.DEBUG)
                    pluginsPaths.add(path)
                }
            }
        }
        return pluginsPaths
    }

    private class EmbeddedPlugin(
        val pluginKey: String,
        val name: String,
        val jarFilePattern: String
    ) {
        constructor(
            language: Language,
            name: String,
            jarFilePattern: String
        ) : this(
            language.pluginKey, name, jarFilePattern
        )
    }
}
