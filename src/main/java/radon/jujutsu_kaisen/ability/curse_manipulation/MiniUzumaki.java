package radon.jujutsu_kaisen.ability.curse_manipulation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.entity.projectile.MiniUzumakiProjectile;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import net.minecraft.world.phys.Vec2;
import radon.jujutsu_kaisen.ability.JJKAbilities;

import java.util.Map;

public class MiniUzumaki extends Ability {
    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null) return false;
        return HelperMethods.RANDOM.nextInt(10) == 0 && owner.hasLineOfSight(target) && owner.distanceTo(target) <= MiniUzumakiProjectile.RANGE;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        MiniUzumakiProjectile uzumaki = new MiniUzumakiProjectile(owner, this.getPower(owner));
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
    public boolean isDisplayed(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        CursedTechnique technique = cap.getTechnique();
        return technique == CursedTechnique.CURSE_MANIPULATION && super.isDisplayed(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0;
    }

    @Override
    public int getPointsCost() {
        return 5;
    }

    @Nullable
    @Override
    public Ability getParent(LivingEntity owner) {
        return JJKAbilities.MAXIMUM_UZUMAKI.get();
    }

    @Override
    public Vec2 getDisplayCoordinates() {
        return new Vec2(2.0F, 3.0F);
    }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }


}
