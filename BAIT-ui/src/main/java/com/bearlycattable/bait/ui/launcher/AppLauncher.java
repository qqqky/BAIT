package com.bearlycattable.bait.ui.launcher;

public class AppLauncher {

    public static void main(String[] args) {
        System.setProperty("javafx.preloader", AppPreloader.class.getName());
        App.main(args);
    }
}
