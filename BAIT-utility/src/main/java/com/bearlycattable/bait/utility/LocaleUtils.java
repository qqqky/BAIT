package com.bearlycattable.bait.utility;

import java.util.Locale;
import java.util.ResourceBundle;

import com.bearlycattable.bait.commons.BaitConstants;

public class LocaleUtils {

    public static final Locale APP_LANGUAGE = readLocaleFromConfig();

    private static Locale readLocaleFromConfig() {
        ResourceBundle rb = ResourceBundle.getBundle("config");
        Locale locale;
        if (rb == null) {
            System.out.println("Config file wasn't found. Will default to EN_US");
            return BaitConstants.EN_US;
        } else {
            try {
                locale = new Locale(rb.getString("app.language"));
                System.out.println("App language [" + locale.getLanguage() + "] has been successfully read and loaded from config file");
            } catch (Exception e) {
                e.printStackTrace();
                return BaitConstants.EN_US;
            }
        }
        return locale;
    }
}
