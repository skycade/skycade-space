package net.skycade.space.model.physics.object;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.skycade.serverruntime.api.space.GameSpace;
import net.skycade.space.model.physics.PhysicsObject;
import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.physics.vector.SectorContainedVec;
import net.skycade.space.space.SpaceShipSpace;
import net.skycade.space.space.SpaceShipSpaceConstants;

/**
 * Represents a star in a sector.
 */
public class SectorStar extends PhysicsObject {

  private final BigDecimal radius;

  /**
   * Draws the star.
   *
   * @param position position of the star.
   * @param radius   radius of the star.
   */
  public SectorStar(SectorContainedPos position, BigDecimal radius) {
    super(position);
    this.radius = radius;
  }

  @Override
  public void draw(SpaceShipSpace space) {
    // all objects are drawn in the minecraft world on the surface of a sphere with a radius of 100
    // meters

    // objective: take the position of the object in the sector and convert it to a position
    // on the surface of the 'draw sphere'
    // then, calculate the radius of the object on the surface of the 'draw sphere'

    SectorContainedPos shipPosition = space.getSpaceShipReference().getPosition();
    SectorContainedPos shipRotation = space.getSpaceShipReference().getRotation();

    BigDecimal drawSphereRadius = SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS;
    BigDecimal distanceFromShipToStar = this.getPosition().distance(shipPosition);

    // calculate the radius of the object on the surface of the 'draw sphere'
    BigDecimal radiusOnDrawSphere = this.radius.multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);

    // calculate the position of the object on the surface of the 'draw sphere'
    // remember, the position of the object is constrained to the SURFACE of the 'draw sphere'
    SectorContainedPos relativePosition = this.getPosition().sub(shipPosition);

