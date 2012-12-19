package jetbrains.buildServer.queueManager.settings;

import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class SettingsManagerImpl implements SettingsManager {

  private volatile boolean queueState;

  @Override
  public void setQueueState(boolean newQueueState) {
    queueState = newQueueState;
  }

  @NotNull
  @Override
  public QueueManagerSettings getSettings() {
    return new QueueManagerSettings(queueState);
  }
}
