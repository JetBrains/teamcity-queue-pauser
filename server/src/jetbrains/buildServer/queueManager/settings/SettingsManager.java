package jetbrains.buildServer.queueManager.settings;

import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public interface SettingsManager {

  public boolean getQueueState();
  public void setQueueState(boolean newQueueState);

  @NotNull
  public String getQueueStateSwitchedBy();

  public void setQueueStateSwitchedBy(@NotNull String userName);

}
