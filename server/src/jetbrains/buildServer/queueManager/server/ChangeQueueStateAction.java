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

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.util.SessionUser;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public final class ChangeQueueStateAction implements ControllerAction {

  private final QueueStateManager myQueueStateManager;

  public ChangeQueueStateAction(@NotNull final QueueStateManager queueStateManager,
                                @NotNull final QueueStateController queueStateController) {
    myQueueStateManager = queueStateManager;
    queueStateController.registerAction(this);
  }


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
    processState(state, request);
  }

  /**
   * Processes new queue state. Generates log message based on state description
   * @param state new queue state
   * @param request http request, that changed queue state
   */
  private void processState(@NotNull QueueState state, @NotNull HttpServletRequest request) {
    Loggers.SERVER.warn(describeStateForSessionUser(state, request));
  }

  /**
   * Generates message to be written to server log
   * @param state queue state
   * @param request http request, that changed queue state
   * @return message with state change description
   */
  private String describeStateForSessionUser(@NotNull QueueState state, @NotNull HttpServletRequest request) {
    final StringBuilder builder = new StringBuilder();
    builder.append("Queue was ").append(state.isQueueEnabled() ? "enabled" : "disabled");
    if (state.getUser() != null) {
      builder.append(" by ").append(state.getUser().getDescriptiveName());
    }
    builder.append(" on ");
    builder.append(Dates.formatDate(state.getTimestamp(), "dd'&nbsp;'MMM'&nbsp;'yy", SessionUser.getUserTimeZone(request)));
    builder.append(" with comment: ").append(state.getReason());
    return builder.toString();
  }
}
