package net.skycade.space.model.physics.vector;

import ch.obermuhlner.math.big.BigDecimalMath;
import java.math.BigDecimal;
import java.math.MathContext;
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

  public BigDecimal horizontalDirection() {
    // use trigonometry to calculate the horizontal direction
    return BigDecimalMath.atan2(x, z, MathContext.DECIMAL128);
  }

  public BigDecimal verticalDirection() {
    // use trigonometry to calculate the vertical direction
    BigDecimal horizontalLength =
        BigDecimalMath.sqrt(x.multiply(x).multiply(z.multiply(z)), MathContext.DECIMAL128);
    return BigDecimalMath.atan2(y, horizontalLength, MathContext.DECIMAL128);
  }

  public BigDecimal projectOnto(SectorContainedVec other) {
    if (other.length().equals(BigDecimal.ZERO)) {
      return BigDecimal.ZERO;
    }
    return this.dot(other).divide(other.length(), MathContext.DECIMAL128);
  }

  public BigDecimal dot(SectorContainedVec other) {
    return x.multiply(other.x()).add(y.multiply(other.y())).add(z.multiply(other.z()));
  }

  public BigDecimal theta() {
    if (!((x.equals(BigDecimal.ZERO) && y.compareTo(BigDecimal.ZERO) > 0) ||
        (x.equals(BigDecimal.ZERO) && y.compareTo(BigDecimal.ZERO) < 0))) {
      return BigDecimal.ZERO;
    }
    return BigDecimalMath.atan2(y, x, MathContext.DECIMAL128);
  }

  public BigDecimal phi() {
    if (!((x.equals(BigDecimal.ZERO) && z.compareTo(BigDecimal.ZERO) > 0) ||
        (x.equals(BigDecimal.ZERO) && z.compareTo(BigDecimal.ZERO) < 0))) {
      return BigDecimal.ZERO;
    }
    return BigDecimalMath.atan2(z, x, MathContext.DECIMAL128);
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
