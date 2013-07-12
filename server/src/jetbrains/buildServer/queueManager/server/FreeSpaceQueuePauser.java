package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateImpl;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.impl.DiskSpaceWatcher;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class FreeSpaceQueuePauser extends BuildServerAdapter {

  /**
   * Key for disabling feature in {@code TeamCityProperties}
   */
  @NotNull
  private static final String KEY = "teamcity.queuePauser.pauseOnNoDiskSpace";

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final DiskSpaceWatcher myDiskSpaceWatcher;

  public FreeSpaceQueuePauser(@NotNull final EventDispatcher<BuildServerListener> dispatcher,
                              @NotNull final QueueStateManager queueStateManager,
                              @NotNull final DiskSpaceWatcher diskSpaceWatcher) {
    myQueueStateManager = queueStateManager;
    myDiskSpaceWatcher = diskSpaceWatcher;
    dispatcher.addListener(this);
  }

  @Override
  public void buildQueueOrderChanged() {
    check();
  }

  @Override
  public void buildTypeAddedToQueue(final SBuildType buildType) {
    check();
  }

  @Override
  public void buildTypeAddedToQueue(@NotNull SQueuedBuild queuedBuild) {
    check();
  }

  @Override
  public void buildRemovedFromQueue(@NotNull SQueuedBuild queued, User user, String comment) {
    check();
  }

  private boolean isEnabled() {
    return TeamCityProperties.getBooleanOrTrue(KEY);
  }

  /**
   * Checks if it is required to pause queue
   */
  private void check() {
    if (isEnabled() && myQueueStateManager.readQueueState().isQueueEnabled()) {
      final Set<String> dirsNoSpace = myDiskSpaceWatcher.getDirsNoSpace();
      if (!dirsNoSpace.isEmpty()) { // some dirs lack required space remaining
        final StringBuilder sb = new StringBuilder("insufficient disk space in the following director");
        sb.append(dirsNoSpace.size() > 1 ? "ies: " : "y: ");
        for (String dir: dirsNoSpace) {
          sb.append(dir).append(", ");
        }
        final String reason = sb.substring(0, sb.length() - 2);
        final QueueState newState = new QueueStateImpl(false, null, reason, new Date());
        myQueueStateManager.writeQueueState(newState);
      }
    }
  }
}
