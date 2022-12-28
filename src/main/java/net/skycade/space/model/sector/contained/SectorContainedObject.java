package net.skycade.space.model.sector.contained;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.skycade.space.constants.PhysicsAndRenderingConstants;
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
   * The rotation of the object.
   */
  private SectorContainedPos rotation;

  /**
   * The velocity of the rotation of the object.
   */
  private SectorContainedVec angularVelocity;

  /**
   * The acceleration of the rotation of the object.
   */
  private SectorContainedVec angularAcceleration;

  /**
   * The time at which the object was last updated.
   */
  private long lastTick;

  /**
   * Construct a new {@link SectorContainedObject}.
   *
   * @param position the position of the object relative to the sector.
   */
  public SectorContainedObject(SectorContainedPos position) {
    this.position = position;
    this.acceleration = SectorContainedVec.ZERO;
    this.velocity = SectorContainedVec.ZERO;
    this.rotation = SectorContainedPos.ZERO;
    this.angularAcceleration = SectorContainedVec.ZERO;
    this.angularVelocity = SectorContainedVec.ZERO;
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
  public SectorContainedObject(SectorContainedPos position, SectorContainedVec velocity,
                               SectorContainedVec acceleration, SectorContainedPos rotation,
                               SectorContainedVec angularVelocity,
                               SectorContainedVec angularAcceleration) {
    this.position = position;
    this.velocity = velocity;
    this.acceleration = acceleration;
    this.rotation = rotation;
    this.angularVelocity = angularVelocity;
    this.angularAcceleration = angularAcceleration;
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
   * Get the rotation of the ship.
   *
   * @return the rotation of the ship.
   */
  public SectorContainedPos getRotation() {
    return this.rotation;
  }

  /**
   * Set the rotation of the ship.
   *
   * @param rotation the rotation of the ship.
   */
  public void setRotation(SectorContainedPos rotation) {
    this.rotation = rotation;
  }

  /**
   * Get the velocity of the rotation of the ship.
   *
   * @return the velocity of the rotation of the ship.
   */
  public SectorContainedVec getAngularVelocity() {
    return this.angularVelocity;
  }

  /**
   * Set the velocity of the rotation of the ship.
   *
   * @param angularVelocity the velocity of the rotation of the ship.
   */
  public void setAngularVelocity(SectorContainedVec angularVelocity) {
    this.angularVelocity = angularVelocity;
  }

  /**
   * Get the acceleration of the rotation of the ship.
   *
   * @return the acceleration of the rotation of the ship.
   */
  public SectorContainedVec getAngularAcceleration() {
    return this.angularAcceleration;
  }

  /**
   * Set the acceleration of the rotation of the ship.
   *
   * @param angularAcceleration the acceleration of the rotation of the ship.
   */
  public void setAngularAcceleration(SectorContainedVec angularAcceleration) {
    this.angularAcceleration = angularAcceleration;
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
    Long physicsTickDelayMillis = PhysicsAndRenderingConstants.PHYSICS_DELAY_MILLIS;

    // tick the physics to be constant to meters per second using the physics tick delay
    // as the time between ticks and "meters/second" as the unit of velocity
    long currentTime = System.currentTimeMillis();
    long timeSinceLastTick = currentTime - lastTick;

    // calculate the velocity of the object in meters per second using the acceleration
    // and the time since the last tick
    BigDecimal velocityX = velocity.x().add(acceleration.y().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal velocityY = velocity.y().add(acceleration.y().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal velocityZ = velocity.z().add(acceleration.z().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));

    this.setVelocity(new SectorContainedVec(velocityX, velocityY, velocityZ));

    // calculate the position of the object in meters using the velocity and the time since
    // the last tick
    BigDecimal positionX = position.x().add(velocity.x().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal positionY = position.y().add(velocity.y().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal positionZ = position.z().add(velocity.z().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));

    this.setPosition(new SectorContainedPos(positionX, positionY, positionZ));

    // calculate the angular velocity of the object in radians per second using the angular
    // acceleration and the time since the last tick
    BigDecimal angularVelocityX = angularVelocity.x().add(angularAcceleration.y().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal angularVelocityY = angularVelocity.y().add(angularAcceleration.y().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal angularVelocityZ = angularVelocity.z().add(angularAcceleration.z().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));

    this.setAngularVelocity(
        new SectorContainedVec(angularVelocityX, angularVelocityY, angularVelocityZ));

    // calculate the rotation of the object in radians using the angular velocity and the time
    // since the last tick
    BigDecimal rotationX = rotation.x().add(angularVelocity.x().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal rotationY = rotation.y().add(angularVelocity.y().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));
    BigDecimal rotationZ = rotation.z().add(angularVelocity.z().multiply(
        BigDecimal.valueOf(timeSinceLastTick)
            .divide(BigDecimal.valueOf(physicsTickDelayMillis), 10, RoundingMode.HALF_UP)));

    this.setRotation(new SectorContainedPos(rotationX, rotationY, rotationZ));

//    this.setVelocity(this.velocity.add(this.acceleration));
//    this.setPosition(this.position.add(this.velocity));

//    this.setAngularVelocity(this.angularVelocity.add(this.angularAcceleration));
//    this.setRotation(this.rotation.add(this.angularVelocity));

    this.lastTick = currentTime;
  }
}
