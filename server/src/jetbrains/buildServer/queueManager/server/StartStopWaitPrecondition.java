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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class StartStopWaitPrecondition implements StartBuildPrecondition {

  private static final Logger LOG = Logger.getInstance(StartStopWaitPrecondition.class.getName());

  @NotNull
  private final QueueStateManager myQueueStateManager;

  public StartStopWaitPrecondition(@NotNull QueueStateManager queueStateManager) {
    myQueueStateManager = queueStateManager;
  }

  @Nullable
  public WaitReason canStart(@NotNull QueuedBuildInfo queuedBuild,
                             @NotNull Map<QueuedBuildInfo, BuildAgent> canBeStarted,
                             @NotNull BuildDistributorInput buildDistributorInput, boolean emulationMode) {
    WaitReason result = null;
    final QueueState queueState = myQueueStateManager.readQueueState();
    if (!queueState.isQueueEnabled()) {
      result =  new SimpleWaitReason("Queue is disabled"); // todo: pretty message in wait reason
      if (LOG.isDebugEnabled()) {
        LOG.debug("Queue disabled. Returning wait reason [" + result.getDescription() + "]");
      }
    }
    return result;
  }
}
