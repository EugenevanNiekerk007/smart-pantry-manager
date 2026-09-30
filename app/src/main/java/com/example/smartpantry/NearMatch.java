package com.example.smartpantry;

// Stores a recipe that is almost a match and what is still missing
public class NearMatch {

    public final Recipe recipe;
    public final String missing;

    // Create a near match with the recipe and missing requirement
    public NearMatch(Recipe recipe, String missing) {
        this.recipe = recipe;
        this.missing = missing;
    }
}