

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.controllers.BaseActionController;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static jetbrains.buildServer.queueManager.PluginConstants.WEB.QUEUE_ACTIONS_URL;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateController extends BaseActionController {

  public QueueStateController(@NotNull final WebControllerManager manager) {
    super(manager);
    manager.registerController(QUEUE_ACTIONS_URL, this);
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
                                  @NotNull final HttpServletResponse response) throws Exception {
    doAction(request, response, null);
    return null;
  }
}