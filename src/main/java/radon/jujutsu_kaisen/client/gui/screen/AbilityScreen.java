package radon.jujutsu_kaisen.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.ability.MenuType;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.ISorcererData;
import radon.jujutsu_kaisen.capability.data.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.client.JJKKeys;
import radon.jujutsu_kaisen.client.gui.screen.base.RadialScreen;

import java.util.*;

public class AbilityScreen extends RadialScreen {
    @Override
    protected List<DisplayItem> getItems() {
        assert this.minecraft != null && this.minecraft.level != null && this.minecraft.player != null;

        if (!this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return List.of();
        ISorcererData cap = this.minecraft.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        List<Ability> abilities = JJKAbilities.getAbilities(this.minecraft.player);
        abilities.removeIf(ability -> ability.getMenuType() != MenuType.RADIAL);

        List<DisplayItem> items = new ArrayList<>(abilities.stream().map(DisplayItem::new).toList());

        Map<EntityType<?>, Integer> curses = cap.getCurses(this.minecraft.level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE));
        items.addAll(curses.entrySet().stream().map(entry -> new DisplayItem(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))).toList());

        Set<CursedTechnique> copied = cap.getCopied();
        items.addAll(copied.stream().map(technique -> new DisplayItem(DisplayItem.Type.COPIED, technique)).toList());

        Set<CursedTechnique> absorbed = cap.getAbsorbed();
        items.addAll(absorbed.stream().map(technique -> new DisplayItem(DisplayItem.Type.ABSORBED, technique)).toList());

        return items;
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTicks);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int x = centerX;
        int y = centerY - RADIUS_OUT - this.font.lineHeight * 2;

        pGuiGraphics.drawCenteredString(this.font, Component.translatable(String.format("gui.%s.ability.right_click", JujutsuKaisen.MOD_ID)), x, y, 16777215);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == JJKKeys.SHOW_ABILITY_MENU.getKey().getValue()) {
            this.onClose();
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }
}