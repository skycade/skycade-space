package net.skycade.space;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.NamespaceID;
import net.skycade.serverruntime.api.Game;
import net.skycade.serverruntime.api.event.EventHandler;
import net.skycade.space.handler.MinecraftSignHandler;
import net.skycade.space.space.SpaceShipSpace;

/**
 * Skycade Space Bootstrapper.
 */
public class SkycadeSpaceBootstrapper extends Game {

  /**
   * Main method of the game.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    new SkycadeSpaceBootstrapper().init();
  }

  @Override
  public void onCompleteStartup() {
    MinecraftServer.getBlockManager()
        .registerHandler(NamespaceID.from("minecraft:sign"), MinecraftSignHandler::new);
  }

  @Override
  protected List<Command> commands() {
    return Collections.emptyList();
  }

  @Override
  protected List<EventNode<Event>> eventNodes() {
    return List.of(new EventHandler<>(EventNode.all("global-" + UUID.randomUUID())) {
      @Override
      protected void init() {
      }
    }.node());
  }

  @Override
  protected List<InstanceContainer> instances() {
    return List.of(SpaceShipSpace.INSTANCE);
  }

  @Override
  protected InstanceContainer provideSpawningInstance(Player player) {
    return SpaceShipSpace.INSTANCE;
  }
}
