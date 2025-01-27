package radon.jujutsu_kaisen.ability.idle_transfiguration;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
import radon.jujutsu_kaisen.effect.JJKEffects;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.util.EntityUtil;
import radon.jujutsu_kaisen.util.HelperMethods;

public class IdleTransfiguration extends Ability implements Ability.IToggled, Ability.IAttack {
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
        return ActivationType.TOGGLED;
    }

    public static float calculateStrength(LivingEntity entity) {
        float strength = Math.min(1.0F,entity.getHealth());
        if (!(entity instanceof Player player) && (strength <= 30.0F)) {
            strength = 1.0F;
        }

        if (entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
            if (entity.level().isClientSide) {
                ClientVisualHandler.ClientData data = ClientVisualHandler.get(entity);

                if (data != null) {
                    strength += data.experience * 0.1F;
                }
            } else {
                ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                strength += cap.getExperience() * 0.1F;
            }
        }
        return strength;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public Status isTriggerable(LivingEntity owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        if (cap.hasToggled(JJKAbilities.SOUL_DECIMATION.get())) {
            cap.toggle(JJKAbilities.SOUL_DECIMATION.get());
        }
        return super.isTriggerable(owner);
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 60.0F;
    }

    @Override
    public int getCooldown() {
        return 5 * 20;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }

    public static void absorb(LivingEntity owner, LivingEntity target) {
        ItemStack stack = new ItemStack(JJKItems.TRANSFIGURED_SOUL.get());
        
        if (owner instanceof Player player) {
            player.addItem(stack);
        } else {
            owner.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }

        EntityUtil.makePoofParticles(target);

        if (!(target instanceof Player)) {
            target.discard();
        } else {
            target.kill();
        }
    }

    @Override
    public boolean attack(DamageSource source, LivingEntity owner, LivingEntity target) {
        if (owner.level().isClientSide) return false;
        if (!HelperMethods.isMelee(source)) return false;
        //if (!owner.getMainHandItem().isEmpty()) return false;

        MobEffectInstance existing = target.getEffect(JJKEffects.TRANSFIGURED_SOUL.get());

        int amplifier = 0;

        if (existing != null) {
            amplifier = existing.getAmplifier() + 1;
        }

        MobEffectInstance instance = new MobEffectInstance(JJKEffects.TRANSFIGURED_SOUL.get(), 15 * 20, amplifier, false, true, true);
        target.addEffect(instance);

        if (!owner.level().isClientSide) {
            PacketDistributor.TRACKING_ENTITY.with(() -> target).send(new ClientboundUpdateMobEffectPacket(target.getId(), instance));
        }

        float attackerStrength = IdleTransfiguration.calculateStrength(owner);
        float victimStrength = IdleTransfiguration.calculateStrength(target);

        int required = Math.round((victimStrength / Math.round(attackerStrength*2/11)) * 2);
        if (victimStrength <= 1.0F) {
            required = 0;
        }

        if (amplifier >= required) {
            if ((target instanceof Mob && !(target instanceof Monster)) || target instanceof Player) {
                absorb(owner, target);
                return true;
            }
        }

        if (!owner.level().isClientSide) {
            PacketDistributor.TRACKING_ENTITY.with(() -> target).send(new ClientboundUpdateMobEffectPacket(target.getId(), instance));
        }
        return true;
    }
}
