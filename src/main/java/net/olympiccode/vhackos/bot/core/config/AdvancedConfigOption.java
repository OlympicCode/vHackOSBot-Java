package net.olympiccode.vhackos.bot.core.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AdvancedConfigOption {
    String path();
    String defaultValue();
    String[] options();
}
