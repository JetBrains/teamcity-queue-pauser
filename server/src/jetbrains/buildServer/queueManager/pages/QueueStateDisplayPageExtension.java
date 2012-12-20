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
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateDisplayPageExtension extends SimplePageExtension {

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final SecurityContext mySecurityContext;

  public QueueStateDisplayPageExtension(@NotNull PagePlaces pagePlaces,
                                        @NotNull PluginDescriptor descriptor,
                                        @NotNull SecurityContext securityContext,
                                        @NotNull QueueStateManager queueStateManager) {
    super(pagePlaces);
    myQueueStateManager = queueStateManager;
    mySecurityContext = securityContext;
    setPlaceId(PlaceId.BEFORE_CONTENT);
    setPluginName(descriptor.getPluginName());
    setIncludeUrl(descriptor.getPluginResourcesPath("queueStateDisplay.jsp"));
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    final SUser user = (SUser) mySecurityContext.getAuthorityHolder().getAssociatedUser();
    return user != null;
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    super.fillModel(model, request);
    model.put(PluginConstants.WEB.PARAM_QUEUE_STATE, myQueueStateManager.readQueueState());
  }
}