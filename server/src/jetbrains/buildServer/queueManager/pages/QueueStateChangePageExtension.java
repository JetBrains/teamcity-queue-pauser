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
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.auth.Permission;
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
 * Class {@code QueueStateChangePageExtension}
 *
 * Implements page extension that allows to change queue state
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateChangePageExtension extends SimplePageExtension {

  @NotNull
  private static final String EXTENSION_INCLUDE_URL = "queuePage.jsp";

  @NotNull
  private static final String EXTENSION_AVAILABILITY_URL = "/queue.html";

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final SecurityContext mySecurityContext;

  public QueueStateChangePageExtension(@NotNull final PagePlaces pagePlaces,
                                       @NotNull final PluginDescriptor descriptor,
                                       @NotNull final SecurityContext securityContext,
                                       @NotNull final QueueStateManager queueStateManager) {
    super(pagePlaces);
    mySecurityContext = securityContext;
    myQueueStateManager = queueStateManager;
    setPlaceId(PlaceId.BEFORE_CONTENT);
    setPluginName(descriptor.getPluginName());
    setIncludeUrl(descriptor.getPluginResourcesPath(EXTENSION_INCLUDE_URL));
  }

  @Override
  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    final SUser user = (SUser) mySecurityContext.getAuthorityHolder().getAssociatedUser();
    String uri = (String) request.getAttribute("javax.servlet.forward.request_uri");
    return WebUtil.getPathWithoutAuthenticationType(WebUtil.getPathWithoutContext(request, uri)).startsWith(EXTENSION_AVAILABILITY_URL)
            && user != null
            && user.isPermissionGrantedGlobally(Permission.ENABLE_DISABLE_AGENT);
  }

  @Override
  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    super.fillModel(model, request);
    model.put(PluginConstants.WEB.PARAM_QUEUE_STATE, myQueueStateManager.readQueueState());
  }
}



