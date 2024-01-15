

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.ServerResponsibility;
import jetbrains.buildServer.serverSide.healthStatus.*;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateHealthReport extends HealthStatusReport {

  @NotNull
  private static final String CATEGORY_ID = "queuePauserQueueState";

  @NotNull
  private static final String CATEGORY_NAME = "Build queue";

  @NotNull
  private final ItemCategory myCategory;

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final FreeSpaceQueuePauser myFreeSpaceQueuePauser;

  @NotNull
  private final ServerResponsibility myResponsibility;

  public QueueStateHealthReport(@NotNull final PluginDescriptor pluginDescriptor,
                                @NotNull final QueueStateManager queueStateManager,
                                @NotNull final PagePlaces pagePlaces,
                                @NotNull final FreeSpaceQueuePauser freeSpaceQueuePauser,
                                @NotNull final ServerResponsibility responsibility) {
    myQueueStateManager = queueStateManager;
    myFreeSpaceQueuePauser = freeSpaceQueuePauser;
    myResponsibility = responsibility;
    myCategory = new ItemCategory(CATEGORY_ID, CATEGORY_NAME, ItemSeverity.ERROR);
    final HealthStatusItemPageExtension myPEx = new HealthStatusItemPageExtension(CATEGORY_ID, pagePlaces) {
      @Override
      public boolean isAvailable(@NotNull final HttpServletRequest request) {
        return !myQueueStateManager.readQueueState().isQueueEnabled() && super.isAvailable(request);
      }
    };
    myPEx.setIncludeUrl(pluginDescriptor.getPluginResourcesPath("queueStateItemDisplay.jsp"));
    myPEx.setVisibleOutsideAdminArea(true);
    myPEx.register();
  }

  @NotNull
  @Override
  public String getType() {
    return CATEGORY_ID;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return "Build queue state report";
  }

  @NotNull
  @Override
  public Collection<ItemCategory> getCategories() {
    return Collections.singleton(myCategory);
  }

  @Override
  public boolean canReportItemsFor(@NotNull HealthStatusScope scope) {
    return scope.globalItems();
  }

  @Override
  public void report(@NotNull final HealthStatusScope scope, @NotNull final HealthStatusItemConsumer resultConsumer) {
    final QueueState queueState = myQueueStateManager.readQueueState();
    if (!queueState.isQueueEnabled()) {
      final Map<String, Object> myData = new HashMap<>();
      myData.put("QUEUE_STATE", queueState);
      myData.put("allowManualResume", queueState.getActor().equals(Actor.USER) || !myFreeSpaceQueuePauser.isAutoResumingEnabled());
      myData.put("serverAllowsResuming", myResponsibility.canProcessUserDataModificationRequests());
      resultConsumer.consumeGlobal(new HealthStatusItem(CATEGORY_ID, myCategory, myData));
    }
  }
}