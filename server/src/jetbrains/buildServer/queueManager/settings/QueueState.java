package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public final class QueueState {

  private final boolean queueEnabled;

  private final SUser user;

  @NotNull
  private final String reason;

  @NotNull
  private final Date timestamp;

  public QueueState(boolean enabled, SUser user , @NotNull String reason, @NotNull Date date) {
    this.queueEnabled = enabled;
    this.user = user;
    this.reason = reason;
    this.timestamp = date;
  }

  public boolean isQueueEnabled() {
    return queueEnabled;
  }

  public SUser getUser() {
    return user;
  }

  @NotNull
  public String getReason() {
    return reason;
  }

  @NotNull
  public Date getTimestamp() {
    return timestamp;
  }
}
