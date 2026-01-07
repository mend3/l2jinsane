package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.ArrayList;
import java.util.List;

public class NewCharacterCreated extends AbstractMods {
    private static final List<Integer> _players = new ArrayList<>();

    public NewCharacterCreated() {
        registerMod(true);
    }

    public static NewCharacterCreated getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public void onCreateCharacter(Player player) {
        player.setTitle(ConfigData.NEW_CHARACTER_CREATED_TITLE);
        _players.add(player.getObjectId());
    }

    public void onEnterWorld(Player player) {
        if (_players.contains(player.getObjectId())) {
            if (ConfigData.NEW_CHARACTER_CREATED_GIVE_BUFF)
                for (IntIntHolder bsh : ConfigData.NEW_CHARACTER_CREATED_BUFFS) {
                    L2Skill skill = bsh.getSkill();
                    if (skill != null)
                        skill.getEffects(player, player);
                }
            if (!ConfigData.NEW_CHARACTER_CREATED_SEND_SCREEN_MSG.equals(""))
                player.sendPacket(new ExShowScreenMessage(ConfigData.NEW_CHARACTER_CREATED_SEND_SCREEN_MSG, 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
            _players.remove(Integer.valueOf(player.getObjectId()));
        }
    }

    private static class SingletonHolder {
        protected static final NewCharacterCreated INSTANCE = new NewCharacterCreated();
    }
}
