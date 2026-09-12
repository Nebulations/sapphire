package me.nebu.sapphire.actions;

import me.nebu.sapphire.punishments.PunishmentType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionDetails {

    private final PunishmentType type;

    private final boolean kickPlayer;
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final List<String> messages;

    public ActionDetails(PunishmentType type, boolean kickPlayer, Sound sound, float volume, float pitch, List<String> messages) {
        this.type = type;
        this.kickPlayer = kickPlayer;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.messages = messages;
    }

    public PunishmentType getType() { return type; }
    public boolean shouldKickPlayer() { return kickPlayer; }

    public Sound getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }

    public List<String> getMessages() { return messages; }

    @Override
    public String toString() {
        return "ActionDetails{" +
                "type=" + type +
                ", kickPlayer=" + kickPlayer +
                ", sound=" + sound +
                ", volume=" + volume +
                ", pitch=" + pitch +
                ", messages=" + messages +
                '}';
    }
}
