package jetbrains.buildServer.queueManager.settings;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

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

  @NotNull
  public Date getQueueStateSwitchedOn();

  public void setQueueStateSwitchedOn(@NotNull Date date);


}
