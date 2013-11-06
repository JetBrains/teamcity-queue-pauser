package jetbrains.buildServer.queueManager.settings;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public enum Actor {

  /**
   * Last action was performed by user
   */
  USER,

  /**
   * Last action was performed by {@code jetbrains.buildServer.queueManager.server.FreeSpaceQueuePauser}
   * @see jetbrains.buildServer.queueManager.server.FreeSpaceQueuePauser
   */
  FREE_SPACE_QUEUE_PAUSER
}
