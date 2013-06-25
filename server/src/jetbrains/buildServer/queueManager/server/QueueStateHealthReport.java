/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.healthStatus.*;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateHealthReport extends HealthStatusReport {

  @NotNull
  private static final String CATEGORY_ID = "queuePauserQueueState";

  @NotNull
  private static final String CATEGORY_NAME = "Build queue";


  @NotNull
  private final ItemCategory myCategory;


  @NotNull
  private final QueueStateManager myQueueStateManager;


  public QueueStateHealthReport(@NotNull PluginDescriptor pluginDescriptor,
                                @NotNull final QueueStateManager queueStateManager,
                                @NotNull final PagePlaces pagePlaces) {
    myQueueStateManager = queueStateManager;
    myCategory = new ItemCategory(CATEGORY_ID, CATEGORY_NAME, ItemSeverity.WARN);
    final HealthStatusItemPageExtension myPEx = new HealthStatusItemPageExtension(CATEGORY_ID, pagePlaces);
    myPEx.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("queueStateItemDisplay.jsp"));
    myPEx.addJsFile(pluginDescriptor.getPluginResourcesPath("/js/QueueStateActions.js"));
    myPEx.setVisibleOutsideAdminArea(true);
    myPEx.register();
  }

  @NotNull
  @Override
  public String getType() {
    return CATEGORY_ID;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Queue state display name";
  }

  @NotNull
  @Override
  public Collection<ItemCategory> getCategories() {
    return Collections.singleton(myCategory);
  }

  @Override
  public void report(@NotNull final HealthStatusScope scope, @NotNull final HealthStatusItemConsumer resultConsumer) {
    final QueueState queueState = myQueueStateManager.readQueueState();
    if (!queueState.isQueueEnabled()) {
      final Map<String, Object> myData = new HashMap<String, Object>();
      myData.put("QUEUE_STATE", queueState);
      resultConsumer.consumeGlobal(new HealthStatusItem(CATEGORY_ID, myCategory, myData));
    }
  }
}
