package net.skycade.space.sector;

import java.util.ArrayList;
import net.skycade.space.model.distance.LightYear;
import net.skycade.space.model.sector.Sector;
import net.skycade.space.model.sector.SectorPosition;

/**
 * Usually sectors are defined dynamically when we create worlds and stuff, but this
 * sector represents "empty space" which just has random stars and planets between the "defined sectors".
 *
 * @author Jacob Cohen
 */
public class PredefinedEmptySpaceSector extends Sector {

  /**
   * Constructor.
   */
  public PredefinedEmptySpaceSector() {
    super(SectorPosition.EMPTY_SPACE, new ArrayList<>(), new LightYear(10).toMeters());
  }
}
