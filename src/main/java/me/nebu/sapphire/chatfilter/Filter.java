package me.nebu.sapphire.chatfilter;

import java.util.List;
import java.util.regex.Pattern;

public class Filter {

    private final String name;
    private final String punishment;
    private final List<Pattern> compiledPatterns;

    public Filter(String name, String punishment, List<String> patterns) {
        this.name = name;
        this.punishment = punishment;
        this.compiledPatterns = patterns.stream()
                .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                .toList();
    }

    public String getName() {
        return name;
    }

    public String getPunishment() {
        return punishment;
    }

    public boolean filter(String text) {
        for (Pattern pattern : compiledPatterns) {
            if (pattern.matcher(text).find()) return true;
        }
        return false;
    }

}
