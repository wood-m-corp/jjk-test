package radon.jujutsu_kaisen;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.capability.data.OverlayDataHandler;
import radon.jujutsu_kaisen.capability.data.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CurseGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.JJKEntities;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.entity.base.SummonEntity;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.HelperMethods;

public class JJKEventHandler {
    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap ->
                        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player));

                player.getCapability(OverlayDataHandler.INSTANCE).ifPresent(cap ->
                        cap.sync(player));
            }
        }

        @SubscribeEvent
        public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap ->
                        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player));
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap ->
                        PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), player));
            }
        }

        @SubscribeEvent
        public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
            Player player = event.getEntity();

            player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap ->
                    cap.setEnergy(cap.getMaxEnergy()));
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            Player original = event.getOriginal();
            Player player = event.getEntity();

            original.reviveCaps();

            original.getCapability(SorcererDataHandler.INSTANCE).ifPresent(oldCap -> {
                player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());

                    if (event.isWasDeath()) {
                        newCap.setEnergy(newCap.getMaxEnergy());
                        newCap.resetCooldowns();
                        newCap.resetBurnout();
                        newCap.clearToggled();
                        newCap.setCopied(null);
                    }
                });
            });
            original.invalidateCaps();
        }

        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof LivingEntity entity) {
                if (entity instanceof Player || entity instanceof ISorcerer) {
                    if (!entity.getCapability(SorcererDataHandler.INSTANCE).isPresent()) {
                        SorcererDataHandler.attach(event);
                    }
                    if (!entity.getCapability(OverlayDataHandler.INSTANCE).isPresent()) {
                        OverlayDataHandler.attach(event);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity owner = event.getEntity();
            owner.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> cap.tick(owner));
            owner.getCapability(OverlayDataHandler.INSTANCE).ifPresent(cap -> cap.tick(owner));
        }

        @SubscribeEvent
        public static void onFall(LivingFallEvent event) {
            event.getEntity().getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                if (cap.hasTrait(Trait.HEAVENLY_RESTRICTION)) {
                    float distance = event.getDistance();
                    event.setDistance(distance * 0.25F);
                }
            });
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            LivingEntity victim = event.getEntity();
            LivingEntity source = (LivingEntity) event.getSource().getEntity();

            if (source == null) return;

            victim.getCapability(SorcererDataHandler.INSTANCE).ifPresent(victimCap -> {
                LivingEntity killer;

                if (source instanceof SummonEntity summon) {
                    LivingEntity owner = summon.getOwner();
                    killer = owner == null ? source : owner;
                } else {
                    killer = source;
                }

                killer.getCapability(SorcererDataHandler.INSTANCE).ifPresent(killerCap -> {
                    if (killerCap.isCurse() != victimCap.isCurse()) {
                        CurseGrade grade = CurseGrade.values()[victimCap.getGrade().ordinal()];

                        killerCap.exorcise(killer, grade);

                        if (killer instanceof ServerPlayer player) {
                            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(killerCap.serializeNBT()), player);
                        }
                    }
                });
            });

            if (HelperMethods.RANDOM.nextInt(100) == 0) {
                victim.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                    if (!cap.isCurse() && cap.getGrade().ordinal() >= SorcererGrade.GRADE_1.ordinal()) {
                        if (!cap.hasTrait(Trait.REVERSE_CURSED_TECHNIQUE)) {
                            victim.heal(1.0F);
                            cap.addTrait(Trait.REVERSE_CURSED_TECHNIQUE);
                            event.setCanceled(true);
                        }
                    }
                });
            }
        }
    }

    @Mod.EventBusSubscriber(modid = JujutsuKaisen.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
            JJKEntities.createAttributes(event);
        }

        @SubscribeEvent
        public static void onRegisterSpawnPlacements(SpawnPlacementRegisterEvent event) {
            JJKEntities.registerSpawnPlacements(event);
        }
    }
}
