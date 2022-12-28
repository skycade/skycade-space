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
import net.skycade.space.space.SpaceShipSpace;
import net.skycade.space.space.SpaceShipSpaceConstants;

/**
 * Represents a planet in a sector.
 */
public class SectorPlanet extends PhysicsObject {

  private final BigDecimal radius;

  /**
   * Draws the planet.
   *
   * @param position position of the planet.
   * @param radius   radius of the planet.
   */
  public SectorPlanet(SectorContainedPos position, BigDecimal radius) {
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
    BigDecimal distanceFromShipToPlanet = this.getPosition().distance(shipPosition);

    // calculate the radius of the object on the surface of the 'draw sphere'
    BigDecimal radiusOnDrawSphere = this.radius.multiply(drawSphereRadius)
        .divide(distanceFromShipToPlanet, 10, RoundingMode.HALF_UP);

    // calculate the position of the object on the surface of the 'draw sphere'
    // remember, the position of the object is constrained to the SURFACE of the 'draw sphere'
    SectorContainedPos relativePosition = this.getPosition().sub(shipPosition);

    BigDecimal xOnDrawSphere = relativePosition.x().multiply(drawSphereRadius)
        .divide(distanceFromShipToPlanet, 10, RoundingMode.HALF_UP);
    BigDecimal yOnDrawSphere = relativePosition.y().multiply(drawSphereRadius)
        .divide(distanceFromShipToPlanet, 10, RoundingMode.HALF_UP);
    BigDecimal zOnDrawSphere = relativePosition.z().multiply(drawSphereRadius)
        .divide(distanceFromShipToPlanet, 10, RoundingMode.HALF_UP);

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
    Pos planetCenterInWorldAbsolute =
        new Pos(xOnDrawSphereRoll.doubleValue(), yOnDrawSphereRoll.doubleValue(),
            zOnDrawSpherePitch.doubleValue());
    Pos planetCenterInWorldRelativeToCenterOfShip =
        planetCenterInWorldAbsolute.add(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP);

    // if the planet in the sector is more than 1,000,000,000 meters away from the ship, just draw a
    // small dot/circle/whatever
    if (distanceFromShipToPlanet.compareTo(new BigDecimal("1000000000")) > 0) {
      drawNonRenderedPlanet(space, planetCenterInWorldRelativeToCenterOfShip);
      return;
    }

    // since we are drawing the planet in the minecraft world, we need to provide the draw method
    // with the x angle and y angle of the planet on the surface of the 'draw sphere' from the
    // perspective of the ship
    double xAngle = Math.atan2(planetCenterInWorldAbsolute.x(), planetCenterInWorldAbsolute.z());
    double yAngle = Math.atan2(planetCenterInWorldAbsolute.y(), planetCenterInWorldAbsolute.z());

    drawPlanet(space, planetCenterInWorldRelativeToCenterOfShip, radiusOnDrawSphere.doubleValue(),
        xAngle, yAngle);
  }

  private void drawNonRenderedPlanet(GameSpace space, Pos planetCenterInWorld) {
    // make a little circle of particles
    for (int i = 0; i < 5; i++) {
      getPredeterminedPointOnSphereSurfaceGivenIndex(5, i, 0.2);
      Pos particlePos =
          getPredeterminedPointOnSphereSurfaceGivenIndex(5, i, 0.2).add(planetCenterInWorld);
      drawParticle(space, particlePos);
    }
  }

  /**
   * Draws a planet in the minecraft world.
   *
   * @param space                           the game space
   * @param planetCenterInWorldRelativeToShip the center of the planet in the minecraft world relative to the center of
   *                                        the ship
   * @param radiusOfPlanetOnDrawSphereSurface the radius of the planet on the surface of the 'draw sphere'
   * @param xAngle                          the x angle of the planet on the surface of the 'draw sphere' from the
   *                                        perspective of the ship
   * @param yAngle                          the y angle of the planet on the surface of the 'draw sphere' from the
   *                                        perspective of the ship
   */
  private void drawPlanet(SpaceShipSpace space, Pos planetCenterInWorldRelativeToShip,
                        double radiusOfPlanetOnDrawSphereSurface, double xAngle, double yAngle) {
    int particleCount = 7000;

    // todo: use xAngle and yAngle to do perspective math and squash the 3D sphere
    // into a 2D circle that matches perspective of the ship (even with changing distance)

    // now we draw a 3D sphere out of particles, but we have to squeeze it into a 2D circle
    // constrained on the SURFACE of the 'draw sphere'

    for (int i = 0; i < particleCount; i++) {
      Pos pointOn3DPlanetAroundOrigin =
          getPredeterminedPointOnSphereSurfaceGivenIndex(particleCount, i,
              radiusOfPlanetOnDrawSphereSurface);

      // calculate the distance from the ship to the point on the 3D planet surface
      BigDecimal distanceFromShipToPointOn3DPlanetSurface = BigDecimal.valueOf(
          pointOn3DPlanetAroundOrigin.add(planetCenterInWorldRelativeToShip)
              .distance(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP));

      // if the particle isn't visible from the ship, don't draw it
      if (distanceFromShipToPointOn3DPlanetSurface.compareTo(
          SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS) > 0) {
        continue;
      }

      // calculate the position of the particle on the 3D planet that is centered around the surf
      // of the 'draw sphere'
      BigDecimal xOnDrawSphere = BigDecimal.valueOf(pointOn3DPlanetAroundOrigin.x())
          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
          .divide(distanceFromShipToPointOn3DPlanetSurface, 10, RoundingMode.HALF_UP);
      BigDecimal yOnDrawSphere = BigDecimal.valueOf(pointOn3DPlanetAroundOrigin.y())
          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
          .divide(distanceFromShipToPointOn3DPlanetSurface, 10, RoundingMode.HALF_UP);
      BigDecimal zOnDrawSphere = BigDecimal.valueOf(pointOn3DPlanetAroundOrigin.z())
          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
          .divide(distanceFromShipToPointOn3DPlanetSurface, 10, RoundingMode.HALF_UP);

      Pos particlePos = new Pos(xOnDrawSphere.doubleValue(), yOnDrawSphere.doubleValue(),
          zOnDrawSphere.doubleValue()).add(planetCenterInWorldRelativeToShip);


      drawParticle(space, particlePos);
    }
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
        ParticleCreator.createParticlePacket(Particle.CRIT, minecraftPos.x(), minecraftPos.y(),
            minecraftPos.z(), 0, 0, 0, 1);
    space.sendGroupedPacket(particlePacket);
  }
}
