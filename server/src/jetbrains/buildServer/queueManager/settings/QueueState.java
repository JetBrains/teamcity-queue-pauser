package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueState {

  private static final String MESSAGE_FORMAT_ENABLED = "Build Queue is enabled by %s on %s (%s).";
  private static final String MESSAGE_FORMAT_DISABLED = "Build Queue is disabled by %s on %s (%s).";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);

  private boolean queueEnabled;

  private SUser user;

  @NotNull
  private String reason;

  @NotNull
  private Date timestamp;

  public QueueState(boolean enabled,SUser user , @NotNull String reason, @NotNull Date date) {
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

  public String describe() { // todo: initial plugin loading: what user to use?, move this out of class mb
    String format;
    if (queueEnabled) {
      format = MESSAGE_FORMAT_ENABLED;
    } else {
      format = MESSAGE_FORMAT_DISABLED;
    }
    return String.format(format, user != null ? user.getDescriptiveName() : "unknown", DATE_FORMAT.format(timestamp), reason);
  }
}
