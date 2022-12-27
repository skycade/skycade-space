package net.skycade.space.model.sector.contained;

import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.physics.vector.SectorContainedVec;
import net.skycade.space.space.SpaceShipSpace;

/**
 * Represents an object that is contained within a sector.
 * <p>
 * This is used to represent objects that are contained within a sector, such as
 * a planet or any other static object that has a position within a sector.
 * <p>
 * The position of the object is represented by a {@link SectorContainedPos}, which is RELATIVE
 * to the sector, not the universe.
 *
 * @author Jacob Cohen
 */
public abstract class SectorContainedObject {

  /**
   * The position of the object relative to the sector.
   */
  private SectorContainedPos position;

  /**
   * The velocity of the object.
   */
  private SectorContainedVec velocity;

  /**
   * The acceleration of the object.
   */
  private SectorContainedVec acceleration;

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position the position of the object relative to the sector.
   */
  public SectorContainedObject(SectorContainedPos position) {
    this.position = position;
    this.acceleration = SectorContainedVec.ZERO;
    this.velocity = SectorContainedVec.ZERO;
  }

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position     the position of the object relative to the sector.
   * @param velocity     the velocity of the object.
   * @param acceleration the acceleration of the object.
   */
  public SectorContainedObject(SectorContainedPos position, SectorContainedVec velocity,
                               SectorContainedVec acceleration) {
    this.position = position;
    this.velocity = velocity;
    this.acceleration = acceleration;
  }

  /**
   * Get the position of the object relative to the sector.
   *
   * @return the position of the object relative to the sector.
   */
  public SectorContainedPos getPosition() {
    return position;
  }

  /**
   * Set the position of the object relative to the sector.
   *
   * @param position the position of the object relative to the sector.
   */
  public void setPosition(SectorContainedPos position) {
    this.position = position;
  }

  /**
   * Gets the velocity of the object.
   *
   * @return the velocity of the object.
   */
  public SectorContainedVec getVelocity() {
    return velocity;
  }

  /**
   * Sets the velocity of the object.
   *
   * @param velocity the new velocity of the object.
   */
  public void setVelocity(SectorContainedVec velocity) {
    this.velocity = velocity;
  }

  /**
   * Gets the acceleration of the object.
   *
   * @return the acceleration of the object.
   */
  public SectorContainedVec getAcceleration() {
    return acceleration;
  }

  /**
   * Sets the acceleration of the object.
   *
   * @param acceleration the new acceleration of the object.
   */
  public void setAcceleration(SectorContainedVec acceleration) {
    this.acceleration = acceleration;
  }

  /**
   * Draw the object.
   *
   * @param space the spaceship space.
   */
  public abstract void draw(SpaceShipSpace space);

  /**
   * Update the object's position based on its velocity and acceleration.
   */
  public void tickPhysics() {
    this.setVelocity(this.velocity.add(this.acceleration));
    this.setPosition(this.position.add(this.velocity));
  }
}
