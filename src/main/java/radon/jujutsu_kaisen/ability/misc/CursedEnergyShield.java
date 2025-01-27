package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.effect.JJKEffects;

public class CursedEnergyShield extends Ability implements Ability.IChannelened {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel)) return;

        owner.addEffect(new MobEffectInstance(JJKEffects.STUN.get(), 2, 1, false, false, false));
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        return JJKAbilities.hasToggled(owner, JJKAbilities.CURSED_ENERGY_FLOW.get()) && super.isValid(owner) && (owner instanceof Player player);
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 4.0F;
    }

    @Override
    public boolean shouldLog(LivingEntity owner) {
        return true;
    }
}
