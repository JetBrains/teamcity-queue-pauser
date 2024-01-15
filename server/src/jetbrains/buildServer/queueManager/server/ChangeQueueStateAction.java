

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.serverSide.audit.AuditLogFactory;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
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
import static jetbrains.buildServer.serverSide.audit.ActionType.BUILD_QUEUE_PAUSED;
import static jetbrains.buildServer.serverSide.audit.ActionType.BUILD_QUEUE_RESUMED;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public final class ChangeQueueStateAction implements ControllerAction {

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final SecurityContext mySecurityContext;

  @NotNull
  private final AuditLogFactory myLogFactory;

  @NotNull
  private final ServerResponsibility myResponsibility;

  public ChangeQueueStateAction(@NotNull final QueueStateManager queueStateManager,
                                @NotNull final QueueStateController queueStateController,
                                @NotNull final SecurityContext securityContext,
                                @NotNull final AuditLogFactory logFactory,
                                @NotNull final ServerResponsibility responsibility) {
    myQueueStateManager = queueStateManager;
    mySecurityContext = securityContext;
    myLogFactory = logFactory;
    myResponsibility = responsibility;
    queueStateController.registerAction(this);
  }

  public boolean canProcess(@NotNull final HttpServletRequest request) {
    final SUser user = getUser();
    return  myResponsibility.canProcessUserDataModificationRequests()
            && user != null
            && user.isPermissionGrantedGlobally(Permission.ENABLE_DISABLE_AGENT)
            && request.getParameter(PARAM_NEW_QUEUE_STATE) != null
            && request.getParameter(PARAM_STATE_CHANGE_REASON) != null;
  }

  public void process(@NotNull final HttpServletRequest request,
                      @NotNull final HttpServletResponse response,
                      @Nullable final Element ajaxResponse) {
    boolean newQueueState = PropertiesUtil.getBoolean(request.getParameter(PARAM_NEW_QUEUE_STATE));
    final String comment = request.getParameter(PARAM_STATE_CHANGE_REASON);
    final SUser user = getUser();
    final Date date = new Date();
    final QueueState state = new QueueState(newQueueState, user, comment, date, Actor.USER);
    changeStateAndLog(state, request);
  }

  /**
   * Gets current user from security context
   * @return current user
   */
  @Nullable
  private SUser getUser() {
    return (SUser) mySecurityContext.getAuthorityHolder().getAssociatedUser();
  }

  /**
   * Processes new queue state. Generates log message based on state description
   * @param state new queue state
   * @param request http request, that changed queue state
   */
  private void changeStateAndLog(@NotNull final QueueState state, @NotNull final HttpServletRequest request) {
    myQueueStateManager.writeQueueState(state);
    myLogFactory.createForServer().logUserAction(
            state.isQueueEnabled() ? BUILD_QUEUE_RESUMED: BUILD_QUEUE_PAUSED,
            state.getReason(), null);
    Loggers.SERVER.warn(describeState(state, request));
  }

  /**
   * Generates message to be written to server log
   * @param state queue state
   * @param request http request, that changed queue state
   * @return message with state change description
   */
  @NotNull
  private String describeState(@NotNull final QueueState state, @NotNull final HttpServletRequest request) {
    final StringBuilder builder = new StringBuilder();
    builder.append("Queue was ").append(state.isQueueEnabled() ? "resumed" : "paused");
    if (state.getUser() != null) {
      builder.append(" by ").append(state.getUser().getDescriptiveName());
    }
    builder.append(" on ");
    builder.append(Dates.formatDate(state.getTimestamp(), "dd MMM yyyy", SessionUser.getUserTimeZone(request)));
    if (!"".equals(state.getReason())) {
      builder.append(" with comment: ").append(state.getReason());
    }
    return builder.toString();
  }
}