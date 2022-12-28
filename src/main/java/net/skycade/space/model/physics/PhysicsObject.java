package net.skycade.space.model.physics;

import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.physics.vector.SectorContainedVec;
import net.skycade.space.model.sector.contained.SectorContainedObject;

/**
 * Represents a physics object.
 *
 * @author Jacob Cohen
 */
public abstract class PhysicsObject extends SectorContainedObject {

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position the position of the object relative to the sector.
   */
  public PhysicsObject(SectorContainedPos position) {
    super(position);
  }

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position            the position of the object relative to the sector.
   * @param velocity            the velocity of the object.
   * @param acceleration        the acceleration of the object.
   * @param rotation            the rotation of the object.
   * @param angularVelocity     the angular velocity of the object.
   * @param angularAcceleration the angular acceleration of the object.
   */
  public PhysicsObject(SectorContainedPos position, SectorContainedVec velocity,
                       SectorContainedVec acceleration, SectorContainedPos rotation,
                       SectorContainedVec angularVelocity, SectorContainedVec angularAcceleration) {
    super(position, velocity, acceleration, rotation, angularVelocity, angularAcceleration);
  }
}
