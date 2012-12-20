package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateManagerImpl implements QueueStateManager {

  @NotNull
  private final SettingsManager mySettingsManager;

  @NotNull
  private final UserModel myUserModel;

  private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock(true);

  public QueueStateManagerImpl(@NotNull SettingsManager settingsManager, @NotNull UserModel userModel) {
    mySettingsManager = settingsManager;
    myUserModel = userModel;
  }

  @NotNull
  @Override
  public QueueState readQueueState() {
    try {
      myLock.readLock().lock();
      return new QueueState(
              mySettingsManager.isQueueEnabled(),
              myUserModel.findUserById(mySettingsManager.getQueueStateChangedBy()),
              mySettingsManager.getQueueStateChangedReason(),
              mySettingsManager.getQueueStateChangedOn()
      );
    } finally {
      myLock.readLock().unlock();
    }
  }

  @Override
  public void writeQueueState(@NotNull QueueState queueState) {
    try {
      myLock.writeLock().lock();
      mySettingsManager.setQueueEnabled(queueState.isQueueEnabled());
      mySettingsManager.setQueueStateChangedBy(queueState.getUser().getId());
      mySettingsManager.setQueueStateChangedOn(queueState.getTimestamp());
      mySettingsManager.setQueueStateChangedReason(queueState.getReason());
    } finally {
      myLock.writeLock().unlock();
    }
  }
}
