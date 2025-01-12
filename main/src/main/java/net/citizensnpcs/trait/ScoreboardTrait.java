package net.citizensnpcs.trait;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("scoreboardtrait")
public class ScoreboardTrait extends Trait {
    @Persist
    private ChatColor color;
    private ChatColor previousGlowingColor;
    @Persist
    private final Set<String> tags = new HashSet<String>();

    public ScoreboardTrait() {
        super("scoreboardtrait");
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void apply(Team team, boolean nameVisibility) {
        Set<String> newTags = new HashSet<String>(tags);
        for (String oldTag : npc.getEntity().getScoreboardTags()) {
            if (!newTags.remove(oldTag)) {
                npc.getEntity().removeScoreboardTag(oldTag);
            }
        }
        for (String tag : newTags) {
            npc.getEntity().addScoreboardTag(tag);
        }

        if (SUPPORT_TEAM_SETOPTION) {
            try {
                team.setOption(Option.NAME_TAG_VISIBILITY, nameVisibility ? OptionStatus.ALWAYS : OptionStatus.NEVER);
            } catch (NoSuchMethodError e) {
                SUPPORT_TEAM_SETOPTION = false;
            } catch (NoClassDefFoundError e) {
                SUPPORT_TEAM_SETOPTION = false;
            }
        }

        if (npc.data().has(NPC.GLOWING_COLOR_METADATA)) {
            color = ChatColor.valueOf(npc.data().get(NPC.GLOWING_COLOR_METADATA));
            npc.data().remove(NPC.GLOWING_COLOR_METADATA);
        }
        if (color != null) {
            if (SUPPORT_GLOWING_COLOR) {
                try {
                    if (team.getColor() == null || previousGlowingColor == null
                            || (previousGlowingColor != null && color != previousGlowingColor)) {
                        team.setColor(color);
                        previousGlowingColor = color;
                    }
                } catch (NoSuchMethodError err) {
                    SUPPORT_GLOWING_COLOR = false;
                }
            } else {
                if (team.getPrefix() == null || team.getPrefix().length() == 0 || previousGlowingColor == null
                        || (previousGlowingColor != null
                                && !team.getPrefix().equals(previousGlowingColor.toString()))) {
                    team.setPrefix(color.toString());
                    previousGlowingColor = color;
                }
            }
        }
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void setColor(ChatColor color) {
        this.color = color;
    }

    private static boolean SUPPORT_GLOWING_COLOR = true;
    private static boolean SUPPORT_TEAM_SETOPTION = true;
}