package com.bearlycattable.bait.bl.controllers.advancedTab.proxyInterfaces;

public interface AdvancedProgressAccessProxy {

    boolean isVerboseMode();

    boolean isDarkModeEnabled();

    boolean isBackgroundThreadWorking(String currentThreadNum);
}
