package radon.jujutsu_kaisen.util;

import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererGrade;
import radon.jujutsu_kaisen.config.ConfigHolder;

public class SorcererUtil {
    public static SorcererGrade getGrade(float experience) {
        SorcererGrade result = SorcererGrade.GRADE_4;

        for (SorcererGrade grade : SorcererGrade.values()) {
            if (experience < grade.getRequiredExperience()) break;

            result = grade;
        }
        return result;
    }

    public static float getPower(float experience) {
        return 1.6F + experience / 1340.0F;
    }

    public static float getDefense(float experience) {
        return 1.0F + experience / 4500.0F;
    }

    public static float getDefenseHR(float experience) {
        return 1.8F + experience / 3600.0F;
    }
}
