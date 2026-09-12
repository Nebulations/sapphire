package me.nebu.sapphire.util;

import java.util.*;

public class Cache {

    public static boolean CHAT_MUTED = false;

    public static Set<UUID> VANISHED_PLAYERS = new HashSet<>();
    public static HashMap<UUID, Runnable> CONFIRMATION_SCREENS = new HashMap<>();

    public static List<String> REMINDER_MESSAGE = new ArrayList<>();

}
