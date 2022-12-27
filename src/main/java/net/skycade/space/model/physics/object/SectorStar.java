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

    SectorContainedPos shipPosition = space.getShipPosition();
    BigDecimal drawSphereRadius = SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS;
    BigDecimal starRadius = this.radius;
    BigDecimal distanceFromShipToStar = this.getPosition().distance(shipPosition);

    // calculate the radius of the object on the surface of the 'draw sphere'
    BigDecimal radiusOnDrawSphere = starRadius.multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);

    // calculate the position of the object on the surface of the 'draw sphere'
    // remember, the position of the object is constrained to the SURFACE of the 'draw sphere'
    BigDecimal xOnDrawSphere = this.getPosition().x().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);
    BigDecimal yOnDrawSphere = this.getPosition().y().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);
    BigDecimal zOnDrawSphere = this.getPosition().z().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);

    Pos starCenterInWorld = new Pos(xOnDrawSphere.doubleValue(), yOnDrawSphere.doubleValue(),
        zOnDrawSphere.doubleValue()).add(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP);

    // if the star in the sector is more than 100,000,000 meters away from the ship, just draw a
    // small dot/circle/whatever
    if (distanceFromShipToStar.compareTo(new BigDecimal("100000000")) > 0) {
      drawNonRenderedStar(space, starCenterInWorld);
      return;
    }

    drawStar(space, starCenterInWorld, radiusOnDrawSphere.doubleValue());
  }

  private void drawNonRenderedStar(GameSpace space, Pos starCenterInWorld) {
    // make a little circle of particles
    for (int i = 0; i < 5; i++) {
      getPredeterminedPointOnSphereSurfaceGivenIndex(5, i, 0.2);
      Pos particlePos =
          getPredeterminedPointOnSphereSurfaceGivenIndex(5, i, 0.2).add(starCenterInWorld);
      drawParticle(space, particlePos);
    }
  }

  private void drawStar(SpaceShipSpace space, Pos starCenterInWorld,
                        double radiusOfStarOnDrawSphereSurface) {
    int particleCount = 7000;

    // now we draw a 3D sphere out of particles, but we have to squeeze it into a 2D circle
    // constrained on the SURFACE of the 'draw sphere'

    for (int i = 0; i < particleCount; i++) {
      Pos pointOn3DStarAroundOrigin =
          getPredeterminedPointOnSphereSurfaceGivenIndex(particleCount, i,
              radiusOfStarOnDrawSphereSurface);

      // now we have to squeeze the 3D point into a 2D point constrained on the SURFACE of the 'draw
      // sphere'

      // calculate the distance from the ship to the point on the 3D star surface
      BigDecimal distanceFromShipToPointOn3DStarSurface = BigDecimal.valueOf(
          pointOn3DStarAroundOrigin.add(starCenterInWorld)
              .distance(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP));

      // if the particle isn't visible from the ship, don't draw it
      if (distanceFromShipToPointOn3DStarSurface.compareTo(
          SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS) > 0) {
        continue;
      }

      // calculate the position of the point on the 3D star surface on the surface of the 'draw
      // sphere'- squeeze it into a 2D point
      BigDecimal xOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.x())
          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
          .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
      BigDecimal yOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.y())
          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
          .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
      BigDecimal zOnDrawSphere = BigDecimal.valueOf(pointOn3DStarAroundOrigin.z())
          .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
          .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);

      Pos particlePos = new Pos(xOnDrawSphere.doubleValue(), yOnDrawSphere.doubleValue(),
          zOnDrawSphere.doubleValue()).add(starCenterInWorld);

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
        ParticleCreator.createParticlePacket(Particle.FIREWORK, minecraftPos.x(), minecraftPos.y(),
            minecraftPos.z(), 0, 0, 0, 1);
    space.sendGroupedPacket(particlePacket);
  }
}
