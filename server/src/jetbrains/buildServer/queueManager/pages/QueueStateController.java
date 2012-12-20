package jetbrains.buildServer.queueManager.pages;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.controllers.BaseActionController;
import jetbrains.buildServer.queueManager.settings.SettingsManager;
import jetbrains.buildServer.serverSide.CriticalErrors;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static jetbrains.buildServer.queueManager.PluginConstants.WEB.ERROR_QUEUE_IS_DISABLED;
import static jetbrains.buildServer.queueManager.PluginConstants.WEB.PARAM_NEW_QUEUE_STATE;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateController extends BaseActionController {

  private static final String MESSAGE_FORMAT = "Build Queue is disabled by %s on %s";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);

  @NotNull
  private final SettingsManager mySettingsManager;

  private final CriticalErrors myCriticalErrors;

  public QueueStateController(@NotNull final WebControllerManager manager,
                              @NotNull final SettingsManager settingsManager,
                              @NotNull final CriticalErrors criticalErrors) {
    super(manager);
    mySettingsManager = settingsManager;
    myCriticalErrors = criticalErrors;
    manager.registerController("/queueManager.html", this);
    init();
  }

  private void init() {
    // process current queue state
    processState(mySettingsManager.getQueueState(), mySettingsManager.getQueueStateSwitchedBy(), mySettingsManager.getQueueStateSwitchedOn());
    // register controller for managing it
    registerAction(new ControllerAction() {
      public boolean canProcess(@NotNull final HttpServletRequest request) {
        return request.getParameter(PARAM_NEW_QUEUE_STATE) != null;
      }
      public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response, @Nullable final Element ajaxResponse) {
        boolean newQueueState = PropertiesUtil.getBoolean(request.getParameter(PARAM_NEW_QUEUE_STATE));
        final SUser user = SessionUser.getUser(request); // todo: can it be null?
        final String userName = user.getName();
        final Date date = new Date();
        processState(newQueueState, userName, date);
        mySettingsManager.setQueueState(newQueueState);
        mySettingsManager.setQueueStateSwitchedBy(userName);
        mySettingsManager.setQueueStateSwitchedOn(date);
      }
    });
  }

  /**
   * Processes given state of build queue. Puts disabled state
   * in critical error
   * @param state state of build queue
   */
  private void processState(boolean state, String userName, Date date) {
    if (state) {
      myCriticalErrors.clearError(ERROR_QUEUE_IS_DISABLED);
    } else {
      myCriticalErrors.putError(ERROR_QUEUE_IS_DISABLED,
              String.format(MESSAGE_FORMAT, userName, DATE_FORMAT.format(date)));
    }
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
