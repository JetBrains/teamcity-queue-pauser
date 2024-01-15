

package jetbrains.buildServer.queueManager.settings;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.impl.FileWatcherFactory;
import jetbrains.buildServer.serverSide.impl.persisting.SettingsPersister;
import jetbrains.buildServer.serverSide.healthStatus.GlobalHealthItemsTracker;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Boolean.parseBoolean;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateManager {

  @NotNull
  private static final Logger LOG = Logger.getInstance(QueueStateManager.class.getName());

  private static final String FILENAME = "plugin.queue-pauser.xml";
  private static final String SYSTEM_PROPERTY = "teamcity.plugin.queuePauser.queue.enabled";

  interface FIELDS {
    String QUEUE_ENABLED = "queue-enabled";
    String CHANGED_BY = "state-changed-by";
    String CHANGED_ON = "state-changed-on";
    String CHANGED_REASON = "state-changed-reason";
    String CHANGED_BY_ACTOR = "state-changed-actor";
  }

  private static final Map<String, String> DEFAULTS;

  static {
    final Map<String, String> defaults = new HashMap<>();
    // queue is enabled by default
    defaults.put(FIELDS.QUEUE_ENABLED, Boolean.toString(Boolean.TRUE));
    defaults.put(FIELDS.CHANGED_ON, Long.toString(Dates.makeDate(2012, 12, 12).getTime())); // 12.12.2012 0:0:0  for ease of testing
    defaults.put(FIELDS.CHANGED_BY_ACTOR, Actor.USER.name());
    DEFAULTS = Collections.unmodifiableMap(defaults);
  }

  private final AtomicReference<QueueState> myStateRef = new AtomicReference<>(from(DEFAULTS));

  @NotNull
  private final UserModel myUserModel;

  @NotNull
  private final SettingsPersister mySettingsPersister;

  @NotNull
  private final FileWatcher myChangeObserver;

  @NotNull
  private final GlobalHealthItemsTracker myGlobalHealthItemsTracker;

  private final File myConfigFile;

  public QueueStateManager(@NotNull final UserModel userModel,
                           @NotNull final SettingsPersister settingsPersister,
                           @NotNull final FileWatcherFactory fileWatcherFactory,
                           @NotNull final ServerPaths serverPaths,
                           @NotNull final GlobalHealthItemsTracker globalHealthItemsTracker) {
    myConfigFile = new File(serverPaths.getConfigDir(), FILENAME);
    myUserModel = userModel;
    mySettingsPersister = settingsPersister;
    myChangeObserver = fileWatcherFactory.createFileWatcher(myConfigFile);
    myGlobalHealthItemsTracker = globalHealthItemsTracker;
    myChangeObserver.registerListener(it -> doLoad());
    myChangeObserver.start();
    doLoad();
  }

  @NotNull
  public QueueState readQueueState() {
    return myStateRef.get();
  }

  public void writeQueueState(@NotNull QueueState queueState) {
    myStateRef.set(queueState);
    final Map<String, String> properties = to(queueState);
    Element root = new Element("settings");
    properties.forEach((key, value) -> {
      Element paramEl = new Element("param");
      paramEl.setAttribute("name", key);
      paramEl.setAttribute("value", value);
      root.addContent(paramEl);
    });
    try {
      mySettingsPersister.scheduleSaveDocument("Save queue state", myChangeObserver, new Document(root));
    } catch (IOException e) {
      LOG.warnAndDebugDetails("Failed to save queue state into file \"" + myConfigFile.getAbsolutePath() + "\"", e);
    }
    setSystemProperty(properties.get(FIELDS.QUEUE_ENABLED));
    myGlobalHealthItemsTracker.recalculate();
  }

  private void setSystemProperty(String notificationsEnabled) {
    System.setProperty(SYSTEM_PROPERTY, notificationsEnabled);
  }

  private void doLoad() {
    if (!myConfigFile.exists() || !myConfigFile.canRead()) {
      return; // initial state is already loaded
    }
    Map<String, String> result = new HashMap<>();
    Element element;
    try {
      element = FileUtil.parseDocument(myConfigFile);
    } catch (Exception e) {
      LOG.warnAndDebugDetails("Failed to load usage statistics settings from file \"" + myConfigFile.getAbsolutePath() + "\"", e);
      return;
    }
    element.getChildren("param").forEach(it -> {
      if (it instanceof Element) {
        Element el = (Element) it;
        final String name = el.getAttributeValue("name");
        final String value = el.getAttributeValue("value");
        if (!StringUtil.isEmptyOrSpaces(name) && !StringUtil.isEmptyOrSpaces(value)) {
          result.put(name, value);
        }
      }
    });
    myStateRef.set(from(result));
    setSystemProperty(result.get(FIELDS.QUEUE_ENABLED));
    myGlobalHealthItemsTracker.recalculate();
  }

  private Map<String, String> to(@NotNull final QueueState queueState) {
    Map<String, String> result = new HashMap<>();
    result.put(FIELDS.QUEUE_ENABLED, Boolean.toString(queueState.isQueueEnabled()));
    if (queueState.getUser() != null) {
      result.put(FIELDS.CHANGED_BY, String.valueOf(queueState.getUser().getId()));
    }
    result.put(FIELDS.CHANGED_ON, Long.toString(queueState.getTimestamp().getTime()));
    result.put(FIELDS.CHANGED_REASON, queueState.getReason());
    result.put(FIELDS.CHANGED_BY_ACTOR, queueState.getActor().name());
    return result;
  }

  private QueueState from(@NotNull final Map<String, String> properties) {
    Long userId = null;
    final String userIdStr = readValueWithDefault(properties, FIELDS.CHANGED_BY);
    if (!StringUtil.isEmptyOrSpaces(userIdStr)) {
      try {
        userId = Long.parseLong(userIdStr);
      } catch (NumberFormatException ignored) {
      }
    }

    SUser user = null;
    if (userId != null) {
      user = myUserModel != null ? myUserModel.findUserById(userId) : null;
    }

    Actor actor;
    try {
      actor = Actor.valueOf(readValueWithDefault(properties, FIELDS.CHANGED_BY_ACTOR));
    } catch (IllegalArgumentException e) {
      actor = Actor.valueOf(DEFAULTS.get(FIELDS.CHANGED_BY_ACTOR));
    }

    return new QueueState(
            parseBoolean(readValueWithDefault(properties, FIELDS.QUEUE_ENABLED)),
            user,
            readValueWithDefault(properties, FIELDS.CHANGED_REASON),
            new Date(Long.parseLong(readValueWithDefault(properties, FIELDS.CHANGED_ON))),
            actor
    );
  }

  private static String readValueWithDefault(@NotNull final Map<String, String> properties, @NotNull final String key) {
    return properties.getOrDefault(key, DEFAULTS.getOrDefault(key, ""));
  }
}