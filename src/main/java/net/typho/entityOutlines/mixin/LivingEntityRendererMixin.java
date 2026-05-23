package net.typho.entityOutlines.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.typho.entityOutlines.StoredQuad;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.21.11 {
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
//? } else {
/*
import net.minecraft.client.renderer.rendertype.RenderType;
*///? }

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @WrapOperation(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer render(MultiBufferSource buffers, RenderType renderType, Operation<VertexConsumer> original, @Local(argsOnly = true) T livingEntity, @Share("lines") LocalRef<List<StoredQuad>> lines) {
        if (renderType.mode() == VertexFormat.Mode.QUADS && Minecraft.getInstance().crosshairPickEntity == livingEntity) {
            List<StoredQuad> list = new ArrayList<>();
            lines.set(list);

            return VertexMultiConsumer.create(original.call(buffers, renderType), new VertexConsumer() {
                final Vector3f[] vertices = new Vector3f[4];
                final Vector3f[] normals = new Vector3f[4];
                int index = 0;

                @Override
                public @NotNull VertexConsumer addVertex(float f, float g, float h) {
                    if (index == 4) {
                        index = 0;

                        list.add(new StoredQuad(
                                vertices[0],
                                vertices[1],
                                vertices[2],
                                vertices[3],
                                normals[0],
                                normals[1],
                                normals[2],
                                normals[3]
                        ));
                    }

                    vertices[index++] = new Vector3f(f, g, h);

                    return this;
                }

                @Override
                public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
                    return this;
                }

                @Override
                public @NotNull VertexConsumer setUv(float f, float g) {
                    return this;
                }

                @Override
                public @NotNull VertexConsumer setUv1(int i, int j) {
                    return this;
                }

                @Override
                public @NotNull VertexConsumer setUv2(int i, int j) {
                    return this;
                }

                @Override
                public @NotNull VertexConsumer setNormal(float f, float g, float h) {
                    normals[index - 1] = new Vector3f(f, g, h);
                    return this;
                }
            });
        } else {
            return original.call(buffers, renderType);
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("TAIL")
    )
    private void render(T livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci, @Share("lines") LocalRef<List<StoredQuad>> lines) {
        if (lines.get() != null) {
            int color = FastColor.ARGB32.color(102, -16777216);
            VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.lines());

            for (StoredQuad quad : lines.get()) {
                consumer.addVertex(quad.v0).setColor(color).setNormal(quad.n0.x, quad.n0.y, quad.n0.z);
                consumer.addVertex(quad.v1).setColor(color).setNormal(quad.n1.x, quad.n1.y, quad.n1.z);

                consumer.addVertex(quad.v1).setColor(color).setNormal(quad.n1.x, quad.n1.y, quad.n1.z);
                consumer.addVertex(quad.v2).setColor(color).setNormal(quad.n2.x, quad.n2.y, quad.n2.z);

                consumer.addVertex(quad.v2).setColor(color).setNormal(quad.n2.x, quad.n2.y, quad.n2.z);
                consumer.addVertex(quad.v3).setColor(color).setNormal(quad.n3.x, quad.n3.y, quad.n3.z);

                consumer.addVertex(quad.v3).setColor(color).setNormal(quad.n3.x, quad.n3.y, quad.n3.z);
                consumer.addVertex(quad.v0).setColor(color).setNormal(quad.n0.x, quad.n0.y, quad.n0.z);
            }
        }
    }
}
