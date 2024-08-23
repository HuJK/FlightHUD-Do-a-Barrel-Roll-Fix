package net.torocraft.flighthud;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FlightComputer {
  private static final float TICKS_PER_SECOND = 20;
  static final float rad2deg = (float)(180/Math.PI);

  public Vec3d velocity;
  public float speed;
  public float pitch;
  public float heading;
  public Vec3d flightPath;
  public float flightPitch;
  public float flightHeading;
  public float roll;
  public float altitude;
  public Integer groundLevel;
  public Float distanceFromGround;
  public Float elytraHealth;

  public static Vector3f quaternionToEuler(Quaternionf q) {
    double x = q.x;
    double y = q.y;
    double z = q.z;
    double w = q.w;

    double yaw, pitch, roll;

    // Yaw (Y-axis rotation)
    double sinr_cosp = 2 * (w * y + x * z);
    double cosr_cosp = 1 - 2 * (y * y + x * x);
    yaw = Math.atan2(sinr_cosp, cosr_cosp);

    // Pitch (X-axis rotation)
    double sinp = 2 * (w * x - z * y);
    if (Math.abs(sinp) >= 1)
        pitch = Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
    else
        pitch = Math.asin(sinp);

    // Roll (Z-axis rotation)
    double siny_cosp = 2 * (w * z + y * x);
    double cosy_cosp = 1 - 2 * (x * x + z * z);
    roll = Math.atan2(siny_cosp, cosy_cosp);

    return new Vector3f((float) yaw, (float) pitch, (float) roll);
  }

  public void update(MinecraftClient client, Quaternionf rotation) {
    if (client==null){
      return;
    }
    Vector3f eulerrotation = quaternionToEuler(rotation);
    heading =  computeHeading(client);
    pitch =  computePitch(client);
    roll = computeRoll(client, -eulerrotation.z* rad2deg);
    velocity = client.player.getVelocity();
    speed = computeSpeed(client);
    altitude = computeAltitude(client);
    groundLevel = computeGroundLevel(client);
    distanceFromGround = computeDistanceFromGround(client, altitude, groundLevel);
    flightPitch = computeFlightPitch(velocity, pitch);
    flightHeading = computeFlightHeading(velocity, heading);
    elytraHealth = computeElytraHealth(client);
  }

  private Float computeElytraHealth(MinecraftClient client) {
    ItemStack stack = client.player.getEquippedStack(EquipmentSlot.CHEST);
    if (stack != null && stack.getItem() == Items.ELYTRA) {
      float remain = ((float) stack.getMaxDamage() - (float) stack.getDamage()) / (float) stack.getMaxDamage();
      return remain * 100f;
    }
    return null;
  }

  private float computeFlightPitch(Vec3d velocity, float pitch) {
    if (velocity.length() < 0.01) {
      return pitch;
    }
    Vec3d n = velocity.normalize();
    return (float) (90 - Math.toDegrees(Math.acos(n.y)));
  }

  private float computeFlightHeading(Vec3d velocity, float heading) {
    if (velocity.length() < 0.01) {
      return heading;
    }
    return toHeading((float) Math.toDegrees(-Math.atan2(velocity.x, velocity.z)));
  }

  /**
   * Roll logic is from:
   * https://github.com/Jorbon/cool_elytra/blob/main/src/main/java/edu/jorbonism/cool_elytra/mixin/GameRendererMixin.java
   * to enable both mods will sync up when used together.
   */
  private float computeRoll(MinecraftClient client, float roll) {
    if (!FlightHud.CONFIG_SETTINGS.calculateRoll) {
      return 0.0f;
    }

    return roll;
  }

  private float computePitch(MinecraftClient client) {
    if (client.player == null) {
      return 0.0f;
    }

    return -client.player.getPitch();
  }

  private boolean isGround(BlockPos pos, MinecraftClient client) {
    BlockState block = client.world.getBlockState(pos);
    return !block.isAir();
  }

  public BlockPos findGround(MinecraftClient client) {
    BlockPos pos = client.player.getBlockPos();
    while (pos.getY() >= 0) {
      pos = pos.down();
      if (isGround(pos, client)) {
        return pos;
      }
    }
    return null;
  }

  private Integer computeGroundLevel(MinecraftClient client) {
    BlockPos ground = findGround(client);
    return ground == null ? null : ground.getY();
  }

  private Float computeDistanceFromGround(MinecraftClient client, float altitude,
      Integer groundLevel) {
    if (groundLevel == null) {
      return null;
    }
    return Math.max(0f, altitude - groundLevel);
  }

  private float computeAltitude(MinecraftClient client) {
    return (float) client.player.getPos().y - 1;
  }

  private float computeHeading(MinecraftClient client) {
    if (client.player == null) {
      return 0.0f;
    }

    return toHeading(client.player.getYaw());
  }

  private float computeSpeed(MinecraftClient client) {
    float speed = 0;
    var player = client.player;
    if (player.hasVehicle()) {
      Entity entity = player.getVehicle();
      speed = (float) entity.getVelocity().length() * TICKS_PER_SECOND;
    } else {
      speed = (float) client.player.getVelocity().length() * TICKS_PER_SECOND;
    }
    return speed;
  }

  private float toHeading(float yawDegrees) {
    return (yawDegrees + 180) % 360;
  }
}
