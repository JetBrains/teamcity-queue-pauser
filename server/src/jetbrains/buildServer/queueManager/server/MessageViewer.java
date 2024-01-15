

package jetbrains.buildServer.queueManager.server;

import jetbrains.buildServer.queueManager.settings.Actor;
import jetbrains.buildServer.queueManager.settings.QueueState;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class {@code MessageViewer}
 *
 * Provides mechanism to display different messages for non-admins
 * if automatically generated messages contain sensitive data
 *
 * @author Oleg Rybak <oleg.rybak@jetbrains.com>
 */
public class MessageViewer {

  private static final Map<Actor, String> DEFAULT_MESSAGES = new HashMap<Actor, String>() {{
    put(Actor.FREE_SPACE_QUEUE_PAUSER, FreeSpaceQueuePauser.DEFAULT_REASON);
  }};

  public static String viewMessage(@Nullable final SUser user, @NotNull final QueueState state) {
    if (user == null || !user.isSystemAdministratorRoleGranted()) {
      final String defaultMessage = DEFAULT_MESSAGES.get(state.getActor());
      return defaultMessage != null ? defaultMessage : state.getReason();
    }
    return state.getReason();
  }

}