/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
