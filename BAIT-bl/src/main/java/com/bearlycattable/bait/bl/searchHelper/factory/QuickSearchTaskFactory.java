// package com.bearlycattable.bait.bl.searchHelper.factory;
//
// import java.util.List;
// import java.util.Optional;
// import java.util.function.BiFunction;
// import java.util.function.BiPredicate;
// import java.util.function.Function;
//
// import org.checkerframework.checker.nullness.qual.NonNull;
//
// import com.bearlycattable.bait.bl.searchHelper.SearchHelperUtils;
// import com.bearlycattable.bait.commons.wrappers.QuickSearchTaskWrapper;
// import com.bearlycattable.bait.commons.contexts.QuickSearchContext;
// import com.bearlycattable.bait.commons.contexts.QuickSearchResponseModel;
// import com.bearlycattable.bait.commons.enums.QuickSearchResponseEnum;
// import com.bearlycattable.bait.commons.enums.SearchModeEnum;
// import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;
// import com.bearlycattable.bait.commons.validators.SearchHelperIterationsValidator;
//
// import javafx.concurrent.Task;
//
// public class QuickSearchTaskFactory {
//
//     // public static QuickSearchTaskWrapper createNewQuickSearchTask(@NonNull QuickSearchContext quickSearchContext) {
//     //     quickSearchContext.setSeed(SearchHelperUtils.determineInitialSeed(quickSearchContext));
//     //     quickSearchContext.setIterations(SearchHelperIterationsValidator.validateAndGet(quickSearchContext.getSearchMode(), quickSearchContext.getIterations()));
//     //
//     //     Optional<String> validationError = quickSearchContext.validate();
//     //     if (validationError.isPresent()) {
//     //         return QuickSearchTaskWrapper.builder()
//     //                 .task(null)
//     //                 .error("Some QuickSearch parameters appear to be invalid. Reason: " + validationError.get())
//     //                 .build();
//     //     }
//     //
//     //     return quickSearchTaskCreationHelper(quickSearchContext);
//     // }
//     //
//     // private static QuickSearchTaskWrapper quickSearchTaskCreationHelper(QuickSearchContext quickSearchContext) {
//     //     Task<PubComparisonResultWrapper> task;
//     //     SearchModeEnum mode = quickSearchContext.getSearchMode();
//     //
//     //     if (SearchModeEnum.MIXED == mode || SearchModeEnum.FUZZING == mode) {
//     //         return QuickSearchTaskWrapper.builder()
//     //                 .task(null)
//     //                 .error("Mode not supported for QuickSearch [mode=" + mode + "]")
//     //                 .build();
//     //     } else if (SearchModeEnum.isVerticalRotationMode(mode)) {
//     //         task = createNewVerticalRotationQuickSearchTask(quickSearchContext);
//     //     } else if (SearchModeEnum.isHorizontalRotationMode(mode)) {
//     //         task = createNewHorizontalRotationQuickSearchTask(quickSearchContext);
//     //     } else {
//     //         //this is for incDec modes and random modes
//     //         task = createNewGeneralQuickSearchTask(quickSearchContext);
//     //     }
//     //
//     //     return QuickSearchTaskWrapper.builder()
//     //             .task(task)
//     //             .error(null)
//     //             .build();
//     // }
//
//     // private static Task<PubComparisonResultWrapper> createNewGeneralQuickSearchTask(QuickSearchContext quickSearchContext) {
//     //     return new Task<PubComparisonResultWrapper>() {
//     //         @Override
//     //         public PubComparisonResultWrapper call() {
//     //             int iterations = quickSearchContext.getIterations();
//     //             int accuracy = quickSearchContext.getAccuracy();
//     //             String seed = quickSearchContext.getSeed();
//     //             List<Integer> disabledWords = quickSearchContext.getDisabledWords();
//     //             // Function<String, String> nextPrivFunction = quickSearchContext.getNextPrivFunction();
//     //             BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> evaluationFunction = quickSearchContext.getEvaluationFunction();
//     //             int printSpacing = quickSearchContext.getPrintSpacing();
//     //             final boolean verbose = quickSearchContext.isVerbose();
//     //
//     //             SearchModeEnum searchMode = quickSearchContext.getSearchMode();
//     //
//     //             QuickSearchResponseModel model = new QuickSearchResponseModel();
//     //             model.setHighestResult(PubComparisonResultWrapper.empty());
//     //
//     //             for (int i = 0; i < iterations; i++) {
//     //                 seed = buildNextPriv(seed, disabledWords);
//     //                 // seed = nextPrivFunction.apply(seed); //implementation is different for every search type
//     //                 if (verbose && ((i + 1) % printSpacing == 0)) {
//     //                     System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
//     //                 }
//     //                 QuickSearchResponseModel response = evaluationFunction.apply(model, seed);
//     //
//     //                 if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
//     //                     if (verbose) {
//     //                         System.out.println("Result found in " + (i + 1) + " iterations (for requested accuracy of " + accuracy + "%)");
//     //                     }
//     //                     updateProgress((i + 1), iterations);
//     //                     break;
//     //                 }
//     //                 updateProgress((i + 1), iterations);
//     //             }
//     //             return model.getHighestResult();
//     //         }
//     //     };
//     // }
//
//     // private static Task<PubComparisonResultWrapper> createNewHorizontalRotationQuickSearchTask(QuickSearchContext quickSearchContext) {
//     //     return new Task<PubComparisonResultWrapper>() {
//     //         @Override
//     //         public PubComparisonResultWrapper call() {
//     //             int iterations = quickSearchContext.getIterations();
//     //             int accuracy = quickSearchContext.getAccuracy();
//     //             String seed = quickSearchContext.getSeed();
//     //             String targetPriv = quickSearchContext.getTargetPriv();
//     //             Function<String, String> nextPrivFunction = quickSearchContext.getNextPrivFunction();
//     //             BiFunction<String, Integer, String> nextPrivFunctionForFullPrefixed = quickSearchContext.getNextPrivFunctionFullPrefixed();
//     //             BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> evaluationFunction = quickSearchContext.getEvaluationFunction();
//     //             int printSpacing = quickSearchContext.getPrintSpacing();
//     //             final boolean verbose = quickSearchContext.isVerbose();
//     //
//     //             SearchModeEnum searchMode = quickSearchContext.getSearchMode();
//     //             final boolean fullPrefixedMode = SearchModeEnum.ROTATION_PRIV_FULL_PREFIXED == searchMode;
//     //             String savedSeed = seed;
//     //
//     //             QuickSearchResponseModel model = new QuickSearchResponseModel();
//     //             model.setHighestResult(PubComparisonResultWrapper.empty());
//     //
//     //             for (int i = 0; i < iterations; i++) {
//     //                 if (!fullPrefixedMode) {
//     //                     seed = nextPrivFunction.apply(seed);
//     //                 } else {
//     //                     seed = nextPrivFunctionForFullPrefixed.apply(savedSeed, (i + 1));
//     //                 }
//     //
//     //                 if (verbose && ((i + 1) % printSpacing == 0)) {
//     //                     System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
//     //                 }
//     //                 if (targetPriv != null && targetPriv.equals(seed)) {
//     //                     updateProgress((i + 1), iterations);
//     //                     continue;
//     //                 }
//     //
//     //                 QuickSearchResponseModel response = evaluationFunction.apply(model, seed);
//     //
//     //                 if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
//     //                     if (verbose) {
//     //                         System.out.println("Result found in " + (i + 1) + " iterations (for requested accuracy of " + accuracy + "%)");
//     //                     }
//     //                     updateProgress((i + 1), iterations);
//     //                     break;
//     //                 }
//     //                 updateProgress((i + 1), iterations);
//     //             }
//     //             return model.getHighestResult();
//     //         }
//     //     };
//     // }
//     //
//     // private static Task<PubComparisonResultWrapper> createNewVerticalRotationQuickSearchTask(QuickSearchContext quickSearchContext) {
//     //     return new Task<PubComparisonResultWrapper>() {
//     //         @Override
//     //         public PubComparisonResultWrapper call() {
//     //             int iterations = quickSearchContext.getIterations();
//     //             int accuracy = quickSearchContext.getAccuracy();
//     //             String seed = quickSearchContext.getSeed();
//     //             String targetPriv = quickSearchContext.getTargetPriv();
//     //             BiFunction<String, Integer, String> nextPrivFunctionVertical = quickSearchContext.getNextPrivFunctionVertical();
//     //             BiFunction<QuickSearchResponseModel, String, QuickSearchResponseModel> evaluationFunction = quickSearchContext.getEvaluationFunction();
//     //             BiPredicate<String, Integer> validityCheckFunction = quickSearchContext.getValidityCheckFunction();
//     //             SearchModeEnum searchMode = quickSearchContext.getSearchMode();
//     //             int printSpacing = quickSearchContext.getPrintSpacing();
//     //             final boolean verbose = quickSearchContext.isVerbose();
//     //
//     //             QuickSearchResponseModel model = new QuickSearchResponseModel();
//     //             model.setHighestResult(PubComparisonResultWrapper.empty());
//     //
//     //             final int maxRotations = 0xF;
//     //             String start = seed;
//     //             int highestPoints = model.getHighestResult().getHighestPoints();
//     //
//     //             INDEX_LOOP: for (int i = 0; i < iterations; i++) {
//     //                 seed = start;
//     //                 //check if index is valid for vertical rotation (not locked), otherwise we will get null
//     //                 if (!validityCheckFunction.test(seed, i)) {
//     //                     updateProgress((i + 1), iterations);
//     //                     continue;
//     //                 }
//     //
//     //                 for (int j = 0; j < maxRotations; j++) {
//     //                     seed = nextPrivFunctionVertical.apply(seed, i);
//     //                     if (verbose && ((i + 1) % printSpacing == 0)) {
//     //                         System.out.println("Current priv (current mode=" + searchMode + ") is " + "[" + (i + 1) + "]: " + seed);
//     //                     }
//     //                     if (targetPriv != null && targetPriv.equals(seed)) {
//     //                         updateProgress((i + 1), iterations);
//     //                         continue;
//     //                     }
//     //
//     //                     QuickSearchResponseModel response = evaluationFunction.apply(model, seed);
//     //
//     //                     if (QuickSearchResponseEnum.BREAK == response.getResponseCommand()) {
//     //                         if (verbose) {
//     //                             System.out.println("Result found in " + (i + 1) + " iterations (for requested accuracy of " + accuracy + "%)");
//     //                         }
//     //                         updateProgress((i + 1), iterations);
//     //                         break INDEX_LOOP;
//     //                     }
//     //                 }
//     //                 if (highestPoints < model.getHighestResult().getHighestPoints()) {
//     //                     start = model.getHighestResult().getCommonPriv();
//     //                     highestPoints = model.getHighestResult().getHighestPoints();
//     //                     if (i != 0) {
//     //                         i = 0; //and restart from beginning with a new seed
//     //                     }
//     //                 }
//     //                 updateProgress((i + 1), iterations);
//     //             }
//     //             return model.getHighestResult();
//     //         }
//     //     };
//     // }
// }
