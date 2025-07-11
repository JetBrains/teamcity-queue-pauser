

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.TestFor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Oleg Rybak <oleg.rybak@jetbrains.com>
 */
@TestFor(testForClass = MessageViewer.class)
public class MessageViewerTest extends BaseTestCase {

  private Mockery m;

  private SUser myUser;

  private QueueState myQueueState;

  @BeforeMethod
  @Override
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
    }};
    myUser = m.mock(SUser.class);
    myQueueState = m.mock(QueueState.class);
  }


  @Test
  @TestFor(issues = "TW-33277")
  public void testFreeSpaceReasonForAdmin() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myUser).isSystemAdministratorRoleGranted();
      will(returnValue(true));

      atMost(2).of(myQueueState).getReason();
      will(returnValue("message for admins"));
    }});
    final String result = MessageViewer.viewMessage(myUser, myQueueState);
    assertEquals(myQueueState.getReason(), result);
  }

  @Test
  @TestFor(issues = "TW-33277")
  public void testFreeSpaceReasonForNonAdmin() throws Exception {
    m.checking(new Expectations() {{
      oneOf(myUser).isSystemAdministratorRoleGranted();
      will(returnValue(false));

      oneOf(myQueueState).getActor();
      will(returnValue(Actor.FREE_SPACE_QUEUE_PAUSER));
    }});
    final String result = MessageViewer.viewMessage(myUser, myQueueState);
    assertEquals(FreeSpaceQueuePauser.DEFAULT_REASON, result);
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }
}