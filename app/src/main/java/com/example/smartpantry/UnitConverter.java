package com.example.smartpantry;

import java.util.Locale;

public final class UnitConverter {
    private UnitConverter() {}

    // Pantry and recipe quantities become comparable base units: grams, milliliters, or count.
    public static String group(String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "g": case "kg": return "mass";
            case "ml": case "l": return "volume";
            case "piece": return "count";
            default: return "unknown";
        }
    }

    public static double base(double quantity, String unit) {
        switch (unit.toLowerCase(Locale.ROOT)) {
            case "kg": case "l": return quantity * 1000;
            default: return quantity;
        }
    }

    // Simple real-world normalization; no NLP or quantity guessing.
    public static String normalize(String name) {
        String value = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        switch (value) {
            case "tomatoes": return "tomato";
            case "potatoes": return "potato";
            case "eggs": return "egg";
            case "onions": return "onion";
            case "bananas": return "banana";
            case "apples": return "apple";
            case "carrots": return "carrot";
            case "leaves": return "leaf";
            default:
                if (value.endsWith("ies") && value.length() > 4)
                    return value.substring(0, value.length() - 3) + "y";
                if (value.endsWith("s") && !value.endsWith("ss")
                        && !value.endsWith("us") && !value.endsWith("is")
                        && value.length() > 3)
                    return value.substring(0, value.length() - 1);
                return value;
        }
    }

    public static String number(double value) {
        if (value == Math.rint(value)) return String.format(Locale.ROOT, "%.0f", value);
        return String.format(Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
