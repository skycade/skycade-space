package net.skycade.space.model.sector.contained;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.minestom.server.timer.Task;
import net.skycade.space.constants.PhysicsAndRenderingConstants;
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

  @Override
  public void tickCustomPhysics() {
    // no-op
  }

  /**
   * Adds a thrust force to the ship in the direction it is facing, with the given magnitude and
   * duration.
   *
   * @param magnitude the magnitude of the thrust.
   * @param seconds   the duration of the thrust, in seconds.
   */

  public void thrustForward(BigDecimal magnitude, double seconds, SpaceShipSpace space) {
    // add a force in the direction of the ship's rotation, with the given magnitude, for the given
    // duration.
    AtomicReference<SectorContainedVec> previousAcceleration = new AtomicReference<>(null);

    // create a task that repeats every physics tick delay, for the given duration.
    Task thrustTask = space.scheduler().buildTask(() -> {
      // get the current rotation
      SectorContainedPos currentRotation = this.getRotation();

      if (previousAcceleration.get() != null) {
        // remove the previous acceleration
        this.setAcceleration(this.getAcceleration().sub(previousAcceleration.get()));
      }

      // rotation x: pitch, y: yaw, z: roll
      // given the magnitude, we can calculate the x and y, and z components of the new acceleration.
      // -z is forward, +z is backward.
      // -x is left, +x is right.
      // -y is up, +y is down.

      // get the angle between the ship's rotation and the standard rotation (0, 0, 0).
      // (uses trig)
      BigDecimal yawAngle = currentRotation.y();
      BigDecimal pitchAngle = currentRotation.x();


      // push the angles between 0 and 2pi.
      if (yawAngle.compareTo(BigDecimal.ZERO) < 0) {
        yawAngle = yawAngle.add(BigDecimal.valueOf(2 * Math.PI));
      }

      // if it's greater than 2pi, subtract 2pi repeatedly until it's less than 2pi.
      while (yawAngle.compareTo(BigDecimal.valueOf(2 * Math.PI)) >= 0) {
        yawAngle = yawAngle.subtract(BigDecimal.valueOf(2 * Math.PI));
      }

      if (pitchAngle.compareTo(BigDecimal.ZERO) < 0) {
        pitchAngle = pitchAngle.add(BigDecimal.valueOf(2 * Math.PI));
      }

      while (pitchAngle.compareTo(BigDecimal.valueOf(2 * Math.PI)) >= 0) {
        pitchAngle = pitchAngle.subtract(BigDecimal.valueOf(2 * Math.PI));
      }

      // get the x, y, and z components of the acceleration in relation to the standard grid.

      // x component
      BigDecimal xComponent =
          magnitude.multiply(BigDecimal.valueOf(Math.cos(yawAngle.doubleValue())))
              .multiply(BigDecimal.valueOf(-1));
      // y component
      BigDecimal verticalComponent =
          magnitude.multiply(BigDecimal.valueOf(Math.sin(pitchAngle.doubleValue())));
      // z component
      BigDecimal zComponent =
          magnitude.multiply(BigDecimal.valueOf(Math.sin(yawAngle.doubleValue())))
              .multiply(BigDecimal.valueOf(-1));

      SectorContainedVec newAcceleration =
          new SectorContainedVec(zComponent, verticalComponent, xComponent);

      previousAcceleration.set(newAcceleration);
      this.setAcceleration(this.getAcceleration().add(newAcceleration));

    }).repeat(Duration.ofMillis(PhysicsAndRenderingConstants.PHYSICS_DELAY_MILLIS)).schedule();
    // after the duration has passed, cancel the task.
    space.scheduler().buildTask(() -> {
      thrustTask.cancel();
      if (previousAcceleration.get() != null) {
        this.setAcceleration(this.getAcceleration().sub(previousAcceleration.get()));
      }
    }).delay(Duration.ofMillis((long) (seconds * 1000))).schedule();
  }
}
