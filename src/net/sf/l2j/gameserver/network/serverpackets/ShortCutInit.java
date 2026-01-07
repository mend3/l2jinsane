package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Shortcut;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class ShortCutInit extends L2GameServerPacket {
    private final Player _player;

    private final Shortcut[] _shortcuts;

    public ShortCutInit(Player player) {
        this._player = player;
        this._shortcuts = player.getShortcutList().getShortcuts();
    }

    protected final void writeImpl() {
        writeC(69);
        writeD(this._shortcuts.length);
        for (Shortcut sc : this._shortcuts) {
            writeD(sc.getType().ordinal());
            writeD(sc.getSlot() + sc.getPage() * 12);
            switch (sc.getType()) {
                case ITEM:
                    writeD(sc.getId());
                    writeD(sc.getCharacterType());
                    writeD(sc.getSharedReuseGroup());
                    if (sc.getSharedReuseGroup() < 0) {
                        writeD(0);
                        writeD(0);
                    } else {
                        ItemInstance item = this._player.getInventory().getItemByObjectId(sc.getId());
                        if (item == null || !item.isEtcItem()) {
                            writeD(0);
                            writeD(0);
                        } else {
                            IntIntHolder[] skills = item.getEtcItem().getSkills();
                            if (skills == null) {
                                writeD(0);
                                writeD(0);
                            } else {
                                for (IntIntHolder skillInfo : skills) {
                                    L2Skill itemSkill = skillInfo.getSkill();
                                    if (this._player.getReuseTimeStamp().containsKey(Integer.valueOf(itemSkill.getReuseHashCode()))) {
                                        writeD((int) (this._player.getReuseTimeStamp().get(Integer.valueOf(itemSkill.getReuseHashCode())).getRemaining() / 1000L));
                                        writeD((int) (itemSkill.getReuseDelay() / 1000L));
                                    } else {
                                        writeD(0);
                                        writeD(0);
                                    }
                                }
                            }
                        }
                    }
                    writeD(0);
                    break;
                case SKILL:
                    writeD(sc.getId());
                    writeD(sc.getLevel());
                    writeC(0);
                    writeD(1);
                    break;
                default:
                    writeD(sc.getId());
                    writeD(1);
                    break;
            }
        }
    }
}
