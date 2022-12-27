package net.skycade.space.model.physics.vector;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record SectorContainedVec(BigDecimal x, BigDecimal y, BigDecimal z)
    implements SectorContainedPoint {

  public static final SectorContainedVec ZERO =
      new SectorContainedVec(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

  @Override
  public SectorContainedVec add(SectorContainedPoint other) {
    return new SectorContainedVec(x.add(other.x()), y.add(other.y()), z.add(other.z()));
  }

  @Override
  public SectorContainedVec sub(SectorContainedPoint other) {
    return new SectorContainedVec(x.subtract(other.x()), y.subtract(other.y()),
        z.subtract(other.z()));
  }

  @Override
  public SectorContainedVec mul(SectorContainedPoint other) {
    return new SectorContainedVec(x.multiply(other.x()), y.multiply(other.y()),
        z.multiply(other.z()));
  }

  @Override
  public SectorContainedVec div(SectorContainedPoint other) {
    return new SectorContainedVec(x.divide(other.x(), RoundingMode.HALF_UP),
        y.divide(other.y(), RoundingMode.HALF_UP), z.divide(other.z(), RoundingMode.HALF_UP));
  }

  @Override
  public SectorContainedVec add(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedVec(this.x.add(x), this.y.add(y), this.z.add(z));
  }

  @Override
  public SectorContainedVec sub(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedVec(this.x.subtract(x), this.y.subtract(y), this.z.subtract(z));
  }

  @Override
  public SectorContainedVec mul(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedVec(this.x.multiply(x), this.y.multiply(y), this.z.multiply(z));
  }

  @Override
  public SectorContainedVec div(BigDecimal x, BigDecimal y, BigDecimal z) {
    return new SectorContainedVec(this.x.divide(x, RoundingMode.HALF_UP),
        this.y.divide(y, RoundingMode.HALF_UP), this.z.divide(z, RoundingMode.HALF_UP));
  }

  @Override
  public SectorContainedVec mul(BigDecimal other) {
    return new SectorContainedVec(x.multiply(other), y.multiply(other), z.multiply(other));
  }

  @Override
  public SectorContainedVec div(BigDecimal other) {
    return new SectorContainedVec(x.divide(other, RoundingMode.HALF_UP),
        y.divide(other, RoundingMode.HALF_UP), z.divide(other, RoundingMode.HALF_UP));
  }

  @Override
  public SectorContainedVec negate() {
    return new SectorContainedVec(x.negate(), y.negate(), z.negate());
  }

  @Override
  public SectorContainedVec normalize() {
    return new SectorContainedVec(x.divide(length(), RoundingMode.HALF_UP),
        y.divide(length(), RoundingMode.HALF_UP), z.divide(length(), RoundingMode.HALF_UP));
  }

  @Override
  public SectorContainedVec withX(BigDecimal x) {
    return new SectorContainedVec(x, y, z);
  }

  @Override
  public SectorContainedVec withY(BigDecimal y) {
    return new SectorContainedVec(x, y, z);
  }

  @Override
  public SectorContainedVec withZ(BigDecimal z) {
    return new SectorContainedVec(x, y, z);
  }

  @Override
  public BigDecimal length() {
    return BigDecimal.valueOf(Math.sqrt(x.pow(2).add(y.pow(2)).add(z.pow(2)).doubleValue()));
  }

  @Override
  public String toString() {
    return "SectorContainedVec{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
