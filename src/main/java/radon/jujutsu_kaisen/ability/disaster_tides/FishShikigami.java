package radon.jujutsu_kaisen.ability.disaster_tides;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.entity.projectile.base.FishShikigamiProjectile;
import radon.jujutsu_kaisen.entity.projectile.EelShikigamiProjectile;
import radon.jujutsu_kaisen.entity.projectile.PiranhaShikigamiProjectile;
import radon.jujutsu_kaisen.entity.projectile.SharkShikigamiProjectile;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class FishShikigami extends Ability {
    public static final double RANGE = 40.0D;
    public LivingEntity enemy = null;



    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return target != null && !target.isDeadOrDying() && this.getTarget(owner) == target;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private @Nullable LivingEntity getTarget(LivingEntity owner) {
        LivingEntity result = null;

        LivingEntity target = (RotationUtil.getExpandedLookAt(owner, RANGE));
        if (target != null) {
            if (owner.canAttack(target)) {
                result = target;
            }
        }
        return result;
    }

    @Override
    public void run(LivingEntity owner) {
        LivingEntity target = this.enemy;

        float xOffset = (HelperMethods.RANDOM.nextFloat() - 0.5F) * 5.0F;
        float yOffset = owner.getBbHeight() + ((HelperMethods.RANDOM.nextFloat() - 0.5F) * 5.0F);

        FishShikigamiProjectile[] projectiles = new FishShikigamiProjectile[]{
                new EelShikigamiProjectile(owner, this.getPower(owner) * 1.1F, target, xOffset, yOffset),
                new SharkShikigamiProjectile(owner, this.getPower(owner) * 1.1F, target, xOffset, yOffset),
                new PiranhaShikigamiProjectile(owner, getPower(owner) * 1.1F, target, xOffset, yOffset)
        };
        owner.level().addFreshEntity(projectiles[HelperMethods.RANDOM.nextInt(projectiles.length)]);
        this.enemy = null;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        LivingEntity target = this.getTarget(owner);
        this.enemy = target;

        if (target == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 80.0F;
    }

    @Override
    public int getCooldown() {
        return 7 * 20;
    }



    @Override
    public MenuType getMenuType() {
        return MenuType.MELEE;
    }
}
