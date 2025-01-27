package radon.jujutsu_kaisen.ability.boogie_woogie;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.entity.projectile.CursedEnergyImbuedItemProjectile;
import radon.jujutsu_kaisen.sound.JJKSounds;

public class ItemSwap extends Ability {
    public static final double RANGE = 150.0D;

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    private @Nullable Entity getTarget(LivingEntity owner) {
        Entity target = null;

        for (Entity entity : owner.level().getEntitiesOfClass(CursedEnergyImbuedItemProjectile.class, AABB.ofSize(owner.position(), RANGE, RANGE, RANGE))) {
            if (target == null || entity.distanceTo(owner) < target.distanceTo(owner)) {
                target = entity;
            }
        }
        return target;
    }

    @Override
    public void run(LivingEntity owner) {
        owner.swing(InteractionHand.MAIN_HAND);

        if (owner.level().isClientSide) return;

        Entity target = this.getTarget(owner);

        if (target != null) {
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.CLAP.get(), SoundSource.MASTER, 2.0F, 1.0F);
            owner.level().playSound(null, target.getX(), target.getY(), target.getZ(), JJKSounds.CLAP.get(), SoundSource.MASTER, 1.0F, 1.0F);

            Vec3 pos = target.position();

            Vec2 ownerRot = owner.getRotationVector();
            Vec2 targetRot = target.getRotationVector();

            target.teleportTo(owner.getX(), owner.getY(), owner.getZ());
            owner.teleportTo(pos.x, pos.y, pos.z);

            target.setYRot(ownerRot.y);
            target.setXRot(ownerRot.x);

            owner.setYRot(targetRot.y);
            owner.setXRot(targetRot.x);
        }
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 5.0F;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        Entity target = this.getTarget(owner);

        if (target == null) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }
}
