package net.skycade.space.renderer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.skycade.serverruntime.api.space.GameSpace;
import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.sector.contained.SectorContainedObject;
import net.skycade.space.model.sector.contained.SectorSpaceShip;
import net.skycade.space.space.SpaceShipSpace;
import net.skycade.space.space.SpaceShipSpaceConstants;

public class SectorRenderer {

  /**
   * The spaceship space.
   * <p>
   * This is constrained to a spaceship space because the sector renderer
   * is only used in spaceship spaces.
   * <p>
   * Why else would you render a sector? You wouldn't, unless you're travelling through it,
   * which is only possible in a....spaceship. Wow, I'm so smart.
   */
  private final SpaceShipSpace space;

  /**
   * Constructs a new sector renderer.
   *
   * @param space the spaceship space.
   */
  public SectorRenderer(SpaceShipSpace space) {
    this.space = space;
  }

  /**
   * Renders the sector around the spaceship.
   */
  public void render() {
    List<Pos> particles = new ArrayList<>();
    // loop through all the sectors objects in the sector
    // and render them.
    for (SectorContainedObject object : space.getSector().getContainedObjects()) {
      // render the object.
      if (object instanceof SectorSpaceShip) {
        // don't render the ship.
        continue;
      }
      Pos absoluteDrawSphereRadiusBoundObjectCenter =
          transformUniverseObjectPositionCenterToDrawSphereAbsolutePosition(object.getPosition());
      Pos[] positions = object.draw(space, absoluteDrawSphereRadiusBoundObjectCenter);
      particles.addAll(List.of(positions));
    }

    Collections.shuffle(particles);

    for (int i = 0; i < particles.size(); i++) {
      Pos particle = particles.get(i);
      int drawn = i * 2;

      // if last element
      if (i == particles.size() - 1 && drawn < 8000) {
        drawParticle(space,
            translateDrawCircle3DPointToRadiusBoundPerspective2DPoint(particle), 8000 - drawn);
        break;
      }

      if (drawn >= 8000) {
        break;
      }

      drawParticle(space,
          translateDrawCircle3DPointToRadiusBoundPerspective2DPoint(particle), 2);
    }
  }

  private Pos randomPositionOnSurfaceOfSphere(double radius) {
    // random point on the surface of the sphere
    double theta = Math.random() * 2 * Math.PI;
    double phi = Math.random() * Math.PI;

    double x = radius * Math.sin(phi) * Math.cos(theta);
    double y = radius * Math.sin(phi) * Math.sin(theta);
    double z = radius * Math.cos(phi);

    return new Pos(x, y, z);
  }

  /**
   * Translates a point that is in 3 dimensional space around the draw circle radius
   * to a point that is in 2 dimensional space, which is bound
   * to the radius of the draw circle; making it 2D.
   *
   * @param drawCircleBound3DPoint the point to translate.
   * @return the translated point.
   */
  private Pos translateDrawCircle3DPointToRadiusBoundPerspective2DPoint(
      Pos drawCircleBound3DPoint) {

    BigDecimal distanceFromShipToPointOn3DStarSurface = BigDecimal.valueOf(
        drawCircleBound3DPoint.distance(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP));

    // calculate the position of the particle on the 3D star that is centered around the surf
    // of the 'draw sphere'
    BigDecimal xOnDrawSphere = BigDecimal.valueOf(drawCircleBound3DPoint.x())
        .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
        .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
    BigDecimal yOnDrawSphere = BigDecimal.valueOf(drawCircleBound3DPoint.y())
        .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
        .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);
    BigDecimal zOnDrawSphere = BigDecimal.valueOf(drawCircleBound3DPoint.z())
        .multiply(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS)
        .divide(distanceFromShipToPointOn3DStarSurface, 10, RoundingMode.HALF_UP);

    // this particle position is in 3D relative to the surface of the 'draw sphere' (pops out)
    Pos particlePos = new Pos(xOnDrawSphere.doubleValue(), yOnDrawSphere.doubleValue(),
        zOnDrawSphere.doubleValue());

    // this particle position is in 2D relative to the surface of the 'draw sphere' (squished)
    return particlePos.mul(SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS.doubleValue())
        .div(particlePos.distance(SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP));
  }

  /**
   * Draws a particle at the specified position.
   *
   * @param space        the space to draw the particle in.
   * @param minecraftPos the position to draw the particle at.
   * @param count        the amount of particles to draw.
   */
  private void drawParticle(GameSpace space, Pos minecraftPos, int count) {
    ParticlePacket particlePacket =
        ParticleCreator.createParticlePacket(Particle.CRIT, minecraftPos.x(), minecraftPos.y(),
            minecraftPos.z(), 0, 0, 0, count);
    space.sendGroupedPacket(particlePacket);
  }

  /**
   * Transforms a 'universal' position to a position that is relative to the origin of the world,
   * from the perspective of the origin on the draw circle (the position is the center of the object,
   * sitting on the draw sphere's radius).
   *
   * @param universeObjectPositionCenter the position to transform.
   * @return the transformed position.
   */
  private Pos transformUniverseObjectPositionCenterToDrawSphereAbsolutePosition(
      SectorContainedPos universeObjectPositionCenter) {
    // objective: take the position of the object in the sector and convert it to a position
    // on the surface of the 'draw sphere'
    // then, calculate the radius of the object on the surface of the 'draw sphere'

    SectorContainedPos shipPosition = space.getSpaceShipReference().getPosition();
    SectorContainedPos shipRotation = space.getSpaceShipReference().getRotation();

    BigDecimal drawSphereRadius = SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS;
    BigDecimal distanceFromShipToStar = universeObjectPositionCenter.distance(shipPosition);

    // calculate the position of the object on the surface of the 'draw sphere'
    // remember, the position of the object is constrained to the SURFACE of the 'draw sphere'
    SectorContainedPos relativePosition = universeObjectPositionCenter.sub(shipPosition);

    BigDecimal xOnDrawSphere = relativePosition.x().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);
    BigDecimal yOnDrawSphere = relativePosition.y().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);
    BigDecimal zOnDrawSphere = relativePosition.z().multiply(drawSphereRadius)
        .divide(distanceFromShipToStar, 10, RoundingMode.HALF_UP);

    // rotate everything on the surface of the 'draw sphere' by the rotation of the ship
    // this is so that the ship can rotate and the objects will rotate with it,
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
    return new Pos(xOnDrawSphereRoll.doubleValue(), yOnDrawSphereRoll.doubleValue(),
        zOnDrawSpherePitch.doubleValue());
  }
}
