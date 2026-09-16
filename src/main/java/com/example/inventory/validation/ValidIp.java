package com.example.inventory.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IpValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIp {

    String message() default "must be a valid IP address";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}