

package jetbrains.buildServer.queueManager;

import jetbrains.BuildServerCreator;
import jetbrains.buildServer.MockTimeService;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.healthStatus.GlobalHealthItemsTracker;
import jetbrains.buildServer.queueManager.server.FreeSpaceQueuePauser;
import jetbrains.buildServer.queueManager.server.QueuePausePrecondition;
import jetbrains.buildServer.queueManager.settings.QueueStateManager;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.CriticalErrors;
import jetbrains.buildServer.serverSide.MockServerPluginDescriptior;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import jetbrains.buildServer.serverSide.impl.DiskSpaceWatcher;
import jetbrains.buildServer.serverSide.impl.FileWatcherFactory;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class QueuePauserTestSupport {
  public static void apply(@NotNull final BuildServerCreator fixture) {
    final PluginDescriptor descriptor = new MockServerPluginDescriptior();

    GlobalHealthItemsTracker tracker = new GlobalHealthItemsTracker() {
      @Override
      public void recalculate() {
      }
    };

    FastFileWatcherFactory factory = new FastFileWatcherFactory(
            fixture.getServerPaths(),
            fixture.getCriticalErrors(),
            fixture.getEventDispatcher());

    QueueStateManager queueStateManager = new QueueStateManager(
            fixture.getUserModel(),
            fixture.getSettingsPersister(),
            factory,
            fixture.getServerPaths(),
            tracker);
    QueuePausePrecondition precondition = new QueuePausePrecondition(queueStateManager);
    DiskSpaceWatcher watcher = new DiskSpaceWatcher(fixture.getServerPaths(),
            fixture.getServerSettings(), fixture.getSingletonService(ExecutorServices.class),
            new MockTimeService(),
            fixture.getEventDispatcher());

    FreeSpaceQueuePauser freeSpaceQueuePauser = new FreeSpaceQueuePauser(fixture.getEventDispatcher(),
            queueStateManager,
            watcher,
            fixture.getServerResponsibility(),
            tracker);

    fixture.addService(descriptor);
    fixture.addService(queueStateManager);
    fixture.addService(precondition);
    fixture.addService(freeSpaceQueuePauser);
  }

  private static class FastFileWatcherFactory extends FileWatcherFactory {

    public FastFileWatcherFactory(@NotNull final ServerPaths serverPaths,
                                  @NotNull final CriticalErrors criticalErrors,
                                  @NotNull final EventDispatcher<BuildServerListener> dispatcher) {
      super(serverPaths, criticalErrors);
      setEventDispatcher(dispatcher);
    }

    @NotNull
    public FileWatcher createFileWatcher(@NotNull File fileToWatch) {
      FileWatcher fileWatcher = new FileWatcher(fileToWatch);
      fileWatcher.setSleepingPeriod(100);
      return fileWatcher;
    }
  }
}