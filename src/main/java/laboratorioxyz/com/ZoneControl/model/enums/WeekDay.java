package laboratorioxyz.com.ZoneControl.model.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Días de la semana para los turnos/horarios por día de un permiso
 * (3.2 §9, HU-26). Nombres abreviados en español (LUN..DOM).
 */
public enum WeekDay {
    LUN, MAR, MIE, JUE, VIE, SAB, DOM;

    public static WeekDay from(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> LUN;
            case TUESDAY -> MAR;
            case WEDNESDAY -> MIE;
            case THURSDAY -> JUE;
            case FRIDAY -> VIE;
            case SATURDAY -> SAB;
            case SUNDAY -> DOM;
        };
    }

    public static WeekDay today() {
        return from(LocalDate.now().getDayOfWeek());
    }
}
