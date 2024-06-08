package de.verdox.mccreativelab.util.io;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StringAlign {
    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

    public static List<String> formatStringToLines(String input, int maxChars, Alignment alignment) {
        var output = new LinkedList<String>();
        var words = input.split(" ");
        var charCounter = 0;
        var builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            var word = words[i];
            var spaceNeeded = charCounter == 0 ? 0 : 1; // Kein Leerzeichen für das erste Wort der Zeile

            // Entscheiden, ob ein neues Wort + evtl. Leerzeichen passt oder nicht
            if (charCounter + spaceNeeded + word.length() > maxChars) {
                output.add(alignment.align(builder.toString(), maxChars));
                builder = new StringBuilder();
                charCounter = 0;
                spaceNeeded = 0; // Zurücksetzen, da es das erste Wort der neuen Zeile ist
            }

            // Leerzeichen hinzufügen, wenn es nicht das erste Wort in der Zeile ist
            if (charCounter > 0) {
                builder.append(" ");
                charCounter++; // Leerzeichen zählen
            }

            // Wort hinzufügen
            builder.append(word);
            charCounter += word.length();
        }

        // Den Inhalt des Builders zur Liste hinzufügen, wenn noch etwas vorhanden ist
        var content = builder.toString();
        if (!content.isEmpty())
            output.add(alignment.align(content, maxChars));

        return output;
    }

    public enum Alignment {
        LEFT {
            @Override
            public String align(String text, int maxCharsPerLine) {
                return StringUtils.rightPad(text, maxCharsPerLine);
            }
        },
        CENTER {
            @Override
            public String align(String text, int maxCharsPerLine) {
                if (maxCharsPerLine > text.length())
                    return StringUtils.center(text, maxCharsPerLine);
                else
                    return text; // Keine zusätzlichen Leerzeichen hinzufügen
            }
        }, RIGHT {
            @Override
            public String align(String text, int maxCharsPerLine) {
                if (maxCharsPerLine > text.length())
                    return StringUtils.leftPad(text, maxCharsPerLine);
                else
                    return text; // Keine zusätzlichen Leerzeichen hinzufügen
            }
        },
        ;

        Alignment() {
        }

        public abstract String align(String text, int maxCharsPerLine);
    }
}
