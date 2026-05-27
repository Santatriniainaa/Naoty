package com.naoty.gradebook.model;

public enum Mention {
    INSUFFISANT,
    PASSABLE,
    ASSEZ_BIEN,
    BIEN,
    TRES_BIEN,
    EXCELLENT;

    public static Mention from(double moyenne, MentionThresholds t) {
        if (moyenne >= t.excellent()) return EXCELLENT;
        if (moyenne >= t.tresBien())  return TRES_BIEN;
        if (moyenne >= t.bien())      return BIEN;
        if (moyenne >= t.assezBien()) return ASSEZ_BIEN;
        if (moyenne >= t.passable())  return PASSABLE;
        return INSUFFISANT;
    }

    public record MentionThresholds(
            double passable,
            double assezBien,
            double bien,
            double tresBien,
            double excellent
    ) {}
}
