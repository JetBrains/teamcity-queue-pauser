

package jetbrains.buildServer.queueManager;

/**
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public final class PluginConstants {

  public interface WEB {
    String QUEUE_ACTIONS_URL = "/queueStartStop.html";
    String PARAM_NEW_QUEUE_STATE = "newQueueState";
    String PARAM_STATE_CHANGE_REASON = "stateChangeReason";
    String PARAM_QUEUE_STATE = "queueState";
  }
}