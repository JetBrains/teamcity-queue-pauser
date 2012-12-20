package jetbrains.buildServer.queueManager.settings;

import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public interface QueueStateManager {

  /**
   * Reads queue state from storage
   * @return queue state
   */
  @NotNull
  public QueueState readQueueState();

  /**
   * Writes queue state to storage
   * @param queueState queue state
   */
  public void writeQueueState(@NotNull QueueState queueState);


}
