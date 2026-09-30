package com.example.smartpantry;

import java.util.Locale;

// Helper class used to compare ingredient names and measurement units
public final class UnitConverter {

    // Prevent this helper class from being created as an object
    private UnitConverter() {}

    // Put units into groups so compatible measurements can be compared
    public static String group(String unit) {

        switch (unit.toLowerCase(Locale.ROOT)) {

            case "g":
            case "kg":
                return "mass";

            case "ml":
            case "l":
                return "volume";

            case "piece":
                return "count";

            default:
                return "unknown";
        }
    }

    // Convert larger units into the base unit used for comparison
    public static double base(double quantity, String unit) {

        switch (unit.toLowerCase(Locale.ROOT)) {

            // Kilograms become grams and litres become millilitres
            case "kg":
            case "l":
                return quantity * 1000;

            default:
                return quantity;
        }
    }

    // Make ingredient names easier to compare
    public static String normalize(String name) {

        // Remove extra spaces and make the name lowercase
        String value = name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        // Change common plural ingredient names to singular
        switch (value) {

            case "tomatoes":
                return "tomato";

            case "potatoes":
                return "potato";

            case "eggs":
                return "egg";

            case "onions":
                return "onion";

            case "bananas":
                return "banana";

            case "apples":
                return "apple";

            case "carrots":
                return "carrot";

            case "leaves":
                return "leaf";

            default:

                // Change words ending in "ies" to a singular form
                if (value.endsWith("ies")
                        && value.length() > 4) {

                    return value.substring(
                            0,
                            value.length() - 3
                    ) + "y";
                }

                // Remove a final "s" from simple plural words
                if (value.endsWith("s")
                        && !value.endsWith("ss")
                        && !value.endsWith("us")
                        && !value.endsWith("is")
                        && value.length() > 3) {

                    return value.substring(
                            0,
                            value.length() - 1
                    );
                }

                return value;
        }
    }

    // Format quantities so they display neatly to the user
    public static String number(double value) {

        // Show whole numbers without decimal places
        if (value == Math.rint(value)) {

            return String.format(
                    Locale.ROOT,
                    "%.0f",
                    value
            );
        }

        // Remove unnecessary zeros from decimal values
        return String.format(
                        Locale.ROOT,
                        "%.2f",
                        value
                )
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }
}