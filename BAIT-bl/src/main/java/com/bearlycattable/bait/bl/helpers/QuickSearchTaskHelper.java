package com.bearlycattable.bait.bl.helpers;


import com.bearlycattable.bait.bl.searchHelper.context.QuickSearchThreadContext;
import com.bearlycattable.bait.commons.wrappers.PubComparisonResultWrapper;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;

public class QuickSearchTaskHelper {

    // private QuickSearchTabController quickSearchTabController;

    public void prepareTask(QuickSearchThreadContext context) {
        Task<PubComparisonResultWrapper> searchTask = context.getSearchTask();

        if (!context.getController().getTaskMap().isEmpty()) {
            throw new IllegalStateException("Task map is not empty. Will not prepare a new task.");
        }
        context.getController().getTaskMap().put(context.getThreadId(), context.getSearchTask());

        //bind to progress bar
        context.getController().getQuickSearchProgressBar().progressProperty().bind(searchTask.progressProperty());

        searchTask.setOnRunning(event -> doOnRunning(context));
        searchTask.setOnSucceeded(event -> doOnSucceeded(context, event));
        searchTask.setOnCancelled(event -> doOnCancelled(context));
        searchTask.setOnFailed(event -> doOnFailed(context));

        //set prepared flag
        context.setPrepared(true);
    }

    private void doOnRunning(QuickSearchThreadContext context) {
        if (context.getController().getMainController().isVerboseMode()) {
            System.out.println("QuickSearch task is now RUNNING");
        }
    }

    private void doOnSucceeded(QuickSearchThreadContext context, WorkerStateEvent event) {
        boolean verboseMode = context.getController().getMainController().isVerboseMode();
        if (verboseMode) {
            System.out.println("QuickSearch task has FINISHED.");
        }
        PubComparisonResultWrapper result = (PubComparisonResultWrapper) event.getSource().getValue();

        String threadNum = context.getThreadId();

        if (!context.getController().getTaskMap().containsKey(threadNum)) {
            throw new IllegalStateException("Task map does not contain the specified thread id: " + threadNum);
        }
        if (verboseMode) {
            System.out.println("Result has been obtained from thread: " + threadNum);
        }
        context.getController().getTaskMap().remove(threadNum);
        // context.getController().setCurrentSearchResultPK(result.getCommonPriv());
        context.getController().showQuickSearchResults(result, context.getAccuracy());
        context.getController().getQuickSearchBtnSearch().setDisable(false);
    }

    private void doOnCancelled(QuickSearchThreadContext context) {
        context.getController().getQuickSearchBtnSearch().setDisable(false);
        if (context.getController().getMainController().isVerboseMode()) {
            System.out.println("QuickSearch task is now CANCELLED");
        }
        context.getController().getTaskMap().remove(context.getThreadId());
    }

    private void doOnFailed(QuickSearchThreadContext context) {
        context.getController().getQuickSearchBtnSearch().setDisable(false);
        if (context.getController().getMainController().isVerboseMode()) {
            System.out.println("QuickSearch task has FAILED");
        }
        context.getController().getTaskMap().remove(context.getThreadId());
    }

}
