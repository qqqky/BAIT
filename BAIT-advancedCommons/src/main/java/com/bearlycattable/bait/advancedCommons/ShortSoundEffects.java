package com.bearlycattable.bait.advancedCommons;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum ShortSoundEffects {
    SINGLE_BEEP ("com.bearlycattable.bait.ui.sounds/singleBeep.wav"),
    DOUBLE_BEEP ("com.bearlycattable.bait.ui.sounds/doubleBeep.wav");

    private static final int LIMIT = 5;
    private enum Position {START, MIDDLE, END}

    private final Clip[] clips;

    ShortSoundEffects(String name) {
        List<Clip> clips = new ArrayList<>();
        for (int i = 0; i < LIMIT; i++) {
            Clip clip = null;
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(name))))) {
                clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioStream.getFormat()));
                clip.open(audioStream);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
            clips.add(clip);
        }

        this.clips = clips.toArray(new Clip[LIMIT]);
    }

    public void play() {
        for (int i = 0; i < LIMIT; i++) {
            if (!clips[i].isRunning()) {
                processAtIndex(i);
                break;
            }
        }
    }

    private void processAtIndex(int i) {
        switch (getPosition(i)) {
            case START:
                //reset the last clip
                if (!clips[LIMIT - 1].isRunning()) {
                    clips[LIMIT - 1].setFramePosition(0);
                }
                break;
            case MIDDLE:
                break;
            case END:
                //reset all clips except the last one
                for (int j = 0; j < LIMIT - 2; j++) {
                    if (!clips[j].isRunning()) {
                        clips[j].setFramePosition(0);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Position not supported at ShortSoundEffects#processPosition [position=" + getPosition(i) + "]");
        }

        resetAndStartCurrent(i);
    }

    private void resetAndStartCurrent(int i) {
        if (clips[i].isRunning()) {
            return;
        }

        clips[i].setFramePosition(0);
        clips[i].start();
    }

    private Position getPosition(int index) {
        if (index <= 0) {
            return Position.START;
        }

        return index >= LIMIT ? Position.END : Position.MIDDLE;
    }
}
