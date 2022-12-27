package net.skycade.space.space;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.skycade.serverruntime.api.space.GameSpace;
import net.skycade.space.model.dimension.SpaceDimension;
import net.skycade.space.model.distance.LightYear;
import net.skycade.space.model.physics.object.SectorStar;
import net.skycade.space.model.physics.vector.SectorContainedPos;
import net.skycade.space.model.physics.vector.SectorContainedVec;
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
  private final SectorContainedObject spaceShipReference;

  /**
   * Constructor.
   */
  public SpaceShipSpace() {
    super(UUID.randomUUID(), SpaceDimension.INSTANCE);
    this.sectorRenderer = new SectorRenderer(this);
    this.sector = new PredefinedEmptySpaceSector();
    this.spaceShipReference = new SectorSpaceShip(
        new SectorContainedPos(new BigDecimal("500000000"), BigDecimal.ZERO, BigDecimal.ZERO));
  }

  //
  // todo: redo the whole star animation thing
  //
  // add "stars" to the scene, and when drawing them,
  // make them a "ball" if they're moving slow (CREATE PHYSICS ENGINE AND STORE THE VELOCITY IN THE STAR OBJECT)
  // and if they're moving fast, make them a "line" (CREATE PHYSICS ENGINE AND STORE THE VELOCITY IN THE STAR OBJECT)
  // this will GREATLY reduce the amount of code needed, because we can just modulate the velocity of each start instead
  // of individually drawing each particle

  @Override
  public void init() {
    setChunkLoader(new AnvilLoader(Path.of("spaceship-space")));
    setTimeRate(0);

    // info key about the sector
    // +x is behind the spaceship
    // +y is up
    // +z is to the left of the spaceship


    this.instanceBoundPlayerEventNode().addListener(PlayerSpawnEvent.class, event -> {
      event.getPlayer().setRespawnPoint(SpaceShipSpaceConstants.SPAWN_POSITION);
      event.getPlayer().teleport(SpaceShipSpaceConstants.SPAWN_POSITION);
      event.getPlayer().setGameMode(GameMode.SPECTATOR);
      runJoinTasks();
    });

    scheduleNextTick((in) -> {
      SectorContainedPos starPos =
          new SectorContainedPos(new BigDecimal("-84400000"), BigDecimal.ZERO, BigDecimal.ZERO);
      SectorStar star = new SectorStar(starPos, new BigDecimal("1737400"));

      SectorContainedPos secondStarPos =
          new SectorContainedPos(new BigDecimal("-44400000"), BigDecimal.ZERO,
              new BigDecimal("44400000"));
      SectorStar secondStar = new SectorStar(secondStarPos, new BigDecimal("2737400"));

      // add the star to the sector
      this.sector.addContainedObject(star);
      this.sector.addContainedObject(secondStar);

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

  private static class BeamData {
    private final Pos start;
    private final Pos end;

    public BeamData(Pos start, Pos end) {
      this.start = start;
      this.end = end;
    }

    public Pos getStart() {
      return start;
    }

    public Pos getEnd() {
      return end;
    }
  }

  private void runJoinTasks() {
    scheduler().buildTask(() -> {
      // tick physics for all the stars
      for (SectorContainedObject containedObject : this.sector.getContainedObjects()) {
        containedObject.tickPhysics();
      }
      // tick physics for the spaceship
      this.spaceShipReference.tickPhysics();
    }).delay(TaskSchedule.tick(20)).repeat(Duration.ofMillis(100)).schedule();

    // do hyperspace animation for 2 seconds then star rendering the sector
    Task task1 = scheduler().buildTask(() -> {
      try {
        drawHyperSpeedLines();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).delay(TaskSchedule.tick(20)).repeat(Duration.ofMillis(100)).schedule();

    scheduler().buildTask(() -> {
      // cancel the previous task
      task1.cancel();

      MinecraftServer.getSchedulerManager().buildTask(sectorRenderer::render)
          .repeat(Duration.ofMillis(100)).schedule();

      scheduler().buildTask(() -> {
        this.spaceShipReference.setVelocity(this.spaceShipReference.getVelocity()
            .add(new BigDecimal("-51000000"), BigDecimal.ZERO, BigDecimal.ZERO));
        // add an acc to the spaceship so it slows down
        this.spaceShipReference.setAcceleration(this.spaceShipReference.getAcceleration()
            .add(new BigDecimal("2300000"), BigDecimal.ZERO, BigDecimal.ZERO));

        // after 1 second, set the acc to 0 and vel to constant
        scheduler().buildTask(() -> {
          this.spaceShipReference.setVelocity(
              new SectorContainedVec(new BigDecimal("-500000"), BigDecimal.ZERO, BigDecimal.ZERO));
          this.spaceShipReference.setAcceleration(
              new SectorContainedVec(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        }).delay(TaskSchedule.tick(70)).schedule();
      }).delay(TaskSchedule.tick(1)).schedule();
    }).delay(TaskSchedule.tick(20)).schedule();
  }


  private void drawHyperSpeedLines() throws InterruptedException {
    Pos hyperSpeedParticleCenter = new Pos(-300, 215, 2.5);
    Pos centerOfShip = SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP;
    int particlesPerBeamSegment = 1300;
    int minimumEndingRadiusAroundShip = 50;
    int maximumEndingRadiusAroundShip = 100;
    int numberOfBeams = 200;
    double timeInSecondsForEachBeamToTravel = 2;

    List<BeamData> beams = new ArrayList<>();

    // the ship starts at -x and along the length from front to back it goes +x
    // the ship starts at +z and along the length from left to right it goes -z
    // the ship starts at -y and along the length from bottom to top it goes +y

    // draw 50 beams of particles coming from the center and going past the center of the ship
    for (int beam = 0; beam < numberOfBeams; beam++) {
      // get a random ending distance for the beam, in a circle around the center of the ship
      double randomAngle = Math.random() * 2 * Math.PI;
      // get the difference in z and y coordinates between the center of the ship and the end of the beam (random radius)
      double randomRadius = minimumEndingRadiusAroundShip +
          Math.random() * (maximumEndingRadiusAroundShip - minimumEndingRadiusAroundShip);
      double endingZDiff = Math.cos(randomAngle) * randomRadius;
      double endingYDiff = Math.sin(randomAngle) * randomRadius;

      double randomStartRadius = 10 + Math.random() * (30 - 10);
      double startingZDiff = Math.cos(randomAngle) * randomStartRadius;
      double startingYDiff = Math.sin(randomAngle) * randomStartRadius;

      beams.add(new BeamData(
          new Pos(hyperSpeedParticleCenter.x(), hyperSpeedParticleCenter.y() + startingYDiff,
              hyperSpeedParticleCenter.z() + startingZDiff),
          new Pos(centerOfShip.x() + 100, centerOfShip.y() + endingYDiff,
              centerOfShip.z() + endingZDiff)));
    }

    // recreate the star wars hyper-speed effect using particles
    for (BeamData beamData : beams) {
      // get the difference in x, y, and z coordinates between the start and end of the beam
      double xDiff = beamData.getEnd().x() - beamData.getStart().x();
      double yDiff = beamData.getEnd().y() - beamData.getStart().y();
      double zDiff = beamData.getEnd().z() - beamData.getStart().z();

      // get the difference in x, y, and z coordinates between each particle in the beam
      double xDiffPerParticle = xDiff / particlesPerBeamSegment;
      double yDiffPerParticle = yDiff / particlesPerBeamSegment;

      // get the difference in z and y coordinates between each particle in the beam
      double zDiffPerParticle = zDiff / particlesPerBeamSegment;

      // draw the beam (make a task that runs along the length of the beam, drawing the segments moving along the beam)
      new Thread(() -> {
        // draw the beam
        for (int j = 0; j < particlesPerBeamSegment; j++) {
          // get the current position of the particle
          double currentX = beamData.getStart().x() + (xDiffPerParticle * j);
          double currentY = beamData.getStart().y() + (yDiffPerParticle * j);
          double currentZ = beamData.getStart().z() + (zDiffPerParticle * j);

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
      Thread.sleep(50);
    }
  }

  private static class StarData {
    private final Pos start;
    private final Pos end;

    private StarData(Pos start, Pos end) {
      this.start = start;
      this.end = end;
    }

    public Pos getStart() {
      return start;
    }

    public Pos getEnd() {
      return end;
    }
  }

  private void drawStars() {
    Pos hyperSpeedParticleCenter = new Pos(-300, 215, 2.5);
    Pos centerOfShip = SpaceShipSpaceConstants.THEORETICAL_CENTER_OF_SHIP;
    int numberOfStars = 100;
    double fastSpeedParticlesPerSecond = 15;
    double slowSpeedBlocksPerSecond = 0.06;
    int numberOfParticlesPerStarTravelTrack = 1000;


    // the ship starts at -x and along the length from front to back it goes +x
    // the ship starts at +z and along the length from left to right it goes -z
    // the ship starts at -y and along the length from bottom to top it goes +y

    List<StarData> stars = new ArrayList<>();

    // get random points in a circle surrounding the starting point of the particles
    // we'll move them slowly towards the center of the ship
    for (int star = 0; star < numberOfStars; star++) {
      // get a random angle
      double randomAngle = Math.random() * 2 * Math.PI;
      // get the difference in z and y coordinates between the center of the ship and the end of the beam (random radius)
      double randomRadius = 10 + Math.random() * (150 - 10);
      double endingZDiff = Math.cos(randomAngle) * randomRadius;
      double endingYDiff = Math.sin(randomAngle) * randomRadius;

      Pos start = new Pos(hyperSpeedParticleCenter.x(), hyperSpeedParticleCenter.y() + endingYDiff,
          hyperSpeedParticleCenter.z() + endingZDiff);
      Pos end = new Pos(centerOfShip.x() + 100, hyperSpeedParticleCenter.y() + endingYDiff,
          hyperSpeedParticleCenter.z() + endingZDiff);

      stars.add(new StarData(start, end));
    }

    // draw the stars, one particle for each star
    // do a velocity curve, go really fast like the stars are coming in, then slow down quickly
    // and maintain the slow speed up until the end of the path
    for (StarData starData : stars) {
      // get the difference in x, y, and z coordinates between the start and end of the beam
      double xDiff = starData.getEnd().x() - starData.getStart().x();
      double yDiff = starData.getEnd().y() - starData.getStart().y();
      double zDiff = starData.getEnd().z() - starData.getStart().z();

      new Thread(() -> {
        double currentX = starData.getStart().x();
        double currentY = starData.getStart().y();
        double currentZ = starData.getStart().z();

        // start the particle at the start of the path, go fast then quickly slow down and maintain the slow speed for the rest of the path
        for (int j = 0; j < 420; j++) {
          // get the current position of the particle
          Pos currentPos = new Pos(currentX, currentY, currentZ);

          // draw the particle
          drawParticle(currentPos);

          // get the difference in x, y, and z coordinates between each particle in the beam
          double xDiffPerParticle = xDiff / numberOfParticlesPerStarTravelTrack;
          double yDiffPerParticle = yDiff / numberOfParticlesPerStarTravelTrack;
          double zDiffPerParticle = zDiff / numberOfParticlesPerStarTravelTrack;

          // get the current position of the particle
          currentX += xDiffPerParticle;
          currentY += yDiffPerParticle;
          currentZ += zDiffPerParticle;

          // wait for the next particle to be drawn
          try {
            Thread.sleep((long) (1000 / (fastSpeedParticlesPerSecond * 100)));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        // maintain the slow speed for the rest of the path
        for (int j = 420; j < numberOfParticlesPerStarTravelTrack; j++) {
          // get the current position of the particle
          Pos currentPos = new Pos(currentX, currentY, currentZ);

          // draw the particle, but for each do a little ball of particles to make it look like a star
          // (bigger)
          double starSize = 0.5;
          for (int k = 0; k < 10; k++) {
            // get a random angle
            double randomAngle = Math.random() * 2 * Math.PI;
            // get the difference in z and y coordinates between the center of the ship and the end of the beam (random radius)
            double randomRadius = Math.random() * starSize;
            double endingZDiff = Math.cos(randomAngle) * randomRadius;
            double endingYDiff = Math.sin(randomAngle) * randomRadius;

            drawParticle(
                new Pos(currentPos.x(), currentPos.y() + endingYDiff, currentPos.z() + endingZDiff),
                1);
          }

          // get the difference in x, y, and z coordinates between each particle in the beam
          double xDiffPerParticle = xDiff / numberOfParticlesPerStarTravelTrack;
          double yDiffPerParticle = yDiff / numberOfParticlesPerStarTravelTrack;
          double zDiffPerParticle = zDiff / numberOfParticlesPerStarTravelTrack;

          // get the current position of the particle
          currentX += xDiffPerParticle;
          currentY += yDiffPerParticle;
          currentZ += zDiffPerParticle;

          // wait for the next particle to be drawn
          try {
            Thread.sleep((long) (1000 / (slowSpeedBlocksPerSecond * 100)));
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();
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
   * Gets the ship's current position.
   *
   * @return the ship's current position
   */
  public SectorContainedPos getShipPosition() {
    return this.spaceShipReference.getPosition();
  }
}
