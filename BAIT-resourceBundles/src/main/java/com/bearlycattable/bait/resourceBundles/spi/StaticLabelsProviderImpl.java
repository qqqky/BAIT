// package com.bearlycattable.bait.resourceBundles.spi;
//
// import java.util.Locale;
// import java.util.spi.AbstractResourceBundleProvider;
//
// public class StaticLabelsProviderImpl extends AbstractResourceBundleProvider implements StaticLabelsProvider {
//
//     public StaticLabelsProviderImpl() {
//         super("java.properties");
//     }
//
//     // this provider maps the resource bundle to per-language package
//     @Override
//     protected String toBundleName(String baseName, Locale locale) {
//         System.out.println("--- Provider was reached --- toBundleName() called ---");
//         if (!locale.getLanguage().isEmpty()) {
//             int baseIndex = baseName.lastIndexOf(".");
//             //from
//             //com.bearlycattable.bait.resourceBundles.StaticLabels
//             //to
//             //com.bearlycattable.bait.resourceBundles.en.StaticLabels
//             // return baseName.substring(0, baseIndex) + "." + locale.getLanguage() + baseName.substring(baseIndex);
//             return super.toBundleName("resourceBundles" + "." + locale.getLanguage() + baseName.substring(baseName.lastIndexOf(".")), locale);
//         }
//
//         return super.toBundleName(baseName, locale);
//     }
//
//     // @Override
//     // public ResourceBundle getBundle(String baseName, Locale locale) {
//     //     System.out.println("--- Provider was reached ---");
//     //
//     //     Module module = getClass().getModule();
//     //     // return ResourceBundle.getBundle(baseName, locale, module);
//     //     // if (!locale.getLanguage().isEmpty()) {
//     //     //     int baseIndex = baseName.lastIndexOf(".");
//     //     //     String relocated = baseName.substring(0, baseIndex) + "." + locale.getLanguage() + baseName.substring(baseIndex);
//     //     //
//     //     //     return ResourceBundle.getBundle(relocated, locale, module);
//     //     // }
//     //     //
//     //     return super.getBundle(baseName, locale);
//     // }
//
// }
