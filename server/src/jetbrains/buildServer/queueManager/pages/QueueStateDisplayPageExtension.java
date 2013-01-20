package jetbrains.buildServer.queueManager.pages;

import jetbrains.buildServer.queueManager.PluginConstants;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Class {@code QueueStateDisplayPageExtension}
 *
 * Implements page extension that displays current queue state
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateDisplayPageExtension extends SimplePageExtension {

  @NotNull
  private static final String EXTENSION_INCLUDE_URL = "queueStateDisplay.jsp";

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final SecurityContext mySecurityContext;

  public QueueStateDisplayPageExtension(@NotNull final PagePlaces pagePlaces,
                                        @NotNull final PluginDescriptor descriptor,
                                        @NotNull final SecurityContext securityContext,
                                        @NotNull final QueueStateManager queueStateManager) {
    super(pagePlaces);
    myQueueStateManager = queueStateManager;
    mySecurityContext = securityContext;
    setPlaceId(PlaceId.BEFORE_CONTENT);
    setPluginName(descriptor.getPluginName());
    setIncludeUrl(descriptor.getPluginResourcesPath(EXTENSION_INCLUDE_URL));
  }

  @Override
  public boolean isAvailable(@NotNull final HttpServletRequest request) {
    final SUser user = (SUser) mySecurityContext.getAuthorityHolder().getAssociatedUser();
    return user != null;
  }

  @Override
  public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
    super.fillModel(model, request);
    model.put(PluginConstants.WEB.PARAM_QUEUE_STATE, myQueueStateManager.readQueueState());
  }
}