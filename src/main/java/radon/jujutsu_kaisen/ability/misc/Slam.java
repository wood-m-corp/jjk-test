package radon.jujutsu_kaisen.ability.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ExplosionHandler;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.client.ClientWrapper;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.sound.JJKSounds;
import radon.jujutsu_kaisen.util.RotationUtil;

import java.util.*;

public class Slam extends Ability implements Ability.ICharged {
    private static final double RANGE = 30.0D;
    private static final double LAUNCH_POWER = 2.0D;
    private static final float MAX_EXPLOSION = 5.5F;

    public static Map<UUID, Float> TARGETS = new HashMap<>();

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        if (target == null || target.isDeadOrDying()) return false;
        return owner.hasLineOfSight(target);
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.CHANNELED;
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
        if (!(owner instanceof Player) || !owner.level().isClientSide) return;
        ClientWrapper.setOverlayMessage(Component.translatable(String.format("chat.%s.charge", JujutsuKaisen.MOD_ID),
                Math.round(((float) Math.min(20, this.getCharge(owner)) / 20) * 100)), false);
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        return (!(owner instanceof ISorcerer sorcerer) || sorcerer.hasMeleeAttack() && sorcerer.canJump()) && super.isValid(owner);
    }

    @Override
    public Status isStillUsable(LivingEntity owner) {
        if (owner.hasEffect(JJKEffects.STUN.get())) {
            return Status.FAILURE;
        }
        return super.isStillUsable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION) ? 0.0F : 30.0F;
    }

    public int getCooldown() {
        return 10 * 20;
    }

    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean isMelee() {
        return true;
    }

    @Override
    public MenuType getMenuType() {
        return MenuType.MELEE;
    }

    public static void onHitGround(LivingEntity owner, float distance) {
        slamCrater(owner,distance);
    }

    public static void slamCrater(LivingEntity owner, float distance) {
        if (owner.level().isClientSide) return;

        float radius = Math.min(MAX_EXPLOSION, 2.0F+7.5F * TARGETS.get(owner.getUUID()));
        float dmgMult = 0.55F;
        if (JJKAbilities.hasTrait(owner, Trait.HEAVENLY_RESTRICTION)) {
            dmgMult = 0.75F;
            radius*=1.35F;
        }
        owner.swing(InteractionHand.MAIN_HAND);

        if (!owner.level().isClientSide) {
            ExplosionHandler.spawn(owner.level().dimension(), owner.position(), radius, 5, Ability.getPower(JJKAbilities.SLAM.get(), owner) * dmgMult, owner,
                    owner instanceof Player player ? owner.damageSources().playerAttack(player) : owner.damageSources().mobAttack(owner), false);
        }
        owner.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), JJKSounds.SLAM.get(), SoundSource.MASTER, 1.0F, 1.0F);

        TARGETS.remove(owner.getUUID());
    }

    @Override
    public boolean onRelease(LivingEntity owner) {
        double launchPower = 2.0D + (2.0D * (Math.min(20, this.getCharge(owner)) / 20));
        if (!owner.onGround()) {
            if (!owner.level().isClientSide) {
                TARGETS.put(owner.getUUID(), ((float) Math.min(20, this.getCharge(owner)) / 20));
            }
            
            ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            Vec3 target = this.getTarget(owner);
            Vec3 velocity = owner.getDeltaMovement().add(target.subtract(owner.position()).normalize().scale(launchPower));
            if (velocity.y > 0) {
                velocity.multiply(1.0D, 0.5D, 1.0D);
            }
            owner.setDeltaMovement(velocity);
            owner.swing(InteractionHand.MAIN_HAND);
            cap.delayTickEvent(() -> {
                TARGETS.remove(owner.getUUID());
            }, 20*3);
        }
        else {
            if (owner.isShiftKeyDown()) {
                if (!owner.level().isClientSide) {
                    TARGETS.put(owner.getUUID(), ((float) Math.min(20, this.getCharge(owner)) / 20));
                }
                owner.swing(InteractionHand.MAIN_HAND);
                slamCrater(owner,1);
            }
            else {
                Vec3 direction = new Vec3(0.0D, Math.min(2.0D,launchPower*0.75D), 0.0D);
                owner.setDeltaMovement(owner.getDeltaMovement().add(direction));
        
                if (!owner.level().isClientSide) {
                    TARGETS.put(owner.getUUID(), ((float) Math.min(20, this.getCharge(owner)) / 20));
                }
        
                ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        
                cap.delayTickEvent(() -> {
                    Vec3 target = this.getTarget(owner);
                    Vec3 velocity = owner.getDeltaMovement().add(target.subtract(owner.position()).normalize().scale(launchPower));
                    if (velocity.y > 0) {
                        velocity.multiply(1.0D, 0.5D, 1.0D);
                    }
                    owner.setDeltaMovement(owner.getDeltaMovement().add(target.subtract(owner.position()).normalize().scale(launchPower)).multiply(1.0D, 0.5D, 1.0D));
                    cap.delayTickEvent(() -> {
                        TARGETS.remove(owner.getUUID());
                    }, 20*3);
                }, 20);
            }
        }
        return true;
    }
}
