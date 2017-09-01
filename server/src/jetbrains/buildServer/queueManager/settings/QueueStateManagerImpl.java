package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
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

  @NotNull
  private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock(true);

  private QueueState myCachedState = null;

  public QueueStateManagerImpl(@NotNull final SettingsManager settingsManager, @NotNull final UserModel userModel) {
    mySettingsManager = settingsManager;
    myUserModel = userModel;
  }

  @NotNull
  @Override
  public QueueState readQueueState() {
    Date changedOn;
    try {
      myLock.readLock().lock();
      changedOn = mySettingsManager.getQueueStateChangedOn();
      if (myCachedState != null && changedOn.equals(myCachedState.getTimestamp())) {
        return myCachedState;
      }
    } finally {
      myLock.readLock().unlock();
    }
    try {
      myLock.writeLock().lock();
      final Long userId = mySettingsManager.getQueueStateChangedBy();
      myCachedState = new QueueStateImpl(
              mySettingsManager.isQueueEnabled(),
              userId != null ? myUserModel.findUserById(userId) : null,
              mySettingsManager.getQueueStateChangedReason(),
              changedOn,
              mySettingsManager.getQueueStateChangedActor()
      );
      return myCachedState;
    } finally {
      myLock.writeLock().unlock();
    }
  }

  @Override
  public void writeQueueState(@NotNull final QueueState queueState) {
    try {
      myLock.writeLock().lock();
      mySettingsManager.setQueueEnabled(queueState.isQueueEnabled());
      mySettingsManager.setQueueStateChangedBy(queueState.getUser() != null ? queueState.getUser().getId(): null);
      mySettingsManager.setQueueStateChangedOn(queueState.getTimestamp());
      mySettingsManager.setQueueStateChangedReason(queueState.getReason());
      mySettingsManager.setQueueStateChangedActor(queueState.getActor());
      myCachedState = queueState;
    } finally {
      myLock.writeLock().unlock();
    }
  }
}
