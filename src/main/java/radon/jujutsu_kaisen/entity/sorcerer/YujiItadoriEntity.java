package radon.jujutsu_kaisen.entity.sorcerer;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.sorcerer.base.SorcererEntity;
import radon.jujutsu_kaisen.item.JJKItems;

import java.util.List;

public class YujiItadoriEntity extends SorcererEntity {
    public YujiItadoriEntity(EntityType<? extends PathfinderMob> pType, Level pLevel) {
        super(pType, pLevel);
    }

    @Override
    protected boolean isCustom() {
        return false;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);

        if (stack.is(JJKItems.SUKUNA_FINGER.get())) {
            ISorcererData cap = this.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            int count = stack.getCount();
            int eaten = cap.addFingers(count);

            if (eaten > 0) {
                this.playSound(this.getEatingSound(stack), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);

                stack.shrink(eaten);

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return InteractionResult.FAIL;
        } else {
            return super.mobInteract(pPlayer, pHand);
        }
    }

    @Override
    public float getExperience() {
        return SorcererGrade.GRADE_1.getRequiredExperience();
    }

    @Override
    public @Nullable CursedTechnique getTechnique() {
        return null;
    }

    @Override
    public JujutsuType getJujutsuType() {
        return JujutsuType.SORCERER;
    }

    @Override
    public @NotNull List<Trait> getTraits() {
        return List.of(Trait.VESSEL);
    }


    @Override
    public void init(ISorcererData data) {
        super.init(data);

        data.setFingers(15);
    }
}
