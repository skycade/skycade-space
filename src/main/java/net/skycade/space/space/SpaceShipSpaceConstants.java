package net.skycade.space.space;

import java.math.BigDecimal;
import net.minestom.server.coordinate.Pos;

public class SpaceShipSpaceConstants {

  /**
   * The position of default spawn point in the world space.
   */
  public static final Pos SPAWN_POSITION = new Pos(0.5, 0, -4.5, 180, 0);

  /**
   * The theoretical center of the ship
   */
  public static final Pos THEORETICAL_CENTER_OF_SHIP = new Pos(0.5, 0, -4.5);

  /**
   * The radius of the ship.
   * In meters.
   */
  public static final BigDecimal SHIP_RADIUS = new BigDecimal(10);

  /**
   * The radius of the circle at which any objects will be drawn.
   * In meters.
   */
  public static final BigDecimal DRAW_ON_CIRCLE_RADIUS = new BigDecimal(50);
}
