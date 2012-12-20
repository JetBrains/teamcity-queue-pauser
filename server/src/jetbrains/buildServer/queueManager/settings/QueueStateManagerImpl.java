package jetbrains.buildServer.queueManager.settings;

import jetbrains.buildServer.users.UserModel;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Oleg Rybak (oleg.rybak@jetbrains.com)
 */
public class QueueStateManagerImpl implements QueueStateManager {

  @NotNull
  private final SettingsManager mySettingsManager;
  private UserModel myUserModel;


  public QueueStateManagerImpl(@NotNull SettingsManager settingsManager, @NotNull UserModel userModel) {
    mySettingsManager = settingsManager;
    myUserModel = userModel;
  }

  // todo: locks???

  // todo: maybe builder?

  @NotNull
  @Override
  public QueueState readQueueState() {
    return new QueueState(
            mySettingsManager.isQueueEnabled(),
            myUserModel.findUserById(mySettingsManager.getQueueStateChangedBy()),
            mySettingsManager.getQueueStateChangedReason(),
            mySettingsManager.getQueueStateChangedOn()
    );
  }

  @Override
  public void writeQueueState(@NotNull QueueState queueState) {
    mySettingsManager.setQueueEnabled(queueState.isQueueEnabled());
    mySettingsManager.setQueueStateChangedBy(queueState.getUser().getId());
    mySettingsManager.setQueueStateChangedOn(queueState.getTimestamp());
    mySettingsManager.setQueueStateChangedReason(queueState.getReason()); // todo: implement me
  }
}
