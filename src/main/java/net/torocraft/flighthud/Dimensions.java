package net.torocraft.flighthud;

import net.minecraft.client.MinecraftClient;
import net.torocraft.flighthud.config.HudConfig;

import java.lang.Math;

public class Dimensions {

  public float hScreen;
  public float wScreen;
  public float pixelsPerDegree;
  public float xMid;
  public float yMid;

  public float wFrame;
  public float hFrame;
  public float lFrame;
  public float rFrame;
  public float tFrame;
  public float bFrame;

  public void update(MinecraftClient client) {
    if (HudComponent.CONFIG == null) {
      return;
    }
    HudConfig c = HudComponent.CONFIG;
    hScreen = client.getWindow().getScaledHeight();
    wScreen = client.getWindow().getScaledWidth();

    if (c.scale != 1d && c.scale > 0) {
      hScreen = hScreen * c.scale;
      wScreen = wScreen * c.scale;
    }

    // The HUD is rendered as a plane in front of the player and not at a
    // spehere. The pixelsPerDegree parameter is therefore a simplification,
    // which is not exact.
    //
    // To use this simplification, a given spot on the screen has to be picked
    // to be be correct for the approximation.
    //
    // Most intuitive would then be to have the horizon line correctly stick to
    // the world when in the center of the screen. So therefore, calculate the
    // number of degrees per pixel in the _center_ of the screen
    //
    // This simplifies rendering a lot, instead of always calculate the correct
    // transformation between spherical and cartesian coordinates


    // Calculate height of virtual screen at distance of 1 unit
    // Note that FOV is degrees from top to bottom, we need height from center
    // to top

    Integer fov_deg = client.options.getFov().getValue();
    double hud_height = Math.tan(fov_deg * Math.PI / 180.0 / 2.0);
    double hud_pixel_height = hud_height / (double)(hScreen/2);
    pixelsPerDegree = 1.0f / (float)(Math.atan(hud_pixel_height) * 180.0 / Math.PI);

    xMid = wScreen / 2;
    yMid = hScreen / 2;

    wFrame = wScreen * c.width;
    hFrame = hScreen * c.height;

    lFrame = ((wScreen - wFrame) / 2) + c.xOffset;
    rFrame = lFrame + wFrame;

    tFrame = ((hScreen - hFrame) / 2) + c.yOffset;
    bFrame = tFrame + hFrame;
  }

}
