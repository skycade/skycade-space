package net.skycade.space.constants;

/**
 * Represents constants used for physics and rendering.
 * <p>
 * The "RENDER_DELAY_MILLIS" and "PHYSICS_DELAY_MILLIS" constants MUST be the same, this is because
 * the physics engine and rendering engine are synchronized.
 *
 * @author Jacob Cohen
 */
public class PhysicsAndRenderingConstants {

  /**
   * The delay between each rendering tick, in milliseconds.
   */
  public static final Long RENDER_DELAY_MILLIS = 40L;

  /**
   * The delay between each physics tick, in milliseconds.
   */
  public static final Long PHYSICS_DELAY_MILLIS = 40L;
}
