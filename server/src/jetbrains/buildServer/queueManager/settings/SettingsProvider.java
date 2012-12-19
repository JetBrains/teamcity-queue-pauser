package jetbrains.buildServer.queueManager.settings;

import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public interface SettingsProvider {

  @NotNull
  public QueueManagerSettings getSettings();

}
