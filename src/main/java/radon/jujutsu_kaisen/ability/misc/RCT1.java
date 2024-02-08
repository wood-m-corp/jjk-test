package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;


public class RCT1 extends Ability implements Ability.IChannelened {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return owner.getHealth() < owner.getMaxHealth();
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner instanceof Player player) {
            float healMult = 0.225F;
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            if (cap.hasTrait(Trait.DOCTOR_HOUSE)) {
                healMult *= 2.0F;
            }
            owner.heal((float) Math.min(1.0F, ConfigHolder.SERVER.sorcererHealingAmount.get().floatValue() * Math.pow(this.getPower(owner) * healMult, Math.log(this.getPower(owner))) * healMult));
            }
            else {
            owner.heal((float) Math.min(1.0F, ConfigHolder.SERVER.sorcererHealingAmount.get().floatValue() * Math.pow(this.getPower(owner) * 0.075F, Math.log(this.getPower(owner))) * 0.075F));
            }
    }

    @Override
    public float getCost(LivingEntity owner) {
        if (owner.getHealth() < owner.getMaxHealth()) {
            return (float) Math.min(1.0F, (ConfigHolder.SERVER.sorcererHealingAmount.get().floatValue() * Math.pow(this.getPower(owner), Math.log(this.getPower(owner)))) * this.getMultiplier());
        }
        return 0;
    }

    @Override
    public boolean isUnlockable() {
        return this == JJKAbilities.RCT1.get() || super.isUnlockable();
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        return true;
    }

    @Override
    public boolean canUnlock(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getType() == JujutsuType.SORCERER && super.canUnlock(owner);
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.CURSED_ENERGY_FLOW.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 2.0F);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }

    protected int getMultiplier() {
        return 2;
    }
}
