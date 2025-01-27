package radon.jujutsu_kaisen.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.entity.PartEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.client.JJKRenderTypes;
import radon.jujutsu_kaisen.entity.base.JJKPartEntity;

import java.util.ArrayList;
import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow @Final private RenderBuffers renderBuffers;

    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    public Iterable<Entity> entitiesForRendering(ClientLevel instance) {
        Iterable<Entity> iter = instance.entitiesForRendering();

        List<Entity> result = new ArrayList<>();

        iter.forEach(entity -> {
            result.add(entity);

            if (entity.isMultipartEntity() && entity.getParts() != null) {
                for (PartEntity<?> part : entity.getParts()) {
                    if (part instanceof JJKPartEntity<?>) {
                        result.add(part);
                    }
                }
            }
        });
        return result;
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V", ordinal = 6, shift = At.Shift.AFTER))
    private void renderLevel(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
        this.renderBuffers.bufferSource().endBatch(JJKRenderTypes.sky());
    }
}
