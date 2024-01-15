

package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueState {

  private final boolean myQueueEnabled;

  @Nullable
  private final SUser myUser;

  @NotNull
  private final String myReason;

  @NotNull
  private final Date myTimestamp;

  @NotNull
  private final Actor myActor;

  public QueueState(boolean enabled,
                    @Nullable SUser user,
                    @NotNull String reason,
                    @NotNull Date timestamp,
                    @NotNull final Actor actor) {
    myQueueEnabled = enabled;
    myUser = user;
    myReason = reason;
    myTimestamp = timestamp;
    myActor = actor;
  }

  public boolean isQueueEnabled() {
    return myQueueEnabled;
  }

  @Nullable
  public SUser getUser() {
    return myUser;
  }

  @NotNull
  public String getReason() {
    return myReason;
  }

  @NotNull
  public Date getTimestamp() {
    return myTimestamp;
  }

  @NotNull
  public Actor getActor() {
    return myActor;
  }
}