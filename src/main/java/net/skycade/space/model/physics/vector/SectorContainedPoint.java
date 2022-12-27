package net.skycade.space.model.physics.vector;

import java.math.BigDecimal;

public sealed interface SectorContainedPoint permits SectorContainedPos, SectorContainedVec {

  BigDecimal x();

  BigDecimal y();

  BigDecimal z();

  SectorContainedPoint add(SectorContainedPoint other);

  SectorContainedPoint sub(SectorContainedPoint other);

  SectorContainedPoint mul(SectorContainedPoint other);

  SectorContainedPoint div(SectorContainedPoint other);

  SectorContainedPoint add(BigDecimal x, BigDecimal y, BigDecimal z);

  SectorContainedPoint sub(BigDecimal x, BigDecimal y, BigDecimal z);

  SectorContainedPoint mul(BigDecimal x, BigDecimal y, BigDecimal z);

  SectorContainedPoint div(BigDecimal x, BigDecimal y, BigDecimal z);

  SectorContainedPoint mul(BigDecimal other);

  SectorContainedPoint div(BigDecimal other);

  SectorContainedPoint negate();

  SectorContainedPoint normalize();

  SectorContainedPoint withX(BigDecimal x);

  SectorContainedPoint withY(BigDecimal y);

  SectorContainedPoint withZ(BigDecimal z);

  BigDecimal length();
}

