package com.bearlycattable.bait.utility;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BundleUtils {

    static {
        GLOBAL_BASE_NAME = deriveBaseNameForCurrentBuild();
        MODULAR = deriveExpectedModularity();
    }

    public static final String GLOBAL_BASE_NAME;
    public static final boolean MODULAR;

    private static String deriveBaseNameForCurrentBuild() {
        String result;
        System.out.println("Testing resource acquisition paths:");

        if ((result = testModularizedPaths(LocaleUtils.APP_LANGUAGE)) == null) {
            result = testNakedPaths(LocaleUtils.APP_LANGUAGE);
        }

        return result;
    }

    private static String testModularizedPaths(Locale locale) {
        String currentName = "com.bearlycattable.bait.resourceBundles.BundleTest";
        ResourceBundle rb = null;
        int currentIndex;

        do {
            try {
                rb = ResourceBundle.getBundle(currentName, locale);
            } catch (MissingResourceException | IllegalStateException e) {
                //swallow
            }
            if (rb != null) {
                System.out.println("Test resource bundle successfully acquired via Provider (app is properly modularized).");
                return currentName.substring(0, currentName.lastIndexOf(".") + 1);
            }

            currentIndex = currentName.indexOf(".");
            currentName = currentName.substring(currentIndex + 1);
        } while (currentIndex > -1);

        return null;
    }

    private static String testNakedPaths(Locale locale) {
        String currentName = "com.bearlycattable.bait.resourceBundles." + locale.getLanguage() + ".BundleTest";
        ResourceBundle rb = null;
        int currentIndex;

        do {
            try {
                rb = ResourceBundle.getBundle(currentName, locale);
            } catch (MissingResourceException e) {
                //swallow
            }
            if (rb != null) {
                System.out.println("Test resource bundle has been acquired directly (Provider was ignored).");
                return currentName.substring(0, currentName.lastIndexOf(".") + 1);
            }

            currentIndex = currentName.indexOf(".");
            currentName = currentName.substring(currentIndex + 1);
        } while (currentIndex > -1);

        return null;
    }

    private static boolean deriveExpectedModularity() {
        return GLOBAL_BASE_NAME != null && GLOBAL_BASE_NAME.contains(LocaleUtils.APP_LANGUAGE.getLanguage());
    }

}
