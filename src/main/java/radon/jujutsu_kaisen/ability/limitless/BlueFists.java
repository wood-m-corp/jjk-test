package radon.jujutsu_kaisen.ability.limitless;

import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.RotationUtil;

public class BlueFists extends Ability implements Ability.IToggled {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return target != null && !target.isDeadOrDying() && owner.distanceTo(target) < 10.0D;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public void run(LivingEntity owner) {

    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0.75F;
    }

    @Override
    public void onEnabled(LivingEntity owner) {

    }

    @Override
    public void onDisabled(LivingEntity owner) {

    }

    @Override
    public Classification getClassification() {
        return Classification.BLUE;
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BlueFistsForgeEvents {
        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            DamageSource source = event.getSource();
            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            if (attacker.level().isClientSide) return;

            LivingEntity victim = event.getEntity();

            if (!HelperMethods.isMelee(source)) return;

            if (!JJKAbilities.hasToggled(attacker, JJKAbilities.BLUE_FISTS.get())) return;

            victim.setDeltaMovement(attacker.position().subtract(victim.position()).normalize().multiply(3.0,3.0,3.0));
            victim.hurtMarked = true;

            victim.playSound(SoundEvents.PLAYER_ATTACK_CRIT);

            ((ServerLevel) attacker.level()).getChunkSource().broadcastAndSend(attacker, new ClientboundAnimatePacket(victim, ClientboundAnimatePacket.CRITICAL_HIT));

            if (victim.hurt(JJKDamageSources.jujutsuAttack(attacker, JJKAbilities.BLUE_FISTS.get()), event.getAmount() * 0.75F)) {
                if (victim.isDeadOrDying()) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
