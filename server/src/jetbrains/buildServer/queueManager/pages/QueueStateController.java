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

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.controllers.BaseActionController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static jetbrains.buildServer.queueManager.PluginConstants.WEB.PARAM_NEW_QUEUE_STATE;
import static jetbrains.buildServer.queueManager.PluginConstants.WEB.PARAM_STATE_CHANGE_REASON;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateController extends BaseActionController {

  private final QueueStateManager myQueueStateManager;

  public QueueStateController(@NotNull final WebControllerManager manager,
                              @NotNull final QueueStateManager queueStateManager) {
    super(manager);
    myQueueStateManager = queueStateManager;
    manager.registerController("/queueManager.html", this);
    init();
  }

  private void init() {
    // process current queue state
    processState(myQueueStateManager.readQueueState());
    // register controller for managing it
    registerAction(new ControllerAction() {
      public boolean canProcess(@NotNull final HttpServletRequest request) {
        return request.getParameter(PARAM_NEW_QUEUE_STATE) != null &&
                request.getParameter(PARAM_STATE_CHANGE_REASON) != null;
      }
      public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response, @Nullable final Element ajaxResponse) {
        boolean newQueueState = PropertiesUtil.getBoolean(request.getParameter(PARAM_NEW_QUEUE_STATE));
        final String comment = request.getParameter(PARAM_STATE_CHANGE_REASON);
        final SUser user = SessionUser.getUser(request);
        final Date date = new Date();
        final QueueState state = new QueueState(newQueueState, user, comment, date);
        myQueueStateManager.writeQueueState(state);
        processState(state);
      }
    });
  }

  private void processState(QueueState state) {
    Loggers.SERVER.warn(state.describe());
  }



  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
    doAction(request, response, null);
    ModelAndView result = null;
    String redirectTo = request.getParameter("redirectTo");
    if (StringUtil.isEmpty(redirectTo)) {
      redirectTo = request.getHeader("Referer");
    }
    if (redirectTo != null) {
      result = new ModelAndView(new RedirectView(redirectTo));
    }
    return result;
  }
}
