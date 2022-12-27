package net.skycade.space.model.sector;

import java.math.BigDecimal;
import java.util.List;
import net.skycade.space.model.sector.contained.SectorContainedObject;

/**
 * Represents a sector in the space game.
 * A sector is a 3D space that contains a number of planets, objects and other entities.
 * <p>
 * A sector's location is defined by its X, Y and Z coordinates in the universe.
 * The position is not related to Minecraft's coordinates, because the universe is not a Minecraft world,
 * it's a 3D space that was defined by the game's developers.
 * <p>
 * All sectors are SPHERICAL, and the radius of the sector is defined by the {@link #radius} constant.
 *
 * @author Jacob Cohen
 */
public class Sector {

  /**
   * The position of the sector in the universe.
   * Again, this is not related to Minecraft's coordinates.
   * The position's X, Y, and Z coordinates may be VERY large.
   */
  private final SectorPosition position;

  /**
   * The objects contained in this sector.
   */
  private final List<SectorContainedObject> containedObjects;

  /**
   * The radius of the sector.
   * Meters.
   */
  private final BigDecimal radius;

  /**
   * Constructor.
   *
   * @param position         The position of the sector in the universe.
   * @param containedObjects The objects contained in this sector.
   * @param radius           The radius of the sector.
   */
  public Sector(SectorPosition position, List<SectorContainedObject> containedObjects,
                BigDecimal radius) {
    this.position = position;
    this.containedObjects = containedObjects;
    this.radius = radius;
  }

  /**
   * Get the position of the sector in the universe.
   *
   * @return The position of the sector in the universe.
   */
  public SectorPosition getPosition() {
    return position;
  }

  /**
   * Get the objects contained in this sector.
   *
   * @return The objects contained in this sector.
   */
  public List<SectorContainedObject> getContainedObjects() {
    return containedObjects;
  }

  /**
   * Add an object to this sector.
   *
   * @param object The object to add.
   */
  public void addContainedObject(SectorContainedObject object) {
    this.containedObjects.add(object);
  }

  /**
   * Get the radius of the sector.
   *
   * @return The radius of the sector.
   */
  public BigDecimal getRadius() {
    return radius;
  }
}
