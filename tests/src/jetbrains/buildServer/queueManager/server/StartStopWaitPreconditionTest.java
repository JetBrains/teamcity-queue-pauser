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

import jetbrains.buildServer.BaseJMockTestCase;
import jetbrains.buildServer.Mocked;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.buildDistribution.BuildDistributorInput;
import jetbrains.buildServer.serverSide.buildDistribution.QueuedBuildInfo;
import jetbrains.buildServer.serverSide.buildDistribution.WaitReason;
import jetbrains.buildServer.util.TestFor;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
@TestFor (testForClass = StartStopWaitPrecondition.class)
public class StartStopWaitPreconditionTest extends BaseJMockTestCase {

  private StartStopWaitPrecondition myWaitPrecondition;

  @Mocked
  private QueueStateManager myQueueStateManager;

  @Mocked
  private QueueState myState;

  @Mocked
  private QueuedBuildInfo canStart;

  @Mocked
  private BuildDistributorInput myBuildDistributorInput;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myWaitPrecondition = new StartStopWaitPrecondition(myQueueStateManager);
  }

  @Test
  public void testCanStart_Yes() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myQueueStateManager).readQueueState();
      will(returnValue(myState));

      oneOf(myState).isQueueEnabled();
      will(returnValue(true));
    }});

    final WaitReason result = myWaitPrecondition.canStart(canStart, Collections.emptyMap(), myBuildDistributorInput, false);
    assertNull(result);
  }

  @Test
  public void testCanStart_No() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myQueueStateManager).readQueueState();
      will(returnValue(myState));

      oneOf(myState).isQueueEnabled();
      will(returnValue(false));
    }});

    final WaitReason result = myWaitPrecondition.canStart(canStart, Collections.emptyMap(), myBuildDistributorInput, false);
    assertNotNull(result);

  }
}
