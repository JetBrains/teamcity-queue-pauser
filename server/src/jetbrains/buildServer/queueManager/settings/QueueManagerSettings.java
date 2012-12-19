package jetbrains.buildServer.queueManager.settings;

/**
 * Created with IntelliJ IDEA.
 *
 * Contains settings for Queue Manager
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueManagerSettings {

  private final boolean isQueueEnabled;


  public QueueManagerSettings(boolean queueEnabled) {
    isQueueEnabled = queueEnabled;
  }

  public boolean isQueueEnabled() {
    return isQueueEnabled;
  }
}
