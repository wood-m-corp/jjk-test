package radon.jujutsu_kaisen.ability.misc;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.client.particle.CursedEnergyParticle;
import radon.jujutsu_kaisen.client.particle.ParticleColors;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;


public class Heal extends Ability implements Ability.IChannelened {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return owner.getHealth() < owner.getMaxHealth();
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
    }

    @Override
    public void run(LivingEntity owner) {
        if (owner instanceof Player player) {
            owner.heal((float) Math.min(1.0F, ConfigHolder.SERVER.curseHealingAmount.get().floatValue() * Math.pow(this.getPower(owner) * 0.225F, Math.log(this.getPower(owner))) * 0.225F));
        }
        else {
            owner.heal((float) Math.min(1.0F, ConfigHolder.SERVER.curseHealingAmount.get().floatValue() * Math.pow(this.getPower(owner) * 0.075F, Math.log(this.getPower(owner))) * 0.075F));
        }
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        if (!(owner.level() instanceof ServerLevel level)) return;
        for (int i = 0; i < 2; i++) {
            cap.delayTickEvent(() -> {
                for (int j = 0; j < 2; j++) {
                    double x = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).x;
                    double y = owner.getY() + HelperMethods.RANDOM.nextDouble() * (owner.getBbHeight());
                    double z = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).z;
                    double speed = (owner.getBbHeight() * 0.1F) * HelperMethods.RANDOM.nextDouble();
                    level.sendParticles(new CursedEnergyParticle.CursedEnergyParticleOptions(ParticleColors.RCT, owner.getBbWidth() * 0.5F,
                               0.2F, 16), x, y, z, 0, 0.0D, speed, 0.0D, 1.0D);
                }
            }, i * 2);
         }
    }

    @Override
    public float getCost(LivingEntity owner) {
        if (owner.getHealth() < owner.getMaxHealth()) {
            return (float) Math.min(2.0F, (ConfigHolder.SERVER.curseHealingAmount.get().floatValue() * Math.pow(this.getPower(owner), Math.log(this.getPower(owner))))*3.0F);
        }
        return 0.0F;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.getType() == JujutsuType.CURSE && super.isValid(owner);
    }
}
