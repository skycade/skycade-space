package net.skycade.space.model.physics.vector;

import java.math.BigDecimal;
import java.math.MathContext;

public record SectorContainedPos(BigDecimal x, BigDecimal y, BigDecimal z)
    implements SectorContainedPoint {

  public static final SectorContainedPos ZERO =
      new SectorContainedPos(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

  @Override
  public SectorContainedPos add(SectorContainedPoint other) {
    return new SectorContainedPos(x.add(other.x()), y.add(other.y()), z.add(other.z()));
  }

  @Override
  public SectorContainedPos sub(SectorContainedPoint other) {
    return new SectorContainedPos(x.subtract(other.x()), y.subtract(other.y()),
        z.subtract(other.z()));
  }

  @Override
  public SectorContainedPos mul(SectorContainedPoint other) {
    return new SectorContainedPos(x.multiply(other.x()), y.multiply(other.y()),
        z.multiply(other.z()));
  }

  @Override
  public SectorContainedPos div(SectorContainedPoint other) {
    return new SectorContainedPos(x.divide(other.x(), MathContext.DECIMAL128),
        y.divide(other.y(), MathContext.DECIMAL128), z.divide(other.z(), MathContext.DECIMAL128));
  }

  @Override
  public SectorContainedPos add(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedPos(this.x.add(x), this.y.add(y), this.z.add(z));
  }

  @Override
  public SectorContainedPos sub(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedPos(this.x.subtract(x), this.y.subtract(y), this.z.subtract(z));
  }

  @Override
  public SectorContainedPos mul(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedPos(this.x.multiply(x), this.y.multiply(y), this.z.multiply(z));
  }

  @Override
  public SectorContainedPos div(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedPos(this.x.divide(x, MathContext.DECIMAL128),
        this.y.divide(y, MathContext.DECIMAL128), this.z.divide(z, MathContext.DECIMAL128));
  }

  @Override
  public SectorContainedPos mul(BigDecimal other) {
    return new SectorContainedPos(x.multiply(other), y.multiply(other), z.multiply(other));
  }

  @Override
  public SectorContainedPos div(BigDecimal other) {
    return new SectorContainedPos(x.divide(other, MathContext.DECIMAL128),
        y.divide(other, MathContext.DECIMAL128), z.divide(other, MathContext.DECIMAL128));
  }

  @Override
  public SectorContainedPos negate() {
    return new SectorContainedPos(x.negate(), y.negate(), z.negate());
  }

  @Override
  public SectorContainedPos normalize() {
    // if the vector is zero, return zero
    if (this.length().equals(BigDecimal.ZERO)) {
      return ZERO;
    }

    return new SectorContainedPos(x.divide(this.length(), MathContext.DECIMAL128),
        y.divide(this.length(), MathContext.DECIMAL128),
        z.divide(this.length(), MathContext.DECIMAL128));
  }

  @Override
  public SectorContainedPos withX(BigDecimal x) {
    return new SectorContainedPos(x, y, z);
  }

  @Override
  public SectorContainedPos withY(BigDecimal y) {
    return new SectorContainedPos(x, y, z);
  }

  @Override
  public SectorContainedPos withZ(BigDecimal z) {
    return new SectorContainedPos(x, y, z);
  }

  @Override
  public BigDecimal length() {
    return x.pow(2).add(y.pow(2)).add(z.pow(2)).sqrt(MathContext.DECIMAL128);
  }

  public SectorContainedPos distance(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedPos(this.x.subtract(x), this.y.subtract(y), this.z.subtract(z));
  }

  public SectorContainedPos distance(SectorContainedPoint other) {
    return new SectorContainedPos(x.subtract(other.x()), y.subtract(other.y()),
        z.subtract(other.z()));
  }

  public BigDecimal distanceSquared(SectorContainedPos other) {
    return x.subtract(other.x).pow(2).add(y.subtract(other.y).pow(2))
        .add(z.subtract(other.z).pow(2));
  }

  /**
   * Returns the distance between this point and the given point.
   *
   * @param other the other point.
   * @return the distance between this point and the given point.
   */
  public BigDecimal distance(SectorContainedPos other) {
    return BigDecimal.valueOf(distanceSquared(other).sqrt(MathContext.DECIMAL128).doubleValue());
  }

  @Override
  public String toString() {
    return "SectorContainedPos{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}