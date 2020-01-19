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

import jetbrains.buildServer.BaseJMockTestCase;
import jetbrains.buildServer.Mocked;
import jetbrains.buildServer.serverSide.CustomSettings;
import jetbrains.buildServer.serverSide.CustomSettingsManager;
import jetbrains.buildServer.serverSide.SettingsMap;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Contains tests for {@code SettingsManagerImpl}
 *
 * @see SettingsManager
 * @see SettingsManagerImpl
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
@TestFor (testForClass = {SettingsManager.class, SettingsManagerImpl.class})
public class SettingsManagerImplTest extends BaseJMockTestCase {

  @Mocked
  private SettingsManager mySettingsManager;

  @Mocked
  private CustomSettingsManager myCustomSettingsManager;

  @Mocked
  private CustomSettings myCustomSettings;

  @Mocked
  private PluginDescriptor myPluginDescriptor;

  @Mocked
  private SettingsMap mySettingsMap;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // common expectations for settings manager
    m.checking(new Expectations() {{
      oneOf(myCustomSettingsManager).getCustomSettings(myPluginDescriptor);
      will(returnValue(myCustomSettings));

      oneOf(myCustomSettings).getGlobalSettings();
      will(returnValue(mySettingsMap));
    }});
    mySettingsManager = new SettingsManagerImpl(myPluginDescriptor, myCustomSettingsManager);
  }

  @Test
  public void testIsQueueEnabled_Default() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.QUEUE_ENABLED);
      will(returnValue(null));

      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.QUEUE_ENABLED, "true");
    }});

    boolean defaultQueueState = mySettingsManager.isQueueEnabled();
    assertTrue("Queue must be enabled by default", defaultQueueState);
  }

  @Test
  public void testIsQueueEnabled() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.QUEUE_ENABLED);
      will(returnValue("false"));
    }});

    boolean queueEnabled = mySettingsManager.isQueueEnabled();
    assertFalse(queueEnabled);
    m.assertIsSatisfied();
  }

  @Test
  public void testSetQueueEnabled() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.QUEUE_ENABLED, "false");
    }});

    mySettingsManager.setQueueEnabled(false);
    m.assertIsSatisfied();
  }

  @Test
  public void testGetQueueStateChangedBy_Default() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_BY);
      will(returnValue(null));

      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_BY, "");
    }});

    final Long l = mySettingsManager.getQueueStateChangedBy();
    assertNull(l);
  }

  @Test
  public void testGetQueueStateChangedBy() throws Exception {
    final Long val = 12345L;
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_BY);
      will(returnValue(Long.toString(val)));
    }});

    final Long l = mySettingsManager.getQueueStateChangedBy();
    assertEquals(val, l);
  }

  @Test
  public void testSetQueueStateChangedBy() throws Exception {
    final Long newVal = 54321L;
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_BY, Long.toString(newVal));
    }});

    mySettingsManager.setQueueStateChangedBy(newVal);
  }

  /**
   * Support for automatic queue pausing (no user involved)
   * @throws Exception if something goes wrong
   */
  @Test
  @TestFor (issues = "TW-10787")
  public void testSetQueueStateChangedBy_Null() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_BY, "");
    }});
    mySettingsManager.setQueueStateChangedBy(null);
  }

  @Test
  public void testGetQueueStateChangedOn_Default() throws Exception {
    final long customTime = Dates.makeDate(2012, 12, 12).getTime();
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_ON);
      will(returnValue(null));

      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_ON, Long.toString(customTime));
    }});

    final Date defaultDate = mySettingsManager.getQueueStateChangedOn();
    assertEquals(customTime, defaultDate.getTime());
  }

  @Test
  public void testGetQueueStateChangedOn() throws Exception {
    final Date now = new Date();
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_ON);
      will(returnValue(Long.toString(now.getTime())));
    }});

    final Date date = mySettingsManager.getQueueStateChangedOn();
    assertEquals(now, date);
  }

  @Test
  public void testSetQueueStateChangedOn() throws Exception {
    final Date now = new Date();
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_ON, Long.toString(now.getTime()));
    }});

    mySettingsManager.setQueueStateChangedOn(now);
  }

  @Test
  public void testGetQueueStateChangedReason_Default() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_REASON);
      will(returnValue(null));

      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_REASON, "");
    }});

    final String result = mySettingsManager.getQueueStateChangedReason();
    assertEquals("", result);
  }

  @Test
  public void testGetQueueStateChangedReason() throws Exception {
    final String expected = "This is a test string";

    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_REASON);
      will(returnValue(expected));
    }});

    final String result = mySettingsManager.getQueueStateChangedReason();
    assertEquals(expected, result);
  }

  @Test
  public void testSetQueueStateChangedReason() throws Exception {
    final String newValue = "NEW_VALUE";
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_REASON, newValue);
    }});

    mySettingsManager.setQueueStateChangedReason(newValue);
  }

  @Test
  @TestFor(issues = "TW-33042")
  public void testGetQueueStateChangedActor_Default() throws Exception {
    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_BY_ACTOR);
      will(returnValue(null));

      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_BY_ACTOR, Actor.USER.name());
    }});

    final Actor result = mySettingsManager.getQueueStateChangedActor();
    assertEquals(Actor.USER, result);
  }

  @Test
  @TestFor(issues = "TW-33042")
  public void testGetQueueStateChangedActor() throws Exception {
    final Actor expected = Actor.values()[0];

    m.checking(new Expectations() {{
      oneOf(mySettingsMap).getValue(SettingsManagerImpl.FIELDS.CHANGED_BY_ACTOR);
      will(returnValue(expected.name()));
    }});

    final Actor result = mySettingsManager.getQueueStateChangedActor();
    assertEquals(expected, result);
  }

  @Test
  @TestFor(issues = "TW-33042")
  public void testSetQueueStateChangedActor() throws Exception {
    final Actor value = Actor.values()[0];

    m.checking(new Expectations() {{
      oneOf(mySettingsMap).setValue(SettingsManagerImpl.FIELDS.CHANGED_BY_ACTOR, value.name());
    }});

    mySettingsManager.setQueueStateChangedActor(value);
  }
}
