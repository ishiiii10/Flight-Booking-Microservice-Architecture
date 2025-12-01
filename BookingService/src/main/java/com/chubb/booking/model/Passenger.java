package com.chubb.booking.model;



import com.chubb.booking.enums.Gender;
import com.chubb.booking.enums.MealType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Passenger {

    @NotBlank(message = "Passenger name is required")
    private String name;

    @Min(value = 0, message = "Passenger age must be non-negative")
    private int age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Meal type is required")
    private MealType mealType;
}