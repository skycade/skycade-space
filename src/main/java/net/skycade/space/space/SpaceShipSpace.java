package net.skycade.space.space;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.Task;
import net.skycade.serverruntime.api.space.GameSpace;
import net.skycade.space.constants.PhysicsAndRenderingConstants;
import net.skycade.space.model.dimension.SpaceDimension;
import net.skycade.space.model.distance.LightYear;
import net.skycade.space.model.physics.object.SectorPlanet;
import net.skycade.space.model.physics.object.SectorStar;
import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.sector.Sector;
import net.skycade.space.model.sector.contained.SectorContainedObject;
import net.skycade.space.model.sector.contained.SectorSpaceShip;
import net.skycade.space.renderer.SectorRenderer;
import net.skycade.space.sector.PredefinedEmptySpaceSector;

/**
 * Represents the spaceship space in the game.
 * The world space can only render a single sector at a time,
 * and usually only nearby objects are rendered anyway.
 * <p>
 * The spaceship space is a 3D space that contains a number of objects in the current sector.
 * The coordinates in the world space are related to Minecraft's coordinates because
 * we use Minecraft's world to render the world space (particles, entities, blocks, etc.).
 * This does not mean the rendered objects' positions are related to Minecraft's coordinates, though.
 * <p>
 * The spaceship space is the primary space in the game, and it's only in use when the player is in the spaceship.
 * All other times, a separate space is used to render the actual world space (like when the player is ON a planet, other space object, etc.).
 *
 * @author Jacob Cohen
 */
public class SpaceShipSpace extends GameSpace {

  /**
   * Creates a new spaceship space.
   */
  public static final SpaceShipSpace INSTANCE = new SpaceShipSpace();

  /**
   * The sector renderer.
   */
  private final SectorRenderer sectorRenderer;

  /**
   * The current sector.
   */
  private final Sector sector;

  /**
   * A reference to the player/spaceship's position in the sector.
   */
  private final SectorSpaceShip spaceShipReference;

