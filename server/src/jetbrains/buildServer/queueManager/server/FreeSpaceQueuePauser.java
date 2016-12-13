package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateImpl;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.serverSide.impl.DiskSpaceWatcher;
import jetbrains.buildServer.util.Alarm;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class FreeSpaceQueuePauser {

  /**
   * Key for disabling feature in {@code TeamCityProperties}
   */
  @NotNull
  private static final String KEY_AUTO_PAUSE = "teamcity.queuePauser.pauseOnNoDiskSpace";

  @NotNull
  private static final String KEY_PAUSE_THRESHOLD = "teamcity.pauseBuildQueue.diskSpace.threshold";

  private static final long DEFAULT_THRESHOLD = 50 * 1024; // Default queue pausing threshold

  /**
   * Key for auto resuming queue
   */
  @NotNull
  private static final String KEY_AUTO_RESUME = "teamcity.queuePauser.resumeOnDiskSpace";

  @NotNull
  static final String DEFAULT_REASON = "Insufficient disk space. Please contact your system administrator.";

  @NotNull
  private static final Actor ACTOR = Actor.FREE_SPACE_QUEUE_PAUSER;

  @NotNull
  private final EventDispatcher<BuildServerListener> myDispatcher;

  @NotNull
  private final QueueStateManager myQueueStateManager;

  @NotNull
  private final DiskSpaceWatcher myDiskSpaceWatcher;

  private Alarm myWatcher = null;

  public FreeSpaceQueuePauser(@NotNull final EventDispatcher<BuildServerListener> dispatcher,
                              @NotNull final QueueStateManager queueStateManager,
                              @NotNull final DiskSpaceWatcher diskSpaceWatcher) {
    myDispatcher = dispatcher;
    myQueueStateManager = queueStateManager;
    myDiskSpaceWatcher = diskSpaceWatcher;
    initWatcher();
  }

  private void initWatcher() {
    int dswRepeatDelay = TeamCityProperties.getInteger("teamcity.diskSpaceWatcher.repeatDelay", 120 * 1000); // 120 seconds
    int qpRepeatDelay = TeamCityProperties.getInteger("teamcity.queuePauser.repeatDelay", dswRepeatDelay);
    qpRepeatDelay = Math.max(dswRepeatDelay + 1000, qpRepeatDelay); // no use updating more frequently than dsw
    if (myWatcher == null) {
      myWatcher = new Alarm("Queue pause/resume watcher");
      myWatcher.addRepeatableRequest(this::check, qpRepeatDelay, qpRepeatDelay);
      myDispatcher.addListener(new BuildServerAdapter() {
        @Override
        public void serverShutdown() {
          myWatcher.dispose();
        }
      });
    }
  }

  private boolean isEnabled() {
    return TeamCityProperties.getBooleanOrTrue(KEY_AUTO_PAUSE);
  }

  boolean isResumingEnabled() {
    return TeamCityProperties.getBooleanOrTrue(KEY_AUTO_RESUME);
  }

  private boolean canResume(@NotNull final QueueState state, @NotNull final Map<String, Long> dirsNoSpace) {
    return isResumingEnabled() && ACTOR.equals(state.getActor()) && dirsNoSpace.isEmpty();
  }

  private long getThreshold() {
    return TeamCityProperties.getLong(KEY_PAUSE_THRESHOLD, DEFAULT_THRESHOLD) * 1024;
  }

  private void check() {
    if (isEnabled()) {
      final QueueState qs = myQueueStateManager.readQueueState();
      final Map<String, Long> dirsNoSpace = myDiskSpaceWatcher.getDirsNoSpace();
      final Long threshold = getThreshold();
      final Set<String> keys = new HashSet<>(dirsNoSpace.keySet()); // filter dirs that have enough space to run build
      keys.stream().filter(key -> dirsNoSpace.get(key) > threshold).forEach(dirsNoSpace::remove);
      if (qs.isQueueEnabled()) {
        if (!dirsNoSpace.isEmpty()) { // disable queue
          final String pauseReason = getPauseReason(dirsNoSpace);
          final QueueState newState = new QueueStateImpl(false, null, pauseReason, new Date(), ACTOR);
          myQueueStateManager.writeQueueState(newState);
          Loggers.SERVER.info("Build queue was automatically paused. " + pauseReason);
        }
      } else {
        // queue is disabled. try to resume
        if (canResume(qs, dirsNoSpace)) {
          final String resumeReason = "Queue was automatically enabled as disk space became available";
          final QueueState newState = new QueueStateImpl(true, null, resumeReason, new Date(), ACTOR);
          myQueueStateManager.writeQueueState(newState);
          Loggers.SERVER.info(resumeReason);
        }
      }
    }
  }

  private String getPauseReason(@NotNull final Map<String, Long> dirsNoSpace) {
    final StringBuilder sb = new StringBuilder("Insufficient disk space in the following ");
    sb.append(StringUtil.pluralize("directory", dirsNoSpace.size())).append(": ");
    boolean first = true;
    for (Map.Entry<String, Long> e: dirsNoSpace.entrySet()) {
      if (!first) {
        sb.append(", ");
      }
      sb.append(e.getKey());
      sb.append(" (");
      sb.append(StringUtil.formatFileSize(e.getValue()));
      sb.append(")");
      first = false;
    }
    sb.append(". Disk space threshold is set to ");
    sb.append(StringUtil.formatFileSize(getThreshold()));
    sb.append(".");
    return sb.toString();
  }
}
