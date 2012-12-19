package jetbrains.buildServer.queueManager.pages;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.controllers.BaseActionController;
import jetbrains.buildServer.queueManager.settings.SettingsManager;
import jetbrains.buildServer.serverSide.CriticalErrors;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateController extends BaseActionController {

  @NotNull
  private final SettingsManager mySettingsManager;

  @NotNull
  private static final String PARAM_NEW_QUEUE_STATE = "newQueueState";

  @NonNls
  public static final String QUEUE_IS_PAUSED = "buildQueueIsPaused";

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
    registerAction(new ControllerAction() {
      public boolean canProcess(@NotNull final HttpServletRequest request) {
        return request.getParameter(PARAM_NEW_QUEUE_STATE) != null;
      }
      public void process(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response, @Nullable final Element ajaxResponse) {
        boolean newQueueState = PropertiesUtil.getBoolean(request.getParameter(PARAM_NEW_QUEUE_STATE));
        mySettingsManager.setQueueState(newQueueState);
        if (newQueueState) {
          myCriticalErrors.clearError(QUEUE_IS_PAUSED);
        } else {
          myCriticalErrors.putError(QUEUE_IS_PAUSED, "Build Queue is paused");
        }
      }
    });
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
    doAction(request, response, null);
    String redirectTo = request.getParameter("redirectTo");
    if (StringUtil.isEmpty(redirectTo)) {
      redirectTo = request.getHeader("Referer");
      if (redirectTo == null) return null;
    }
    return new ModelAndView(new RedirectView(redirectTo));
  }
}
