/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.queueManager;

import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.BuildEstimates;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.buildDistribution.WaitReason;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.serverSide.impl.timeEstimation.CachingBuildEstimator;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.WaitForAssert;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Objects;

public class QueuePauserIntegrationTest extends BaseServerTestCase {

  private QueueStateManager myQueueStateManager;

  @BeforeMethod(alwaysRun = true)
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    QueuePauserTestSupport.apply(myFixture);
    myQueueStateManager = myFixture.getSingletonService(QueueStateManager.class);
  }

  @Test
  public void testPauseUnpause() {
    final SUser admin = createAdmin("my_admin");
    QueueState stateToWrite = new QueueState(false, admin, "test", new Date(), Actor.USER);
    myQueueStateManager.writeQueueState(stateToWrite);

    SQueuedBuild queuedBuild = myBuildType.addToQueue("");
    assertNotNull(queuedBuild);

    waitForReason(queuedBuild, "Build queue was paused");

    stateToWrite = new QueueState(true, admin, "test", new Date(), Actor.USER);
    myQueueStateManager.writeQueueState(stateToWrite);

    myFixture.flushQueueAndWait();
  }


  @SuppressWarnings("SameParameterValue")
  protected void waitForReason(@NotNull final SQueuedBuild queuedBuild, @NotNull final String expectedReason) {
    final CachingBuildEstimator estimator = myFixture.getSingletonService(CachingBuildEstimator.class);

    new WaitForAssert() {

      private String myReportedReason = null;

      @Override
      protected boolean condition() {
        estimator.invalidate(false);
        final BuildEstimates buildEstimates = queuedBuild.getBuildEstimates();
        if (buildEstimates != null) {
          final WaitReason waitReason = buildEstimates.getWaitReason();
          if (waitReason != null) {
            myReportedReason = waitReason.getDescription();
          }
          return Objects.equals(myReportedReason, expectedReason);
        }
        return false;
      }

      @Override
      protected String getAssertMessage() {
        return "Expected wait reason [" + expectedReason + "], last reported: [" + myReportedReason + "]";
      }
    };
  }
}
