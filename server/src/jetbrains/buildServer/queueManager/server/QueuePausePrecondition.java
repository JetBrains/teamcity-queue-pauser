

package jetbrains.buildServer.queueManager.server;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import jetbrains.buildServer.serverSide.impl.buildDistribution.BuildDistributorInputEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class QueuePausePrecondition implements StartBuildPrecondition {
  private static final String PROPERTY_WAIT_REASON = "queuePauser.waitReason";
  private static final String PROPERTY_QUEUE_STATE = "queuePauser.queueState";
  private static final String PROPERTY_CHECKED = "queuePauser.checked";

  private static final Logger LOG = Logger.getInstance(QueuePausePrecondition.class.getName());

  @NotNull
  private final QueueStateManager myQueueStateManager;

  public QueuePausePrecondition(@NotNull final QueueStateManager queueStateManager) {
    myQueueStateManager = queueStateManager;
  }

  @Nullable
  @Override
  public WaitReason canStart(@NotNull QueuedBuildInfo queuedBuild,
                             @NotNull Map<QueuedBuildInfo, BuildAgent> canBeStarted,
                             @NotNull BuildDistributorInput buildDistributorInput,
                             boolean emulationMode) {
    final BuildDistributorInputEx input = (BuildDistributorInputEx) buildDistributorInput;
    final WaitReason cachedWaitReason = input.getCustomData(PROPERTY_WAIT_REASON, WaitReason.class);
    if (cachedWaitReason != null) {
      return cachedWaitReason;
    }
    // check that we have already checked this precondition this cycle
    if (input.getCustomData(PROPERTY_CHECKED, Boolean.class) != null) {
      return null;
    }
    // no wait reason, haven't checked within this cycle
    QueueState state;
    final QueueState cachedState = input.getCustomData(PROPERTY_QUEUE_STATE, QueueState.class);
    if (cachedState != null) {
      state = cachedState;
    } else {
      state = myQueueStateManager.readQueueState();
    }

    if (!state.isQueueEnabled()) {
      WaitReason reason = new SimpleWaitReason("Build queue was paused");
      if (LOG.isDebugEnabled()) {
        LOG.debug("Build queue was paused. Returning wait reason [" + reason.getDescription() + "]");
      }
      // cache wait reason in the distribution context,
      // so all other builds in the distribution cycle will receive it
      input.setCustomData(PROPERTY_WAIT_REASON, reason);
      return reason;
    } else {
      input.setCustomData(PROPERTY_CHECKED, Boolean.TRUE);
    }
    return null;
  }
}