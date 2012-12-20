/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.serverSide.CustomSettings;
import jetbrains.buildServer.serverSide.CustomSettingsManager;
import jetbrains.buildServer.serverSide.SettingsMap;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class SettingsManagerImpl implements SettingsManager {

  private interface FIELDS {
    public static final String QUEUE_STATE = "queue-state";
    public static final String SWITCHED_BY = "switched-by";
    public static final String SWITCHED_ON = "switched-on";
  }

  private static final Map<String, String> DEFAULTS;
  static {
    final Map<String, String> defaults = new HashMap<String, String>();
    // queue is enabled by default
    defaults.put(FIELDS.QUEUE_STATE, Boolean.toString(Boolean.TRUE));
    defaults.put(FIELDS.SWITCHED_ON, Long.toString(System.currentTimeMillis()));
    DEFAULTS = Collections.unmodifiableMap(defaults);
  }

  private static final String DEFAULT_VALUE = "";

  private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock(true);

  private final SettingsMap mySettingsMap;

  public SettingsManagerImpl(@NotNull PluginDescriptor pluginDescriptor,
                             @NotNull CustomSettingsManager customSettingsManager) {
    final CustomSettings settings = customSettingsManager.getCustomSettings(pluginDescriptor);
    mySettingsMap = settings.getGlobalSettings();
  }


  @Override
  public void setQueueState(boolean newQueueState) {
    try {
      myLock.writeLock().lock();
      mySettingsMap.setValue(FIELDS.QUEUE_STATE, Boolean.toString(newQueueState));
    } finally {
      myLock.writeLock().unlock();
    }
  }

  @Override
  public boolean getQueueState() {
    boolean result;
    try {
      myLock.readLock().lock();
      final String val = readValueWithDefault(FIELDS.QUEUE_STATE);
      result =  Boolean.valueOf(val);
    } finally {
      myLock.readLock().unlock();
    }
    return result;
  }

  @Override
  @NotNull
  public String getQueueStateSwitchedBy() {
    String result;
    try {
      myLock.readLock().lock();
      result = readValueWithDefault(FIELDS.SWITCHED_BY);
    } finally {
      myLock.readLock().unlock();
    }
    return result;
  }

  @Override
  public void setQueueStateSwitchedBy(@NotNull String userName) {
    try {
      myLock.writeLock().lock();
      mySettingsMap.setValue(FIELDS.SWITCHED_BY, userName);
    } finally {
      myLock.writeLock().unlock();
    }
  }

  @NotNull
  public Date getQueueStateSwitchedOn() {
    Date result;
    try {
      myLock.readLock().lock();
      final String val = readValueWithDefault(FIELDS.SWITCHED_ON);
      result = new Date(Long.parseLong(val));
    } finally {
      myLock.readLock().unlock();
    }
    return result;
  }

  @Override
  public void setQueueStateSwitchedOn(@NotNull Date date) {
    try {
      myLock.writeLock().lock();
      mySettingsMap.setValue(FIELDS.SWITCHED_ON, Long.toString(date.getTime()));
    } finally {
      myLock.writeLock().unlock();
    }
  }

  /**
   * Reads value from storage.
   * <b>Should be called under read lock</b>, as it promotes it to write lock
   * to write default value
   *
   * @param key key for the value
   * @return value from storage. If value is absent, returns {@code default} value.
   * If default value is absent, returns {@code empty string}.
   */
  @NotNull
  private String readValueWithDefault(String key) {
    String result = mySettingsMap.getValue(key);
    if (result == null) {
      try {
        myLock.readLock().unlock();
        myLock.writeLock().lock();
        result = DEFAULTS.get(key);
        if (result == null) {
          result = DEFAULT_VALUE;
        }
        mySettingsMap.setValue(key, result);
      } finally {
        myLock.readLock().lock();
        myLock.writeLock().unlock();
      }
    }
    return result;
  }


}
