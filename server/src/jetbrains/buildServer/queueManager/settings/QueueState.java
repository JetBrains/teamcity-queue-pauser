package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.SUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueState {

  private static final String MESSAGE_FORMAT = "Build Queue is disabled by %s on %s";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);

  private boolean queueEnabled;

  private SUser user;

  private String reason;

  private Date timestamp;

  public QueueState(boolean enabled, SUser user, String reason, Date date) {
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

  public String getReason() {
    return reason;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public String describe() { // todo: initial plugin loading: what user to use?
    return String.format(MESSAGE_FORMAT, user != null ? user.getDescriptiveName() : "unknown", DATE_FORMAT.format(timestamp));
  }
}
