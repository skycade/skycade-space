package net.skycade.space.model.sector.contained;

import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.physics.vector.SectorContainedVec;
import net.skycade.space.space.SpaceShipSpace;

public class SectorSpaceShip extends SectorContainedObject {

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position the position of the object relative to the sector.
   */
  public SectorSpaceShip(SectorContainedPos position) {
    super(position);
  }

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position     the position of the object relative to the sector.
   * @param velocity     the velocity of the object.
   * @param acceleration the acceleration of the object.
   */
  public SectorSpaceShip(SectorContainedPos position, SectorContainedVec velocity,
                         SectorContainedVec acceleration) {
    super(position, velocity, acceleration);
  }

  @Override
  public void draw(SpaceShipSpace space) {
    // no-op, it's drawn using blocks already.
  }
}
