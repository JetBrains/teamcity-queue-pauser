/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.BaseJMockTestCase;
import jetbrains.buildServer.Mocked;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.TestFor;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
@TestFor (testForClass = {QueueStateManager.class, QueueStateManagerImpl.class})
public class QueueStateManagerImplTest extends BaseJMockTestCase {

  @Mocked
  private SettingsManager mySettingsManager;

  @Mocked
  private UserModel myUserModel;

  @Mocked
  private SUser myUser;

  private QueueStateManager myQueueStateManager;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myQueueStateManager = new QueueStateManagerImpl(mySettingsManager, myUserModel);
  }

  @Test
  public void testReadQueueState_NoUser() {
    final boolean expectedQueueState = true;
    final Date expectedDate = Dates.today();
    final String expectedReason  = "Some expected reason";
    final long userId = 0L;
    final Actor expectedActor = Actor.USER;

    m.checking(new Expectations() {{
      oneOf(mySettingsManager).isQueueEnabled();
      will(returnValue(expectedQueueState));

      oneOf(mySettingsManager).getQueueStateChangedBy();
      will(returnValue(userId));

      oneOf(myUserModel).findUserById(userId);
      will(returnValue(null));

      oneOf(mySettingsManager).getQueueStateChangedOn();
      will(returnValue(expectedDate));

      oneOf(mySettingsManager).getQueueStateChangedReason();
      will(returnValue(expectedReason));

      oneOf(mySettingsManager).getQueueStateChangedActor();
      will(returnValue(expectedActor));

    }});

    final QueueState queueState = myQueueStateManager.readQueueState();
    assertEquals(expectedQueueState, queueState.isQueueEnabled());
    assertNull(queueState.getUser());
    assertEquals(expectedDate, queueState.getTimestamp());
    assertEquals(expectedReason, queueState.getReason());
  }

  @Test
  public void testReadQueueState_User() {
    final boolean expectedQueueState = false;
    final Date expectedDate = Dates.today();
    final String expectedReason  = "Some expected reason";
    final long userId = 0L;
    final Actor expectedActor = Actor.USER;

    addSingleReadExpectations(expectedQueueState, expectedDate, expectedReason, userId, expectedActor);

    final QueueState queueState = myQueueStateManager.readQueueState();
    assertEquals(expectedQueueState, queueState.isQueueEnabled());
    assertNotNull(queueState.getUser());
    assertEquals(myUser, queueState.getUser());
    assertEquals(expectedDate, queueState.getTimestamp());
    assertEquals(expectedReason, queueState.getReason());
  }

  @Test
  public void testWriteQueueState() {
    final boolean newQueueState = true;
    final Date newDate = Dates.yesterday();
    final String newReason  = "Some new reason";
    final long userId = 12345L;

    final QueueState stateToWrite = new QueueStateImpl(newQueueState, myUser, newReason, newDate, Actor.USER);

    m.checking(new Expectations() {{
      oneOf(mySettingsManager).setQueueEnabled(newQueueState);

      oneOf(myUser).getId();
      will(returnValue(userId));

      oneOf(mySettingsManager).setQueueStateChangedBy(userId);
      oneOf(mySettingsManager).setQueueStateChangedReason(newReason);
      oneOf(mySettingsManager).setQueueStateChangedOn(newDate);
      oneOf(mySettingsManager).setQueueStateChangedActor(Actor.USER);
    }});

    myQueueStateManager.writeQueueState(stateToWrite);
  }

  @Test
  @TestFor (issues = "TW-10787")
  public void testWriteQueueState_NullUser() {
    final boolean newQueueState = true;
    final Date newDate = Dates.yesterday();
    final String newReason = "Some new reason";

    final QueueState  stateToWrite = new QueueStateImpl(newQueueState, null, newReason, newDate, Actor.USER);

    m.checking(new Expectations() {{
      oneOf(mySettingsManager).setQueueEnabled(newQueueState);
      oneOf(mySettingsManager).setQueueStateChangedBy(null);
      oneOf(mySettingsManager).setQueueStateChangedReason(newReason);
      oneOf(mySettingsManager).setQueueStateChangedOn(newDate);
      oneOf(mySettingsManager).setQueueStateChangedActor(Actor.USER);
    }});

    myQueueStateManager.writeQueueState(stateToWrite);
  }

  @Test
  @TestFor(issues = "TW-51387")
  public void testResolveUserOnlyOnce() {
    final boolean expectedQueueState = false;
    final Date expectedDate = Dates.today();
    final String expectedReason  = "Some expected reason";
    final long userId = 0L;
    final Actor expectedActor = Actor.USER;

    m.checking(new Expectations() {{
      oneOf(mySettingsManager).isQueueEnabled();
      will(returnValue(expectedQueueState));

      oneOf(mySettingsManager).getQueueStateChangedBy();
      will(returnValue(userId));

      oneOf(myUserModel).findUserById(userId);
      will(returnValue(myUser));

      exactly(2).of(mySettingsManager).getQueueStateChangedOn();
      will(returnValue(expectedDate));

      oneOf(mySettingsManager).getQueueStateChangedReason();
      will(returnValue(expectedReason));

      oneOf(mySettingsManager).getQueueStateChangedActor();
      will(returnValue(expectedActor));

    }});

    myQueueStateManager.readQueueState();
    // readQueueState does not produce second call to user model
    myQueueStateManager.readQueueState();
  }

  @Test
  @TestFor(issues = "TW-51387")
  public void testResolveUserAfterStateChange() {
    final boolean expectedQueueState = true;
    final Date expectedDate = Dates.yesterday();
    final String expectedReason  = "Some expected reason";
    final long userId = 0L;
    final Actor expectedActor = Actor.USER;


    addSingleReadExpectations(expectedQueueState, expectedDate, expectedReason, userId, expectedActor);
    
    final boolean newQueueState = false;
    final Date newDate = Dates.today();
    final String newReason  = "Some new reason";
    final long newUserId = 12345L;

    final QueueState stateToWrite = new QueueStateImpl(newQueueState, myUser, newReason, newDate, Actor.USER);

    myQueueStateManager.readQueueState();

    // expectation that second call of readQueueState only produces timestamp read
    m.checking(new Expectations() {{
      oneOf(mySettingsManager).getQueueStateChangedOn();
      will(returnValue(expectedDate));
    }});

    myQueueStateManager.readQueueState();

    m.checking(new Expectations() {{
      oneOf(myUser).getId();
      will(returnValue(newUserId));
      
      oneOf(mySettingsManager).setQueueEnabled(newQueueState);
      oneOf(mySettingsManager).setQueueStateChangedBy(newUserId);
      oneOf(mySettingsManager).setQueueStateChangedReason(newReason);
      oneOf(mySettingsManager).setQueueStateChangedOn(newDate);
      oneOf(mySettingsManager).setQueueStateChangedActor(Actor.USER);
    }});

    myQueueStateManager.writeQueueState(stateToWrite);

    addSingleReadExpectations(expectedQueueState, expectedDate, expectedReason, newUserId, expectedActor);
    myQueueStateManager.readQueueState();
  }

  private void addSingleReadExpectations(boolean expectedQueueState,
                                         final Date expectedDate,
                                         final String expectedReason,
                                         long userId,
                                         final Actor expectedActor) {
    m.checking(new Expectations() {{
      oneOf(mySettingsManager).isQueueEnabled();
      will(returnValue(expectedQueueState));

      oneOf(mySettingsManager).getQueueStateChangedBy();
      will(returnValue(userId));

      oneOf(myUserModel).findUserById(userId);
      will(returnValue(myUser));

      oneOf(mySettingsManager).getQueueStateChangedOn();
      will(returnValue(expectedDate));

      oneOf(mySettingsManager).getQueueStateChangedReason();
      will(returnValue(expectedReason));

      oneOf(mySettingsManager).getQueueStateChangedActor();
      will(returnValue(expectedActor));
    }});
  }
}
