package radon.jujutsu_kaisen.ability.curse_manipulation;

import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.entity.projectile.MaximumUzumakiProjectile;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import net.minecraft.world.phys.Vec2;
import radon.jujutsu_kaisen.sound.JJKSounds;

public class MaximumUzumaki extends Ability {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return target != null && !target.isDeadOrDying() && owner.hasLineOfSight(target) &&
                (cap.getType() == JujutsuType.CURSE || cap.isUnlocked(JJKAbilities.RCT1.get()) ? owner.getHealth() / owner.getMaxHealth() < 0.9F : owner.getHealth() / owner.getMaxHealth() < 0.4F);
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.GHAST_WARN, SoundSource.MASTER, 3.0F, 0.5F);
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.MASTER, 2.5F, 1.0F);
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.MASTER, 2.0F, 0.75F);
        MaximumUzumakiProjectile uzumaki = new MaximumUzumakiProjectile(owner, this.getPower(owner));
        owner.level().addFreshEntity(uzumaki);
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (!cap.hasSummonOfClass(CursedSpirit.class)) {
            return false;
        }
        return super.isValid(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0;
    }

    @Override
    public int getPointsCost() {
        return 50;
    }

    @Override
    public boolean isDisplayed(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return technique == CursedTechnique.CURSE_MANIPULATION && super.isDisplayed(owner);
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(1.0F, 3.0F);
    }

    @Override
    public int getCooldown() {
        return 30 * 20;
    }
}
