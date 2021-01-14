/*
 * Copyright 2000-2021 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