    BigDecimal xOnDrawSphere = relativePosition.x().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);
    BigDecimal yOnDrawSphere = relativePosition.y().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);
    BigDecimal zOnDrawSphere = relativePosition.z().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);

    // rotate everything on the surface of the 'draw sphere' by the rotation of the ship
    // this is so that the ship can rotate and the objects will rotate with it
    // we need trigonometry for this since we will use yaw, pitch and roll
    // top-down view:
    // forwards: -z
    //       -z
    //       |
    // -x  ------ +x
    //       |
    //       +z
    //

    // yaw: rotation around the y-axis
    // pitch: rotation around the x-axis
    // roll: rotation around the z-axis

    // yaw
    BigDecimal yaw = shipRotation.y();
    BigDecimal yawCos = BigDecimal.valueOf(Math.cos(yaw.doubleValue()));
    BigDecimal yawSin = BigDecimal.valueOf(Math.sin(yaw.doubleValue()));

    BigDecimal xOnDrawSphereYaw =
        xOnDrawSphere.multiply(yawCos).subtract(zOnDrawSphere.multiply(yawSin));
    BigDecimal zOnDrawSphereYaw =
        xOnDrawSphere.multiply(yawSin).add(zOnDrawSphere.multiply(yawCos));

    // pitch
    BigDecimal pitch = shipRotation.x();
    BigDecimal pitchCos = BigDecimal.valueOf(Math.cos(pitch.doubleValue()));
    BigDecimal pitchSin = BigDecimal.valueOf(Math.sin(pitch.doubleValue()));

    BigDecimal yOnDrawSpherePitch =
        yOnDrawSphere.multiply(pitchCos).subtract(zOnDrawSphereYaw.multiply(pitchSin));
    BigDecimal zOnDrawSpherePitch =
        yOnDrawSphere.multiply(pitchSin).add(zOnDrawSphereYaw.multiply(pitchCos));

    // roll
    BigDecimal roll = shipRotation.z();
    BigDecimal rollCos = BigDecimal.valueOf(Math.cos(roll.doubleValue()));
    BigDecimal rollSin = BigDecimal.valueOf(Math.sin(roll.doubleValue()));

    BigDecimal xOnDrawSphereRoll =
        xOnDrawSphereYaw.multiply(rollCos).subtract(yOnDrawSpherePitch.multiply(rollSin));
    BigDecimal yOnDrawSphereRoll =
        xOnDrawSphereYaw.multiply(rollSin).add(yOnDrawSpherePitch.multiply(rollCos));

    // now we have the position of the object on the surface of the 'draw sphere' and the radius
    // of the object on the surface of the 'draw sphere'
    // we can now draw the object
    Pos starCenterInWorldAbsolute =
        new Pos(xOnDrawSphereRoll.doubleValue(), yOnDrawSphereRoll.doubleValue(),
            zOnDrawSpherePitch.doubleValue());
    Pos starCenterInWorldRelativeToCenterOfShip =
        starCenterInWorldAbsolute.add(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP);

    // if the star in the sector is more than 10,000,000,000 meters away from the ship, just draw a
    // small dot/circle/whatever
    if (distanceFromShipToStar.compareTo(new BigDecimal("10000000000")) > 0) {
      drawNonRenderedStar(space, starCenterInWorldRelativeToCenterOfShip);
      return;
    }

    drawStar(space, starCenterInWorldRelativeToCenterOfShip, radiusOnDrawSphere.doubleValue());
  }

  @Override
  public void tickCustomPhysics() {
    // no-op
  }

  private void drawNonRenderedStar(SpaceShipSpace space, Pos starCenterInWorld) {
    // get the directional velocity of the ship
    BigDecimal shipVelocity = space.getSpaceShipReference().getVelocity().length();

    // if the velocity is greater than the speed of light,
    // draw "hyperspace" lines in the direction of the ship's velocity
    if (shipVelocity.compareTo(new BigDecimal("299792458")) > 0) {
      drawHyperspaceLines(space, starCenterInWorld, shipVelocity);
      return;
    }

    drawParticle(space, starCenterInWorld);
  }

  /**
   * Draws hyperspace lines
   *
   * @param space             space
   * @param starCenterInWorld star center in world
   * @param shipVelocity      ship velocity
   */
  public void drawHyperspaceLines(SpaceShipSpace space, Pos starCenterInWorld,
                                  BigDecimal shipVelocity) {
    // get the direction of the ship's velocity
    SectorContainedVec shipVelocityDirection =
        space.getSpaceShipReference().getVelocity().div(shipVelocity);

    // in the position of a star, draw a stretched line in the direction of the ship's velocity
    // to make it look like the star is moving in hyperspace

    // transform ship velocity length to a length that is visible on the screen
    BigDecimal transformedShipVelocity =
        shipVelocity.divide(new BigDecimal("1000000000000"), 10, RoundingMode.HALF_UP);
    SectorContainedVec transformedShipVelocityDirection =
        shipVelocityDirection.mul(transformedShipVelocity);

    Pos end = starCenterInWorld.add(new Pos(transformedShipVelocityDirection.x().doubleValue(),
        transformedShipVelocityDirection.y().doubleValue(),
        transformedShipVelocityDirection.z().doubleValue()));

    // draw the line, split into 200 segments
    for (int i = 0; i < 200; i++) {
      double progress = (double) i / 200;
      Pos currentPos = starCenterInWorld.add(end.sub(starCenterInWorld).mul(progress));

      // distance from the particle to the ship
      double distanceFromParticleToShip =
          currentPos.sub(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP).asVec().length();

      if (distanceFromParticleToShip == 0) {
        continue;
      }

      // draw the particle
      drawParticle(space, translateRelativeOriginWorldPointToDrawSphereBound(currentPos,
          BigDecimal.valueOf(distanceFromParticleToShip), starCenterInWorld));
    }
  }

  /**
   * Draws a star in the minecraft world.
   *
   * @param space                           the game space
   * @param starCenterInWorldRelativeToShip the center of the star in the minecraft world relative to the center of
   *                                        the ship
   * @param radiusOfStarOnDrawSphereSurface the radius of the star on the surface of the 'draw sphere'
   */
  private void drawStar(SpaceShipSpace space, Pos starCenterInWorldRelativeToShip,
                        double radiusOfStarOnDrawSphereSurface) {
    int particleCount = 7000;

    // now we draw a 3D sphere out of particles, but we have to squeeze it into a 2D circle
    // constrained on the SURFACE of the 'draw sphere'

    for (int i = 0; i < particleCount; i++) {
      Pos pointOn3DStarAroundOrigin =
          getPredeterminedPointOnSphereSurfaceGivenIndex(particleCount, i,
              radiusOfStarOnDrawSphereSurface);

      // calculate the distance from the ship to the point on the 3D star surface
      BigDecimal distanceFromShipToPointOn3DStarSurface = BigDecimal.valueOf(
          pointOn3DStarAroundOrigin.add(starCenterInWorldRelativeToShip)
              .distance(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP));

      // if the particle isn't visible from the ship, don't draw it
      if (distanceFromShipToPointOn3DStarSurface.compareTo(
          SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS) > 0) {
        continue;
      }

      Pos radiusBoundParticlePos =
          translateRelativeOriginWorldPointToDrawSphereBound(pointOn3DStarAroundOrigin,
              distanceFromShipToPointOn3DStarSurface, starCenterInWorldRelativeToShip);
//      // calculate the position of the particle on the 3D star that is centered around the surf
//      // of the 'draw sphere'
//      BigDecimal xOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.x())
//          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
//          .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
//      BigDecimal yOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.y())
//          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
//          .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
//      BigDecimal zOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.z())
//          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
//          .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);


//      Pos particlePos = new Pos(xOnDrawSphereAdjusted.doubleValue(),
//          yOnDrawSphereAdjusted.doubleValue(), zOnDrawSphereAdjusted.doubleValue())
//          .add(starCenterInWorldRelativeToShip);

      drawParticle(space, radiusBoundParticlePos);
    }
  }

  private Pos translateRelativeOriginWorldPointToDrawSphereBound(Pos pointOn3DStarAroundOrigin,
                                                                 BigDecimal distanceFromShipToPointOn3DStarSurface,
                                                                 Pos starCenterInWorldRelativeToShip) {
    // calculate the position of the particle on the 3D star that is centered around the surf
    // of the 'draw sphere'
    BigDecimal xOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.x())
        .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
        .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
    BigDecimal yOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.y())
        .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
        .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
    BigDecimal zOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.z())
        .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
        .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);

    // this particle position is in 3D relative to the surface of the 'draw sphere' (pops out)
    Pos particlePos = new Pos(xOnDrawSphere.doubleValue(), yOnDrawSphere.doubleValue(),
        zOnDrawSphere.doubleValue()).add(starCenterInWorldRelativeToShip);
    // this particle position is in 2D relative to the surface of the 'draw sphere' (squished)
    Pos radiusBoundParticlePos =
        particlePos.mul(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS.doubleValue())
            .div(particlePos.distance(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP));

    return radiusBoundParticlePos;
  }

  private Pos getPredeterminedPointOnSphereSurfaceGivenIndex(int numOfParticles, int i,
                                                             double radius) {
    double theta = Math.acos(-1.0 + (2.0 * i) / numOfParticles);
    double phi = Math.sqrt(numOfParticles * Math.PI) * theta;
    double x = Math.cos(phi) * Math.sin(theta) * radius;
    double y = Math.sin(phi) * Math.sin(theta) * radius;
    double z = Math.cos(theta) * radius;
    return new Pos(x, y, z);
  }

  /**
   * Draws a particle at the specified position.
   *
   * @param space        the space to draw the particle in.
   * @param minecraftPos the position to draw the particle at.
   */
  private void drawParticle(GameSpace space, Pos minecraftPos) {
    // todo: draw a particle at the given position.
    ParticlePacket particlePacket =
        ParticleCreator.createParticlePacket(Particle.FIREWORK, minecraftPos.x(), minecraftPos.y(),
            minecraftPos.z(), 0, 0, 0, 1);
    space.sendGroupedPacket(particlePacket);
  }
}
