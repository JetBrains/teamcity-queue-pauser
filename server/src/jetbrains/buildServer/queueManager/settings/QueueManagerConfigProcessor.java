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

import jetbrains.buildServer.serverSide.MainConfigProcessor;
import org.jdom.Element;

/**
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueManagerConfigProcessor implements MainConfigProcessor {

  interface XML {
    public static final String ELEMENT_PLUGIN_ROOT = "TeamCity.QueueManager";
    /** Element responsible for global queue state. */
    public static final String ELEMENT_GLOBAL_QUEUE_STATE = "global_state";
  }

  private boolean queueEnabled = true;

  public boolean isQueueEnabled() {
    return queueEnabled;
  }

  public void readFrom(Element rootElement) {
    // get root element for plugin
    Element pluginRoot = rootElement.getChild(XML.ELEMENT_PLUGIN_ROOT);
    if (pluginRoot != null) {
      Element element = pluginRoot.getChild(XML.ELEMENT_GLOBAL_QUEUE_STATE);
      queueEnabled = Boolean.valueOf(element.getText());
    }
  }

  public void writeTo(Element parentElement) {
    Element pluginRoot = new Element(XML.ELEMENT_PLUGIN_ROOT);
    Element e = new Element(XML.ELEMENT_GLOBAL_QUEUE_STATE);
    e.setText(Boolean.toString(queueEnabled));
    pluginRoot.addContent(e);
    parentElement.addContent(pluginRoot);
  }


}
