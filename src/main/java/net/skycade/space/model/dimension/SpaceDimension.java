package net.skycade.space.model.dimension;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

public class SpaceDimension {
  public static final DimensionType INSTANCE =
      DimensionType.builder(NamespaceID.from("minecraft:the_end"))
          .effects("minecraft:the_end")
          .skylightEnabled(true)
          .bedSafe(true)
          .ceilingEnabled(false)
          .build();

  static {
    MinecraftServer.getDimensionTypeManager().addDimension(INSTANCE);
  }
}
