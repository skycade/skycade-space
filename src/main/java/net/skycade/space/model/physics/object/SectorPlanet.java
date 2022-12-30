package net.skycade.space.model.physics.object;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minestom.server.coordinate.Pos;
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

  /**
   * Draws the object.
   *
   * @param space                                     space to draw the object in.
   * @param absoluteDrawSphereRadiusBoundObjectCenter position of the object on the surface of the 'draw sphere',
   *                                                  transformed by the rotation of the ship and scaled to
   *                                                  the size of the 'draw sphere'; absolute position, not
   *                                                  relative to the ship.
   * @return the positions of the object.
   */
  @Override
  public Pos[] draw(SpaceShipSpace space, Pos absoluteDrawSphereRadiusBoundObjectCenter) {
    BigDecimal drawSphereRadius = SpaceShipSpaceConstants.DRAW_ON_CIRCLE_RADIUS;
    BigDecimal distanceFromShipToPlanet =
        this.getPosition().distance(space.getSpaceShipReference().getPosition());
    // calculate the radius of the object on the surface of the 'draw sphere' (since
    // the planet is a spherical object, we need the radius of it to render a sphere)
    BigDecimal radiusOfObjectOnDrawSphere = this.radius.multiply(drawSphereRadius)
        .divide(distanceFromShipToPlanet, 10, RoundingMode.HALF_UP);


    Pos planetCenterInWorldRelativeToCenterOfShip = absoluteDrawSphereRadiusBoundObjectCenter.add(
        SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP);

    // if the planet in the sector is more than 10,000,000,000 meters away from the ship, just draw a
    // small dot/circle/whatever
    if (distanceFromShipToPlanet.compareTo(new BigDecimal("10000000000")) > 0) {
      return drawNonRenderedPlanet(space, planetCenterInWorldRelativeToCenterOfShip).toArray(
          new Pos[0]);
    }

    return calculateParticlePositions3DBoundToDrawCircleRadius(space,
        planetCenterInWorldRelativeToCenterOfShip,
        radiusOfObjectOnDrawSphere.doubleValue()).toArray(
        new Pos[0]);
  }

  @Override
  public void tickCustomPhysics() {
    // no-op
  }

  private List<Pos> drawNonRenderedPlanet(SpaceShipSpace space, Pos planetCenterInWorld) {
    // get the directional velocity of the ship
    BigDecimal shipVelocity = space.getSpaceShipReference().getVelocity().length();

    // if the velocity is greater than the speed of light,
    // draw "hyperspace" lines in the direction of the ship's velocity
    if (shipVelocity.compareTo(new BigDecimal("10792458")) > 0) {
      // todo: generate a line that is scaled based on the velocity of the ship
      return calculateParticlePositionsHyperspace3DBoundToDrawCircleRadius(space,
          planetCenterInWorld);
    }

    return Collections.singletonList(planetCenterInWorld);
  }

  /**
   * Calculates the positions of the particles that make up the planet in hyperspace.
   *
   * @param space               space to draw the planet in.
   * @param planetCenterInWorld center of the planet in the world.
   * @return the positions of the particles that make up the planet.
   */
  private List<Pos> calculateParticlePositionsHyperspace3DBoundToDrawCircleRadius(
      SpaceShipSpace space, Pos planetCenterInWorld) {
    List<Pos> positions = new ArrayList<>();
    // the ship's velocity
    BigDecimal shipVelocity = space.getSpaceShipReference().getVelocity().length();
    // theta = vertical angle
    BigDecimal theta = space.getSpaceShipReference().getVelocity().theta();
    // phi = horizontal angle
    BigDecimal phi = space.getSpaceShipReference().getVelocity().phi();

    // calculate the length of the line based on the velocity of the ship
    // e.g., speed of light = line length of 10 meters
    BigDecimal length = shipVelocity.multiply(new BigDecimal("10"))
        .divide(new BigDecimal("299792458"), 10, RoundingMode.HALF_UP);

    // max length 15
    if (length.compareTo(new BigDecimal("20")) > 0) {
      length = new BigDecimal("20");
    }

    // calculate the particle count based on the length of the line
    int particleCount = length.divide(new BigDecimal("0.2"), 10, RoundingMode.HALF_UP).intValue();
    for (int i = 0; i < particleCount; i++) {
      // calculate the position of the particle
      // create a line in the direction of the ship's velocity
      double delta = (double) i / particleCount;

      // now calculate the x, y, and z coordinates of the particle
      // that draw a line going in the direction of the ship's velocity
      // keep in mind:
      // top-down view:
      // forwards: -z
      //       -z
      //       |
      // -x  ------ +x
      //       |
      //       +z
      //

      double x = planetCenterInWorld.x() +
          length.doubleValue() * Math.sin(theta.doubleValue()) * Math.cos(phi.doubleValue()) *
              delta;
      double y = planetCenterInWorld.y() +
          length.doubleValue() * Math.sin(theta.doubleValue()) * Math.sin(phi.doubleValue()) *
              delta;
      double z = planetCenterInWorld.z() +
          length.doubleValue() * Math.cos(theta.doubleValue()) * delta;

      // add the position to the list
      positions.add(new Pos(x, y, z));
    }

    return positions;
  }

  /**
   * Draws a planet in the minecraft world.
   *
   * @param space                             the game space
   * @param planetCenterInWorldRelativeToShip the center of the planet in the minecraft world relative to the center of
   *                                          the ship
   * @param radiusOfPlanetOnDrawSphereSurface the radius of the planet on the surface of the 'draw sphere'
   */
  private List<Pos> calculateParticlePositions3DBoundToDrawCircleRadius(SpaceShipSpace space,
                                                                        Pos planetCenterInWorldRelativeToShip,
                                                                        double radiusOfPlanetOnDrawSphereSurface) {
    // use fewer particles if the planet is farther away from the ship,
    // and cap at 3000 particles
    int particleCount = (int) (radiusOfPlanetOnDrawSphereSurface * 250);
    if (particleCount > 3000) {
      particleCount = 3000;
    }

    // we need to generate a list of particle positions that are bound to the 'draw sphere'
    // (we are given the center of the object for reference)
    List<Pos> particlePositions3DBoundToRadius = new ArrayList<>();

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
      particlePositions3DBoundToRadius.add(
          pointOn3DPlanetAroundOrigin.add(planetCenterInWorldRelativeToShip));
    }
    return particlePositions3DBoundToRadius;
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
}
