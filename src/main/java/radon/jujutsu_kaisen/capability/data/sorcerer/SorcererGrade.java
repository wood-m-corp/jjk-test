package radon.jujutsu_kaisen.capability.data.sorcerer;

import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.config.ConfigHolder;


public enum SorcererGrade {
    GRADE_4(0.0F),
    GRADE_3(500.0F),
    SEMI_GRADE_2(1000.0F),
    GRADE_2(1500.0F),
    SEMI_GRADE_1(2000.0F),
    GRADE_1(2500.0F),
    SPECIAL_GRADE_1(3000.0F),
    SPECIAL_GRADE(4000.0F);

    private final float required;

    SorcererGrade(float required) {
        this.required = required;
    }

    public float getRequiredExperience() {
        return this.required;
    }

    public Component getName() {
        return Component.translatable(String.format("grade.%s.%s", JujutsuKaisen.MOD_ID, this.name().toLowerCase()));
    }
}
