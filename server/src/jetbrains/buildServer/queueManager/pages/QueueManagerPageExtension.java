/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.queueManager.pages;

import jetbrains.buildServer.queueManager.PluginConstants;
import jetbrains.buildServer.queueManager.settings.SettingsManager;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueManagerPageExtension extends SimplePageExtension {

  @NotNull
  private final SettingsManager mySettingsManager;

  @NotNull
  private final SecurityContext mySecurityContext;

  public QueueManagerPageExtension(@NotNull PagePlaces pagePlaces,
                                   @NotNull PluginDescriptor descriptor,
                                   @NotNull SecurityContext securityContext,
                                   @NotNull SettingsManager settingsManager
  ) {
    super(pagePlaces);
    mySecurityContext = securityContext;
    mySettingsManager = settingsManager;
    setPlaceId(PlaceId.ALL_PAGES_FOOTER);
    setPluginName(descriptor.getPluginName());
    setIncludeUrl(descriptor.getPluginResourcesPath("queuePage.jsp"));
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    final SUser user = (SUser) mySecurityContext.getAuthorityHolder().getAssociatedUser();
    return WebUtil.getPathWithoutAuthenticationType(request).startsWith("/queue.html")
            && user != null
            && user.isSystemAdministratorRoleGranted();
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    super.fillModel(model, request);
    model.put(PluginConstants.WEB.PARAM_QUEUE_STATE, mySettingsManager.getQueueState());
  }
}



