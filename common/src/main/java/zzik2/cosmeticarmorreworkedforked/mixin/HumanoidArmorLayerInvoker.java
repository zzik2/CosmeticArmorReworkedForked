package zzik2.cosmeticarmorreworkedforked.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerInvoker<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @Invoker("renderArmorPiece")
    void invokeRenderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, T entity, EquipmentSlot slot, int packedLight, A model);

}
