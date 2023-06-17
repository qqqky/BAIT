package com.bearlycattable.bait.commons.contexts;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Holds various messages and diagnostics about the searcher thread
 */
@Builder
@Getter
public class TaskDiagnosticsModel {

   private final String childThreadId;
   @Setter
   private String loopCompletionMessage;
   @Setter
   private int highestAcquiredPoints;
   @Setter
   private int totalNumOfResultsFound;

   //TODO: don't forget this one
   private Map<String, SingleLoopDataModel> dataMap; //key = childThreadId

   public static TaskDiagnosticsModel empty() {
      return builder().build();
   }

   // public void addEmptySingleLoopModel(@NonNull String childThreadId) {
   //    if (dataMap == null) {
   //       dataMap = new HashMap<>();
   //    }
   //
   //    dataMap.putIfAbsent(childThreadId, new SingleLoopDataModel());
   // }

   // public void addToTotalNumOfResults(int resultsFoundInCurrentLoop) {
   //    totalNumOfResultsFound += resultsFoundInCurrentLoop;
   // }

   // public void setLoopCompletionMessage(@NonNull String childThreadId, String message) {
   //    dataMap.get(childThreadId).setCompletionMessage(message);
   // }
}
