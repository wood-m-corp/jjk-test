package radon.jujutsu_kaisen;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.config.ConfigHolder;
import radon.jujutsu_kaisen.damage.JJKDamageSources;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.entity.effect.BlackFlashEntity;
import net.minecraft.world.entity.player.Player;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;
import radon.jujutsu_kaisen.util.SorcererUtil;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedEnergyNature;

public class BlackFlashHandler {
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BlackFlashHandlerForgeEvents {
        private static final float MAX_DAMAGE = 30.0F;

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onLivingDamage(LivingDamageEvent event) {
            DamageSource source = event.getSource();
            if (!(source.getEntity() instanceof LivingEntity attacker)) return;

            LivingEntity victim = event.getEntity();

            if (victim.level().isClientSide) return;

            if (!HelperMethods.isMelee(source)) return;

            if (!attacker.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;
            ISorcererData cap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            if (attacker instanceof ISorcerer sorcerer && !sorcerer.hasArms()) return;

            if (SorcererUtil.getGrade(cap.getExperience()).ordinal() < SorcererGrade.SEMI_GRADE_2.ordinal() ||
                    (!(source instanceof JJKDamageSources.JujutsuDamageSource) && !cap.hasToggled(JJKAbilities.CURSED_ENERGY_FLOW.get()) && !cap.hasToggled(JJKAbilities.BLUE_FISTS.get()))) return;

            long lastBlackFlashTime = cap.getLastBlackFlashTime();
            int seconds = (int) (attacker.level().getGameTime() - lastBlackFlashTime) / 20;

            if (lastBlackFlashTime == 0 || seconds >= 1) {
                int rng = 200;
                if (cap.getNature() == CursedEnergyNature.DIVERGENT) {
                    rng = 175;
                }
                if ((attacker instanceof Player player) && (cap.isInZone())) rng = 30;
                if (HelperMethods.RANDOM.nextInt(rng) != 0) return;
            } else {
                return;
            }
            cap.onBlackFlash();

            if (attacker instanceof ServerPlayer player) {
                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player);
            }

            event.setAmount(Math.min(MAX_DAMAGE, (float) Math.pow(event.getAmount(), 2.5D)));

            attacker.level().addFreshEntity(new BlackFlashEntity(attacker, victim));

            victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.MASTER, 2.0F, 0.8F + HelperMethods.RANDOM.nextFloat() * 0.2F);
            victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.MASTER, 1.0F, 0.5F + HelperMethods.RANDOM.nextFloat() * 0.2F);
        }
    }
}
