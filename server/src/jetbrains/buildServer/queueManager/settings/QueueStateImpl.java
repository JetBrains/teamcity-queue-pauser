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

  private final boolean queueEnabled;

  @Nullable
  private final SUser user;

  @NotNull
  private final String reason;

  @NotNull
  private final Date timestamp;

  public QueueStateImpl(boolean enabled, @Nullable SUser user, @NotNull String reason, @NotNull Date date) {
    this.queueEnabled = enabled;
    this.user = user;
    this.reason = reason;
    this.timestamp = date;
  }

  @Override
  public boolean isQueueEnabled() {
    return queueEnabled;
  }

  @Nullable
  @Override
  public SUser getUser() {
    return user;
  }

  @Override
  @NotNull
  public String getReason() {
    return reason;
  }

  @Override
  @NotNull
  public Date getTimestamp() {
    return timestamp;
  }
}
