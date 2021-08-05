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

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.queueManager.QueuePauserTestSupport;
import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.util.FileUtil;
import org.intellij.lang.annotations.Language;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Objects;

public class QueueStateManagerTest extends BaseServerTestCase {

  private QueueStateManager myQueueStateManager;

  @BeforeMethod(alwaysRun = true)
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    QueuePauserTestSupport.apply(myFixture);
    myQueueStateManager = myFixture.getSingletonService(QueueStateManager.class);
  }

  @Test
  public void testPersist() {
    Date date = new Date();
    SUser user = createUser("my_user");
    QueueState state = new QueueState(true, user, "test", date, Actor.USER);
    myQueueStateManager.writeQueueState(state);
    waitForAssert(() -> {
      File configFile = new File(myFixture.getServerPaths().getConfigDir(), "plugin.queue-pauser.xml");
      if (!configFile.isFile()) {
        return false;
      }
      Element e;
      try {
        e = FileUtil.parseDocument(configFile);
      } catch (JDOMException | IOException ex) {
        return false;
      }
      return e.getChildren("param").size() == 5;
    });
  }

  @Language("xml")
  private static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<settings>\n" +
          "  <param name=\"queue-enabled\" value=\"false\" />\n" +
          "  <param name=\"state-changed-reason\" value=\"External Change\" />\n" +
          "  <param name=\"state-changed-actor\" value=\"USER\" />\n" +
          "  <param name=\"state-changed-by\" value=\"{USER_ID}\" />\n" +
          "  <param name=\"state-changed-on\" value=\"1628175685085\" />\n" +
          "</settings>\n" +
          "\n";


  @Test
  public void testExternalChange() throws Exception {
    SUser user = createUser("mmy_new_user");
    final long id = user.getId();
    String content = TEMPLATE.replace("{USER_ID}", Long.toString(id));
    FileUtil.writeFile(new File(myFixture.getServerPaths().getConfigDir(), "plugin.queue-pauser.xml"), content, Charset.forName("UTF-8"));
    waitForAssert(() -> {
      QueueState s = myQueueStateManager.readQueueState();
      return !s.isQueueEnabled()
              && Objects.equals(s.getUser(), user)
              && Objects.equals(s.getReason(), "External Change");
    });
  }

}