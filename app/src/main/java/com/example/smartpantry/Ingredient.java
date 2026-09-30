package com.example.smartpantry;

// Stores the information for one pantry ingredient
public class Ingredient {

    // Ingredient details
    public final long id;
    public final String name, unit, expiry;
    public final double quantity;

    // Create an ingredient using the values from the database or user input
    public Ingredient(long id, String name, double quantity,
                      String unit, String expiry) {

        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }
}