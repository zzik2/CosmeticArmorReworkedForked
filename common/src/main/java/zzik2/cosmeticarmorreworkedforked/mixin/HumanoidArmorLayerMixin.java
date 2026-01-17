package zzik2.cosmeticarmorreworkedforked.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.PlayerRenderHandler;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {

    @SuppressWarnings("unchecked")
    @Inject(method = "renderArmorPiece", at = @At("RETURN"))
    private void cosarmor_renderCosmeticArmorPiece(PoseStack poseStack, MultiBufferSource buffer, T entity, EquipmentSlot slot, int packedLight, A model, CallbackInfo ci) {
        if (!ModConfigs.getCosArmorStackRendering())
            return;
        if (PlayerRenderHandler.Disabled)
            return;
        if (!(entity instanceof Player player))
            return;

        int slotIndex = switch (slot) {
            case HEAD -> 3;
            case CHEST -> 2;
            case LEGS -> 1;
            case FEET -> 0;
            default -> -1;
        };

        if (slotIndex < 0)
            return;

        InventoryCosArmor invCosArmor = ModObjects.invMan.getCosArmorInventoryClient(player.getUUID());
        if (invCosArmor.isSkinArmor(slotIndex))
            return;

        ItemStack cosStack = invCosArmor.getStackInSlot(slotIndex);
        if (cosStack.isEmpty())
            return;

        ItemStack originalStack = player.getItemBySlot(slot);
        if (ItemStack.isSameItemSameComponents(originalStack, cosStack))
            return;

        try {
            player.getInventory().armor.set(slotIndex, cosStack);
            ((HumanoidArmorLayerInvoker<T, M, A>) this).invokeRenderArmorPiece(poseStack, buffer, entity, slot, packedLight, model);
        } finally {
            player.getInventory().armor.set(slotIndex, originalStack);
        }
    }

}
