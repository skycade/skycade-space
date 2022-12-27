package net.skycade.space.renderer;

import net.minestom.server.coordinate.Pos;
import net.skycade.space.model.sector.contained.SectorContainedObject;
import net.skycade.space.space.SpaceShipSpace;

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
    // loop through all the sectors objects in the sector
    // and render them.

    for (SectorContainedObject object : space.getSector().getContainedObjects()) {
      // render the object.
      object.draw(space);
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
}
