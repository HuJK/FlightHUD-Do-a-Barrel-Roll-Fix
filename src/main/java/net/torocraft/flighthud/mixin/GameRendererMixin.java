package net.torocraft.flighthud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

import org.joml.Matrix3f;
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
    MinecraftClient client;
    Camera camera;

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
        Camera camera = this.camera;
        MatrixStack matrices = new MatrixStack();
        if (camera != null) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        }
        Matrix3f matrix3f = (new Matrix3f(matrices.peek().getNormalMatrix()));
        computer.update(client, matrix3f);
    }
}