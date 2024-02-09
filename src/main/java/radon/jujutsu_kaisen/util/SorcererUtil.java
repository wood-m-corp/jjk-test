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
        return 2.2F + experience / 2400.0F;
    }
}
