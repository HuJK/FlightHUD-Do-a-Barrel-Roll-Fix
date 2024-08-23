package net.torocraft.flighthud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;

import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.torocraft.flighthud.FlightHud.computer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera camera;
    @Shadow
    @Final
    MinecraftClient client;

    @Inject(
            method = "renderWorld(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;loadProjectionMatrix(Lorg/joml/Matrix4f;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderWorld(
            float tickDelta,
            long limitTime,
            CallbackInfo ci
    ) {
        if(camera != null){
            computer.update(this.client,camera.getRotation());
        } else{
            computer.update(this.client, new Quaternionf(0.8775826, 0.0440319, 0, 0));
        }
    }
}