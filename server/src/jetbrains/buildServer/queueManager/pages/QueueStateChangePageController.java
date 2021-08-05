/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.queueManager.PluginConstants;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.*;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class {@code QueueStateChangePageController}
 *
 * Implements page extension that allows to change queue state
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateChangePageController extends BaseController {

  @NotNull
  private static final String EXTENSION_INCLUDE_URL = "/queueStateChange.html";

  @NotNull
  private static final String EXTENSION_AVAILABILITY_URL = "/queue.html";

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final ServerResponsibility myResponsibility;

  @NotNull
  private final SecurityContext mySecurityContext;

  @NotNull
  private final PluginDescriptor myPluginDescriptor;

  public QueueStateChangePageController(@NotNull final PagePlaces pagePlaces,
                                        @NotNull final PluginDescriptor descriptor,
                                        @NotNull final SecurityContext securityContext,
                                        @NotNull final QueueStateManager queueStateManager,
                                        @NotNull final ServerResponsibility responsibility,
                                        @NotNull final WebControllerManager controllerManager) {
    mySecurityContext = securityContext;
    myQueueStateManager = queueStateManager;
    myResponsibility = responsibility;
    myPluginDescriptor = descriptor;

    final SimplePageExtension classicPageExtension = new SimplePageExtension(pagePlaces) {
      @Override
      public boolean isAvailable(@NotNull final HttpServletRequest request) {
        return isEnoughPermissions()
                && WebUtil.getPathWithoutAuthenticationType(WebUtil.getPathWithoutContext(request, WebUtil.getOriginalRequestUrl(request))).startsWith(EXTENSION_AVAILABILITY_URL);
      }
    };
    classicPageExtension.setPlaceId(PlaceId.BEFORE_CONTENT);
    classicPageExtension.setPluginName(descriptor.getPluginName());
    classicPageExtension.setIncludeUrl(EXTENSION_INCLUDE_URL);
    classicPageExtension.register();

    final SimplePageExtension sakuraPageExtension = new SimplePageExtension(pagePlaces) {
      @Override
      public boolean isAvailable(@NotNull final HttpServletRequest request) {
        return isEnoughPermissions();
      }
    };
    sakuraPageExtension.setPlaceId(new PlaceId("SAKURA_QUEUE_ACTIONS"));
    sakuraPageExtension.setPluginName(descriptor.getPluginName());
    sakuraPageExtension.setIncludeUrl(EXTENSION_INCLUDE_URL);
    sakuraPageExtension.register();

    controllerManager.registerController(EXTENSION_INCLUDE_URL, this);
  }

  private boolean isEnoughPermissions() {
    final SUser user = (SUser) mySecurityContext.getAuthorityHolder().getAssociatedUser();
    return myResponsibility.canProcessUserDataModificationRequests()
            && user != null
            && user.isPermissionGrantedGlobally(Permission.ENABLE_DISABLE_AGENT);
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    final ModelAndView mv = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("queuePage.jsp"));
    mv.getModel().put(PluginConstants.WEB.PARAM_QUEUE_STATE, myQueueStateManager.readQueueState());
    return mv;
  }
}



