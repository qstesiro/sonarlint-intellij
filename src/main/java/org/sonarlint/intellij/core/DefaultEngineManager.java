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
package org.sonarlint.intellij.core;

import com.google.common.base.Preconditions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.serviceContainer.NonInjectable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonarlint.intellij.config.global.ServerConnection;
import org.sonarlint.intellij.core.server.events.ServerEventsService;
import org.sonarlint.intellij.exception.InvalidBindingException;
import org.sonarlint.intellij.notifications.AnalysisRequirementNotifications;
import org.sonarlint.intellij.notifications.SonarLintProjectNotifications;
import org.sonarsource.sonarlint.core.client.api.common.SonarLintEngine;
import org.sonarsource.sonarlint.core.client.api.connected.ConnectedSonarLintEngine;
import org.sonarsource.sonarlint.core.client.api.standalone.StandaloneSonarLintEngine;

import com.intellij.openapi.diagnostic.Logger;

import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;
import static org.sonarlint.intellij.common.util.SonarLintUtils.getService;
import static org.sonarlint.intellij.config.Settings.getGlobalSettings;

public class DefaultEngineManager implements EngineManager, Disposable {

    static final Logger log = Logger.getInstance(DefaultEngineManager.class);

    protected final Map<String, ConnectedSonarLintEngine> connectedEngines = new HashMap<>();
    private final SonarLintEngineFactory factory;
    protected StandaloneSonarLintEngine standalone;

    public DefaultEngineManager() {
        this(new SonarLintEngineFactory());
    }

    @NonInjectable
    DefaultEngineManager(SonarLintEngineFactory factory) {
        this.factory = factory;
    }

    private static void doInBackground(String title, Runnable r) {
        ProgressManager.getInstance()
            .run(
                new Task.Backgroundable(null, title, false, ALWAYS_BACKGROUND) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setText2(title);
                        r.run();
                    }
                }
            );
    }

    /**
     * Immediately removes and asynchronously stops all {@link ConnectedSonarLintEngine}
     * corresponding to server IDs that were removed.
     */
    @Override
    public synchronized void stopAllDeletedConnectedEnginesAsync() {
        var it = connectedEngines.entrySet().iterator();
        var configuredStorageIds = getServerNames();
        while (it.hasNext()) {
            var entry = it.next();
            if (!configuredStorageIds.contains(entry.getKey())) {
                doInBackground(
                    "Stop SonarLint engine '" + entry.getKey() + "'",
                    () -> entry.getValue().stop(false)
                );
                it.remove();
            }
        }
    }

    @Override
    public synchronized void stopAllEngines(boolean async) {
        AnalysisRequirementNotifications.resetCachedMessages();
        for (var entry : connectedEngines.entrySet()) {
            if (async) {
                doInBackground(
                    "Stop SonarLint engine '" + entry.getKey() + "'",
                    () -> entry.getValue().stop(false)
                );
            } else {
                entry.getValue().stop(false);
            }
        }
        connectedEngines.clear();
        if (standalone != null) {
            if (async) {
                var standaloneCopy = this.standalone;
                doInBackground(
                    "Stop standalone SonarLint engine",
                    standaloneCopy::stop
                );
            } else {
                standalone.stop();
            }
            standalone = null;
        }
    }

    @Override
    @NotNull
    public synchronized ConnectedSonarLintEngine getConnectedEngine(String connectionId) {
        return connectedEngines.computeIfAbsent(connectionId, this::createConnectedEngine);
    }

    @NotNull
    private ConnectedSonarLintEngine createConnectedEngine(String connectionId) {
        var configOpt = getGlobalSettings().getServerConnectionByName(connectionId);
        if (configOpt.isPresent()) {
            var config = configOpt.get();
            var engine = factory.createEngine(connectionId, config.isSonarCloud());
            getService(ServerEventsService.class).autoSubscribe(engine, config);
            return engine;
        }
        throw new IllegalStateException(
            "Unable to find a configured connection with id '" + connectionId + "'"
        );
    }

    @Override
    public synchronized StandaloneSonarLintEngine getStandaloneEngine() {
        if (standalone == null) {
            log.info("--- getStandaloneEngine");
            standalone = factory.createEngine();
        }
        return standalone;
    }

    @Override
    public ConnectedSonarLintEngine getConnectedEngine(
        SonarLintProjectNotifications notifications,
        String connectionId,
        String projectKey
    ) throws InvalidBindingException {
        Preconditions.checkNotNull(notifications, "notifications");
        Preconditions.checkNotNull(connectionId, "connectionId");
        Preconditions.checkNotNull(projectKey, "projectKey");
        var configuredStorageIds = getServerNames();
        if (!configuredStorageIds.contains(connectionId)) {
            notifications.notifyConnectionIdInvalid();
            throw new InvalidBindingException("Invalid server name: " + connectionId);
        }
        return getConnectedEngine(connectionId);
    }

    private static Set<String> getServerNames() {
        return getGlobalSettings().getServerConnections()
            .stream()
            .map(ServerConnection::getName)
            .collect(Collectors.toSet());
    }

    @Override
    public void dispose() {
        stopAllEngines(false);
    }

    @Override
    @Nullable
    public ConnectedSonarLintEngine getConnectedEngineIfStarted(String connectionId) {
        return connectedEngines.get(connectionId);
    }

    @Override
    @Nullable
    public SonarLintEngine getStandaloneEngineIfStarted() {
        return standalone;
    }
}
