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
   * @param position            the position of the object relative to the sector.
   * @param velocity            the velocity of the object.
   * @param acceleration        the acceleration of the object.
   * @param rotation            the rotation of the object.
   * @param angularVelocity     the velocity of the rotation of the object.
   * @param angularAcceleration the acceleration of the rotation of the object.
   */

  public SectorSpaceShip(SectorContainedPos position, SectorContainedVec velocity,
                         SectorContainedVec acceleration, SectorContainedPos rotation,
                         SectorContainedVec angularVelocity,
                         SectorContainedVec angularAcceleration) {
    super(position, velocity, acceleration, rotation, angularVelocity, angularAcceleration);
  }

  @Override
  public void draw(SpaceShipSpace space) {
    // no-op, it's drawn using blocks already.
  }
}
