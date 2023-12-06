package com.sxtreh.user.annotation;

import com.sxtreh.enumeration.ParameterRuleType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterCheck {
    ParameterRuleType rule();
}