  /**
   * Constructor.
   */
  public SpaceShipSpace() {
    super(UUID.randomUUID(), SpaceDimension.INSTANCE);
    this.sectorRenderer = new SectorRenderer(this);
    this.sector = new PredefinedEmptySpaceSector();
    this.spaceShipReference = new SectorSpaceShip(
        new SectorContainedPos(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
  }

  @Override
  public void init() {
    setChunkLoader(new AnvilLoader(Path.of("spaceship-space")));
    setTimeRate(0);

    this.instanceBoundPlayerEventNode().addListener(PlayerSpawnEvent.class, event -> {
      event.getPlayer().setRespawnPoint(SpaceShipSpaceConstants.SPAWN_POSITION);
      event.getPlayer().teleport(SpaceShipSpaceConstants.SPAWN_POSITION);
      event.getPlayer().setGameMode(GameMode.SPECTATOR);

      scheduleNextTick((i) -> {
        runJoinTasks();
      });
    });


    // top down view:
    // forwards: -z
    //       -z
    //       |
    // -x  ------ +x
    //       |
    //       +z
    //

    scheduleNextTick((in) -> {
      SectorContainedPos starPos =
          new SectorContainedPos(BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("-84400000"));
      SectorStar star = new SectorStar(starPos, new BigDecimal("1737400"));

      SectorContainedPos secondStarPos =
          new SectorContainedPos(new BigDecimal("-44400000"), BigDecimal.ZERO,
              new BigDecimal("-44400000"));
      SectorStar secondStar = new SectorStar(secondStarPos, new BigDecimal("2737400"));

      SectorContainedPos planetPos =
          new SectorContainedPos(new BigDecimal("44400000"), BigDecimal.ZERO,
              new BigDecimal("-54400000"));
      SectorPlanet planet = new SectorPlanet(planetPos, new BigDecimal("5737400"));

      // add the star to the sector
      this.sector.addContainedObject(star);
      this.sector.addContainedObject(secondStar);
      this.sector.addContainedObject(planet);

      // add 400 random small stars around the spaceship
      for (int i = 0; i < 200; i++) {
        // random x between -1 and 1
        int x = (int) (Math.random() * 2) - 1;
        BigDecimal xBigDecimal = new BigDecimal(x + "." + randomNumber(50));
        LightYear xLightYear = LightYear.of(xBigDecimal);

        // random y between -1 and 1
        int y = (int) (Math.random() * 2) - 1;
        BigDecimal yBigDecimal = new BigDecimal(y + "." + randomNumber(50));
        LightYear yLightYear = LightYear.of(yBigDecimal);

        // random z between -1 and 1
        int z = (int) (Math.random() * 2) - 1;
        BigDecimal zBigDecimal = new BigDecimal(z + "." + randomNumber(50));
        LightYear zLightYear = LightYear.of(zBigDecimal);

        // create the star
        SectorContainedPos randomStarPos =
            new SectorContainedPos(xLightYear.toMeters(), yLightYear.toMeters(),
                zLightYear.toMeters());

        SectorStar randomStar = new SectorStar(randomStarPos, new BigDecimal("1737400"));

        // add the star to the sector
        this.sector.addContainedObject(randomStar);
      }
    });
  }

  public String randomNumber(int digits) {
    StringBuilder number = new StringBuilder();
    for (int i = 0; i < digits; i++) {
      number.append((int) (Math.random() * 10));
    }
    return number.toString();
  }

  private record BeamData(Pos start, Pos end) {
  }

  private void runJoinTasks() {
    Task task1 = scheduler().buildTask(() -> {
      try {
        drawHyperSpeedLines();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).repeat(Duration.ofMillis(75)).schedule();

    scheduler().buildTask(() -> {
      // cancel the previous task
      task1.cancel();

      scheduler().buildTask(sectorRenderer::render)
          .repeat(Duration.ofMillis(PhysicsAndRenderingConstants.RENDER_DELAY_MILLIS)).schedule();

      scheduler().buildTask(() -> {
        // tick physics for all the stars
        for (SectorContainedObject containedObject : this.sector.getContainedObjects()) {
          containedObject.tickPhysics();
        }
        // tick physics for the spaceship
        this.spaceShipReference.tickPhysics();
        System.out.println(
            "this.spaceShipReference.getVelocity() = " + this.spaceShipReference.getVelocity());
      }).repeat(Duration.ofMillis(PhysicsAndRenderingConstants.PHYSICS_DELAY_MILLIS)).schedule();

      scheduler().buildTask(() -> {
        this.spaceShipReference.setRotation(new SectorContainedPos(
            BigDecimal.ZERO,
            BigDecimal.ONE,
            BigDecimal.ZERO));
        scheduler().buildTask(() -> {
          this.spaceShipReference.thrustForward(new BigDecimal("10000"), 5, this);
        }).schedule();
      }).delay(Duration.ofSeconds(1)).schedule();

    }).delay(Duration.ofSeconds(1)).schedule();
  }


  private void drawHyperSpeedLines() throws InterruptedException {
    Pos hyperSpeedParticleCenter = new Pos(0, 0, -200);
    Pos centerOfShip = SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP;
    int particlesPerBeamSegment = 1300;
    int minimumEndingRadiusAroundShip = 30;
    int maximumEndingRadiusAroundShip = 50;
    int minimumStartingRadiusAroundShip = 10;
    int maximumStartingRadiusAroundShip = 15;
    int numberOfBeams = 200;
    double timeInSecondsForEachBeamToTravel = 2;

    List<BeamData> beams = new ArrayList<>();

    // the ship starts at -z, front to back is -z to +z
    // ship left to right is -x to +x

    // draw 50 beams of particles coming from the center and going past the center of the ship
    for (int beam = 0; beam < numberOfBeams; beam++) {
      // get a random ending distance for the beam, in a circle around the center of the ship
      double randomAngle = Math.random() * 2 * Math.PI;
      // get the difference in x and y coordinates between the center of the ship and the end of the beam (random radius)
      double randomRadius = minimumEndingRadiusAroundShip +
          Math.random() * (maximumEndingRadiusAroundShip - minimumEndingRadiusAroundShip);
      double endingXDiff = Math.cos(randomAngle) * randomRadius;
      double endingYDiff = Math.sin(randomAngle) * randomRadius;

      double randomStartRadius = minimumStartingRadiusAroundShip +
          Math.random() * (maximumStartingRadiusAroundShip - minimumStartingRadiusAroundShip);
      double startingXDiff = Math.cos(randomAngle) * randomStartRadius;
      double startingYDiff = Math.sin(randomAngle) * randomStartRadius;

      beams.add(new BeamData(new Pos(hyperSpeedParticleCenter.x() + startingXDiff,
          hyperSpeedParticleCenter.y() + startingYDiff, hyperSpeedParticleCenter.z()),
          new Pos(centerOfShip.x() + endingXDiff, centerOfShip.y() + endingYDiff,
              centerOfShip.z() + 100)));
    }

    // recreate the star wars hyper-speed effect using particles
    for (BeamData beamData : beams) {
      // get the difference in x, y, and z coordinates between the start and end of the beam
      double xDiff = beamData.end().x() - beamData.start().x();
      double yDiff = beamData.end().y() - beamData.start().y();
      double zDiff = beamData.end().z() - beamData.start().z();

      // get the difference in x, y, and z coordinates between each particle in the beam
      double xDiffPerParticle = xDiff / particlesPerBeamSegment;
      double yDiffPerParticle = yDiff / particlesPerBeamSegment;
      double zDiffPerParticle = zDiff / particlesPerBeamSegment;

      // draw the beam (make a task that runs along the length of the beam, drawing the segments moving along the beam)
      new Thread(() -> {
        // draw the beam
        for (int j = 0; j < particlesPerBeamSegment; j++) {
          // get the current position of the particle
          double currentX = beamData.start().x() + (xDiffPerParticle * j);
          double currentY = beamData.start().y() + (yDiffPerParticle * j);
          double currentZ = beamData.start().z() + (zDiffPerParticle * j);

          // get the current position of the particle
          Pos currentPos = new Pos(currentX, currentY, currentZ);

          // draw the particle
          drawParticle(currentPos);

          // wait for the next particle to be drawn
          try {
            Thread.sleep(
                (long) (timeInSecondsForEachBeamToTravel * 1000 / particlesPerBeamSegment));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();

      // stagger the beams, so they don't all start at the same time
      Thread.sleep(25);
    }
  }

  /**
   * Draws a particle at the given position.
   *
   * @param pos the position to draw the particle at
   */
  private void drawParticle(Pos pos) {
    drawParticle(pos, 1);
  }

  /**
   * Draws a particle at the given position.
   *
   * @param pos  the position to draw the particle at
   * @param size the size of the particle
   */
  private void drawParticle(Pos pos, int size) {
    ParticlePacket packet =
        ParticleCreator.createParticlePacket(Particle.FIREWORK, pos.x(), pos.y(), pos.z(), 0f, 0f,
            0f, size);
    sendGroupedPacket(packet);
  }

  /**
   * Gets the current sector.
   *
   * @return the current sector
   */
  public Sector getSector() {
    return this.sector;
  }

  /**
   * Gets the ship reference.
   *
   * @return the ship reference
   */
  public SectorSpaceShip getSpaceShipReference() {
    return this.spaceShipReference;
  }
}
