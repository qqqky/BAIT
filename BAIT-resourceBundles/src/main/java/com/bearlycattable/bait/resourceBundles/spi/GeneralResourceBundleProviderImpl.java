package com.bearlycattable.bait.resourceBundles.spi;

import java.util.Locale;
import java.util.spi.AbstractResourceBundleProvider;

public class GeneralResourceBundleProviderImpl extends AbstractResourceBundleProvider
        implements ConstructionTabProvider, ConverterTabProvider, HeatComparisonTabProvider,
            QuickSearchTabProvider, AdvancedTabPageHelperProvider, AdvancedSubTabConfigProvider,
            AdvancedSubTabLogProvider, AdvancedSubTabProgressProvider, AdvancedSubTabResultsProvider,
            AdvancedSubTabSearchProvider, AdvancedSubTabToolsProvider, StaticLabelsProvider,
            BundleTestProvider {

    public GeneralResourceBundleProviderImpl() {
        super("java.properties");
        // System.out.println("GeneralResourceBundleProviderImpl has been initialized");
    }

    // this provider maps the resource bundle to per-language package
    @Override
    protected String toBundleName(String baseName, Locale locale) {
        // System.out.println("Trying to get bundle for basename: " + baseName + " (for locale: " + locale + ")");
        if (!locale.getLanguage().isEmpty()) {
            int baseIndex = baseName.lastIndexOf(".");
            String newBaseName = "resourceBundles" + "." + locale.getLanguage() + baseName.substring(baseIndex);

            // if (!isAccessible(newBaseName, locale)) {
            //     throw new IllegalStateException("Make sure the properties file for bundle '" + baseName.substring(baseIndex + 1) + "' exists, is accessible and properly named for your selected language at: resourceBundles/" + locale.getLanguage() + "/" + baseName.substring(baseIndex + 1) + "_" + locale.getLanguage() + ".properties");
            // }
            // System.out.println("Basename for bundle has been rewritten to: " + newBaseName);
            return super.toBundleName(newBaseName, locale);
        }

        // System.out.println("Basename for this bundle has not been rewritten");
        return super.toBundleName(baseName, locale);
    }

    private boolean isAccessible(String newBaseName, Locale locale) {
        if (newBaseName == null || locale == null) {
            return false;
        }
        // ResourceBundle.getBundle("resourceBundles/en/StaticLabels", LocaleUtils.APP_LANGUAGE)
        String item = newBaseName.replace(".", "/");
        return ClassLoader.getSystemResource(item + "_" + locale.getLanguage() + ".properties") != null;
    }
}
