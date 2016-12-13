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
public final class QueueStateImpl implements QueueState {

  private final boolean myQueueEnabled;

  @Nullable
  private final SUser myUser;

  @NotNull
  private final String myReason;

  @NotNull
  private final Date myTimestamp;

  @NotNull
  private final Actor myActor;

  public QueueStateImpl(boolean enabled,
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

  @Override
  public boolean isQueueEnabled() {
    return myQueueEnabled;
  }

  @Nullable
  @Override
  public SUser getUser() {
    return myUser;
  }

  @Override
  @NotNull
  public String getReason() {
    return myReason;
  }

  @Override
  @NotNull
  public Date getTimestamp() {
    return myTimestamp;
  }

  @NotNull
  @Override
  public Actor getActor() {
    return myActor;
  }
}
