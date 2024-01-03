package com.bearlycattable.bait.bl.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.bearlycattable.bait.advancedCommons.helpers.BaitComponentHelper;
import com.bearlycattable.bait.commons.helpers.BaitHelper;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BaitNumberedListHelper {

    private static final String specialLabelTooltipNormal = "specialLabelTooltipNormal";
    private static final String specialLabelTooltipDark = "specialLabelTooltipDark";

    private static final Map<Integer, String> NUMBERED_LIST_TEXT = new LinkedHashMap<>();
    private static final Map<String, String> SPECIAL_TOOLTIPS = new LinkedHashMap<>();

    public static synchronized VBox readFileAndInsertListToParentComponent(String resource, VBox parentComponent) {
        readLocalInfoFile(resource);
        VBox parent = insertNumberedListToParentComponent(parentComponent);
        clearDataMaps();

        return parent;
    }

    private static void clearDataMaps() {
        NUMBERED_LIST_TEXT.clear();
        SPECIAL_TOOLTIPS.clear();
    }

    private static void readLocalInfoFile(String resource) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (is == null) {
            is = BaitNumberedListHelper.class.getClassLoader().getResourceAsStream(resource);
        }
        if (is == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }

        Pattern regex = Pattern.compile("//\\[[\\d]{1,2}]\\\\\\\\[.]"); //matches //[nn]\\. where nn is 'one or two' digits
        int tooltipCount = 0;
        boolean tooltipOpened = false;
        BaitHelper helper = new BaitHelper();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;

            int itemNum = -1;
            StringBuilder intermediary = new StringBuilder();
            StringBuilder tooltipBuilder = new StringBuilder();
            String tooltipPlaceholder;

            READ_NEXT: while ((line = br.readLine()) != null) {
                // System.out.println(line);
                if (line.startsWith("#")) {
                    continue;
                }

                if (!line.endsWith(System.lineSeparator())) {
                    line = line + System.lineSeparator();
                }

                if (tooltipOpened) {
                    if (line.contains("]]]")) { //tooltip end found
                        tooltipBuilder.append(line, 0, line.indexOf("]]]"));
                        tooltipOpened = false;
                        tooltipPlaceholder = "[#T" + helper.padToX(Integer.toHexString(++tooltipCount), 2, true) + "]";
                        line = line.substring(line.indexOf("]]]") + "]]]".length());
                        intermediary.append(tooltipPlaceholder);
                        SPECIAL_TOOLTIPS.put(tooltipPlaceholder, tooltipBuilder.toString().trim());
                        tooltipBuilder.delete(0, tooltipBuilder.length());
                        if (line.isEmpty()) {
                            continue;
                        }
                    } else { //tooltip end not found
                        tooltipBuilder.append(line);
                        continue;
                    }
                }
                if (line.startsWith("//")) {
                    if (intermediary.length() != 0) {
                        NUMBERED_LIST_TEXT.put(itemNum, intermediary.toString().trim());
                        intermediary.delete(0, intermediary.length());
                        itemNum = -1;
                    }
                    String test = line.substring(0, 8);

                    if (regex.matcher(test).matches()) {
                        String a = test.substring(2, test.indexOf("]\\\\."));
                        a = a.replaceAll("[\\[\\].]", "");
                        itemNum = Integer.parseInt(a);
                        line = line.substring(line.indexOf("]\\\\.") + "]\\\\.".length()); //no trim()?
                    }
                }

                //parse tooltips
                while (line.contains("[[[tooltip:")) {
                    //extract tooltip
                    int tooltipStartIndex = line.indexOf("[[[tooltip:");
                    //if tooltip ends on this line
                    if (line.contains("]]]")) {
                        int tooltipEndIndex = line.indexOf("]]]");
                        tooltipBuilder.append(line, tooltipStartIndex + "[[[tooltip:".length(), tooltipEndIndex);
                        tooltipPlaceholder = "[#T" + helper.padToX(Integer.toHexString(++tooltipCount), 2, true) + "]";
                        String start = line.substring(0, tooltipStartIndex);
                        String end = line.substring(tooltipEndIndex + "]]]".length());
                        line = start + tooltipPlaceholder + end;
                        SPECIAL_TOOLTIPS.put(tooltipPlaceholder, tooltipBuilder.toString().trim());
                        tooltipBuilder.delete(0, tooltipBuilder.length());
                        tooltipOpened = false;
                    } else {
                        //tooltip is not closed yet
                        intermediary.append(line,0, line.indexOf("[[[tooltip:")); //append start
                        tooltipBuilder.append(line.substring(tooltipStartIndex + "[[[tooltip:".length()));
                        tooltipOpened = true;
                        //since we consumed the whole line - we can move on to the next one
                        continue READ_NEXT;
                    }
                }

                if (itemNum != -1 && !line.isEmpty()) {
                    intermediary.append(line);
                }
            }
            if (itemNum != -1) {
                NUMBERED_LIST_TEXT.put(itemNum, intermediary.toString().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading file at " + resource);
        }
    }

    private static VBox insertNumberedListToParentComponent(VBox parent) {
        NUMBERED_LIST_TEXT.keySet().stream().forEach(listNumber -> {
            String labelText = listNumber + ". " + NUMBERED_LIST_TEXT.get(listNumber);

            HBox placeholder = new HBox();
            placeholder.getChildren().add(BaitComponentHelper.createEmptyHBoxSpacer(5, true));

            if (SPECIAL_TOOLTIPS.isEmpty() || SPECIAL_TOOLTIPS.keySet().stream().noneMatch(labelText::contains)) {
                placeholder.getChildren().add(BaitComponentHelper.createLabel(labelText, 18, true));
            } else {
                List<String> labels = new ArrayList<>();
                labels.add(labelText);

                for (String tooltipKey : SPECIAL_TOOLTIPS.keySet()) {
                    labels.stream().filter(lbl -> !lbl.equals(tooltipKey) && lbl.contains(tooltipKey)).findFirst().ifPresent(l -> {
                        //must escape the [ and ] characters, otherwise split won't work properly
                        Iterator<String> iter = Arrays.asList(l.split("\\[" + tooltipKey.substring(tooltipKey.indexOf("[") + 1, tooltipKey.indexOf("]")) + "]")).iterator();
                        List<String> afterSplit = new ArrayList<>();
                        if (l.startsWith(tooltipKey)) {
                            //because split will only cut out the key if it's at the end or at the start of the string
                            afterSplit.add(tooltipKey);
                        }
                        while (iter.hasNext()) {
                            afterSplit.add(iter.next());
                            if (iter.hasNext()) {
                                afterSplit.add(tooltipKey);
                            }
                        }
                        if (l.endsWith(tooltipKey)) {
                            //because split will only cut out the key if it's at the end or at the start of the string
                            afterSplit.add(tooltipKey);
                        }
                        labels.remove(l);
                        labels.addAll(afterSplit);
                    });
                }

                labels.forEach(label -> {
                    if (label.isEmpty()) {
                        return;
                    }
                    if (SPECIAL_TOOLTIPS.keySet().stream().noneMatch(label::equals)) {
                        placeholder.getChildren().add(BaitComponentHelper.createLabel(label, 18, true));
                        return;
                    }
                    SPECIAL_TOOLTIPS.keySet().stream().filter(label::equals).findFirst().ifPresent(kk -> {
                        String tooltipText = SPECIAL_TOOLTIPS.get(kk);
                        placeholder.getChildren().add(BaitComponentHelper.createPrettyLabelWithTooltip(tooltipText));
                    });

                });
            }
            parent.getChildren().add(placeholder);
        });

        return parent;
    }
}
