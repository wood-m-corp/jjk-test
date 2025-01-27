 package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.particle.MirageParticle;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class Dash extends Ability {
    public static final double RANGE = 80.0D;
    private static final float DASH = 2.0F;
    private static final float MAX_DASH = 3.0F;

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
        if (target == null) return false;
        return owner.hasLineOfSight(target) && owner.distanceTo(target) <= getRange(owner);
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.INSTANT;
    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        if (!canDash(owner)) {
            return Status.FAILURE;
        }
        return super.isTriggerable(owner);
    }

    private static boolean canDash(LivingEntity owner) {
        if (owner.hasEffect(JJKEffects.STUN.get())) return false;

        boolean collision = false;

        AABB bounds = owner.getBoundingBox();
        Cursor3D cursor = new Cursor3D(Mth.floor(bounds.minX - 1.0E-7D) - 2,
                Mth.floor(bounds.minY - 1.0E-7D) - 2,
                Mth.floor(bounds.minZ - 1.0E-7D) - 2,
                Mth.floor(bounds.maxX + 1.0E-7D) + 2,
                Mth.floor(bounds.maxY + 1.0E-7D) + 2,
                Mth.floor(bounds.maxZ + 1.0E-7D) + 2);

        while (cursor.advance()) {
            int i = cursor.nextX();
            int j = cursor.nextY();
            int k = cursor.nextZ();
            int l = cursor.getNextType();

            if (l == 3) continue;

            BlockState state = owner.level().getBlockState(new BlockPos(i, j, k));

            if (!state.isAir()) {
                collision = true;
                break;
            }
        }
        return collision || owner.getXRot() >= 15.0F;
    }

    private static float getRange(LivingEntity owner) {
        return (float) (RANGE * (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION) ? 1.5F : 1.0F));
    }

   private Vec3 getTarget(LivingEntity owner) {
        Vec3 start = owner.getEyePosition();
        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);
        Vec3 end = start.add(look.scale(RANGE));
        HitResult result = RotationUtil.getHitResult(owner, start, end);
        return result.getType() == HitResult.Type.MISS ? end : result.getLocation();
    }

    @Override
    public void run(LivingEntity owner) {
        if (!(owner.level() instanceof ServerLevel level)) return;

        if (!canDash(owner)) return;

        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.getSpeedStacks() > 0 || cap.hasTrait(Trait.HEAVENLY_RESTRICTION)) {
            owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.DASH.get(), SoundSource.MASTER, 1.0F, 1.0F);
            owner.addEffect(new MobEffectInstance(JJKEffects.INVISIBILITY.get(), 3, 0, false, false, false));
            level.sendParticles(new MirageParticle.MirageParticleOptions(owner.getId()), owner.getX(), owner.getY(), owner.getZ(),
                    0, 0.0D, 0.0D, 0.0D, 1.0D);
        }

        Vec3 look = RotationUtil.getTargetAdjustedLookAngle(owner);

        HitResult hit = RotationUtil.getLookAtHit(owner, getRange(owner));

        float power = Math.min(MAX_DASH,
                DASH * (1.0F + this.getPower(owner) * 0.1F));
        Vec3 target = this.getTarget(owner);
        Vec3 velocity = target.subtract(owner.position()).normalize().scale(power);
        velocity = velocity.multiply(new Vec3(1.2D, 1.0D, 1.2D));
        if (velocity.y > 0) {
           velocity = velocity.multiply(new Vec3(1.0D, 0.7D, 1.0D));
        }
        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION)) {
            velocity = velocity.multiply(new Vec3(1.5D, 1.25D, 1.5D)).add(new Vec3(0.0D, 0.05D,0.0D));
        }
        velocity = velocity.add(new Vec3(0.0D,0.2D,0.0D));
        if (!owner.onGround() && owner.level().getBlockState(owner.blockPosition()).getFluidState().isEmpty()) {
           velocity = velocity.add(new Vec3(0.0D,-0.75D,0.0D));
        }
        owner.setDeltaMovement(velocity);
        /*if (hit.getType() == HitResult.Type.MISS) {
            float f = owner.getYRot();
            float f1 = owner.getXRot();
            float f2 = -Mth.sin(f * ((float) Math.PI / 180.0F)) * Mth.cos(f1 * ((float) Math.PI / 180.0F));
            float f3 = -Mth.sin(f1 * ((float) Math.PI / 180.0F));
            float f4 = Mth.cos(f * ((float) Math.PI / 180.0F)) * Mth.cos(f1 * ((float) Math.PI / 180.0F));
            float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
            f2 *= power / f5;
            f3 *= power / f5;
            f4 *= power / f5;
            owner.push(f2, f3, f4);
            owner.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
        } else {
            Vec3 target = hit.getLocation();

            double distanceX = target.x - owner.getX();
            double distanceY = target.y - owner.getY();
            double distanceZ = target.z - owner.getZ();

            double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            double motionX = distanceX / distance * power;
            double motionY = distanceY / distance * power;
            double motionZ = distanceZ / distance * power;
            owner.setDeltaMovement(motionX, motionY, motionZ);
        }*/
        owner.hurtMarked = true;

        Vec3 pos = owner.position();

        for (int i = 0; i < 32; i++) {
            double xPos = owner.getX() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).x;
            double yPos= owner.getY() + HelperMethods.RANDOM.nextDouble() * (owner.getBbHeight());
            double zPos = owner.getZ() + (HelperMethods.RANDOM.nextDouble() - 0.5D) * (owner.getBbWidth() * 1.25F) - owner.getLookAngle().scale(0.35D).z;
            double theta = HelperMethods.RANDOM.nextDouble() * 2 * Math.PI;
            double phi = HelperMethods.RANDOM.nextDouble() * Math.PI;
            double r = HelperMethods.RANDOM.nextDouble() * 0.2D;
            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.sin(phi) * Math.sin(theta);
            double z = r * Math.cos(phi);
            Vec3 speed = look.add(x, y, z).reverse();
            level.sendParticles(ParticleTypes.CLOUD, xPos, yPos, zPos, 0, speed.x, speed.y, speed.z, 1.0D);
        }
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        return (!(owner instanceof ISorcerer sorcerer) || sorcerer.canJump()) && super.isValid(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0;
    }

    @Override
    public int getCooldown() {
        return 25;
    }

    @Override
    public int getRealCooldown(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION)) {
            return 2;
        }
        return super.getRealCooldown(owner);
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.NONE;
    }
}
