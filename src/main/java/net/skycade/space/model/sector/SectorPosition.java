package net.skycade.space.model.sector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Represents a sector's position in the universe.
 * A sector's position is the center of the sector.
 * <p>
 * The position is defined by its X, Y and Z coordinates in the universe.
 * The X, Y, and Z coordinates are represented by a BigInteger, which means that the universe is
 * theoretically infinite. When we want to transport players to different sectors, we use the concept
 * of having BigInts because a sector can be light years away from another sector.
 * We use the BigInt values to calculate the distance between sectors, and then play an animation
 * of a certain amount of time to transport the player to the new sector; this time is calculated
 * by the distance between the two sectors.
 */
public class SectorPosition {

  public static final SectorPosition EMPTY_SPACE = new SectorPosition(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);

  /**
   * The X coordinate of the sector.
   */
  private final BigInteger x;

  /**
   * The Y coordinate of the sector.
   */
  private final BigInteger y;

  /**
   * The Z coordinate of the sector.
   */
  private final BigInteger z;

  /**
   * Constructor.
   *
   * @param x The X coordinate of the sector.
   * @param y The Y coordinate of the sector.
   * @param z The Z coordinate of the sector.
   */
  public SectorPosition(BigInteger x, BigInteger y, BigInteger z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Constructor.
   *
   * @param x The X coordinate of the sector.
   * @param y The Y coordinate of the sector.
   * @param z The Z coordinate of the sector.
   */
  public SectorPosition(int x, int y, int z) {
    this.x = BigInteger.valueOf(x);
    this.y = BigInteger.valueOf(y);
    this.z = BigInteger.valueOf(z);
  }

  /**
   * Get the X coordinate of the sector.
   *
   * @return The X coordinate of the sector.
   */
  public BigInteger getX() {
    return x;
  }

  /**
   * Get the Y coordinate of the sector.
   *
   * @return The Y coordinate of the sector.
   */
  public BigInteger getY() {
    return y;
  }

  /**
   * Get the Z coordinate of the sector.
   *
   * @return The Z coordinate of the sector.
   */
  public BigInteger getZ() {
    return z;
  }

  /**
   * Get the distance between this sector and another sector.
   *
   * @param other The other sector.
   * @return The distance between this sector and the other sector.
   */
  public BigDecimal distance(SectorPosition other) {
    // keep using big ints
    BigInteger x = this.x.subtract(other.x);
    BigInteger y = this.y.subtract(other.y);
    BigInteger z = this.z.subtract(other.z);

    // calculate the distance
    BigDecimal distance = new BigDecimal(x.pow(2).add(y.pow(2)).add(z.pow(2)));
    return distance.sqrt(MathContext.DECIMAL128);
  }
}
