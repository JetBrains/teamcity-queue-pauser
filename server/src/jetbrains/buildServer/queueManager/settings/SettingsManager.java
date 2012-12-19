package jetbrains.buildServer.queueManager.settings;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public interface SettingsManager extends SettingsProvider {

  public void setQueueState(boolean newQueueState);

}
