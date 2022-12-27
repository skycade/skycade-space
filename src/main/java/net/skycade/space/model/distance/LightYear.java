package net.skycade.space.model.distance;

import java.math.BigDecimal;
import java.math.MathContext;

public class LightYear {

  /**
   * The value of the distance unit.
   */
  private final BigDecimal value;

  /**
   * Constructor.
   *
   * @param value The value of the distance unit.
   */
  private LightYear(BigDecimal value) {
    this.value = value;
  }

  /**
   * Constructor.
   *
   * @param value The value of the distance unit.
   */
  public LightYear(double value) {
    this.value = new BigDecimal(value);
  }

  /**
   * Convert this distance unit to a meter based distance unit.
   *
   * @return The meter based distance unit.
   */
  public BigDecimal getValue() {
    return value;
  }

  /**
   * Convert this distance unit to a meter based distance unit.
   *
   * @return The meter based distance unit.
   */
  public BigDecimal toMeters() {
    return this.getValue().multiply(new BigDecimal("9460730472580800"), MathContext.DECIMAL128);
  }

  /**
   * Convert this distance unit from a meter based distance unit.
   *
   * @param meters The meter based distance unit.
   * @return The distance unit.
   */
  public static LightYear fromMeters(BigDecimal meters) {
    return new LightYear(meters.divide(new BigDecimal("9460730472580800"), MathContext.DECIMAL128));
  }

  /**
   * Convert this distance unit from a given value.
   *
   * @param lightYears The value of the distance unit.
   * @return The distance unit.
   */
  public static LightYear of(double lightYears) {
    return new LightYear(lightYears);
  }
}
