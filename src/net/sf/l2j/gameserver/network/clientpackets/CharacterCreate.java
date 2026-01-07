package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.PlayerClassData;
import net.sf.l2j.gameserver.data.xml.ScriptData;
import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.Shortcut;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.holder.ItemTemplateHolder;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.scripting.Quest;

public final class CharacterCreate extends L2GameClientPacket {
    private String _name;

    private int _race;

    private byte _sex;

    private int _classId;

    private int _int;

    private int _str;

    private int _con;

    private int _men;

    private int _dex;

    private int _wit;

    private byte _hairStyle;

    private byte _hairColor;

    private byte _face;

    protected void readImpl() {
        this._name = readS();
        this._race = readD();
        this._sex = (byte) readD();
        this._classId = readD();
        this._int = readD();
        this._str = readD();
        this._con = readD();
        this._men = readD();
        this._dex = readD();
        this._wit = readD();
        this._hairStyle = (byte) readD();
        this._hairColor = (byte) readD();
        this._face = (byte) readD();
    }

    protected void runImpl() {
        if (this._race > 4 || this._race < 0) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED);
            return;
        }
        if (this._face > 2 || this._face < 0) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED);
            return;
        }
        if (this._hairStyle < 0 || (this._sex == 0 && this._hairStyle > 4) || (this._sex != 0 && this._hairStyle > 6)) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED);
            return;
        }
        if (this._hairColor > 3 || this._hairColor < 0) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED);
            return;
        }
        if (!StringUtil.isValidString(this._name, "^[A-Za-z0-9]{3,16}$")) {
            sendPacket((this._name.length() > 16) ? (L2GameServerPacket) CharCreateFail.REASON_16_ENG_CHARS : (L2GameServerPacket) CharCreateFail.REASON_INCORRECT_NAME);
            return;
        }
        if (NpcData.getInstance().getTemplateByName(this._name) != null) {
            sendPacket(CharCreateFail.REASON_INCORRECT_NAME);
            return;
        }
        if (PlayerInfoTable.getInstance().getCharactersInAcc(getClient().getAccountName()) >= 7) {
            sendPacket(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
            return;
        }
        if (PlayerInfoTable.getInstance().getPlayerObjectId(this._name) > 0) {
            sendPacket(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
            return;
        }
        PlayerTemplate template = PlayerClassData.getInstance().getTemplate(this._classId);
        if (template == null || template.getClassBaseLevel() > 1) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED);
            return;
        }
        Player player = Player.create(IdFactory.getInstance().getNextId(), template, getClient().getAccountName(), this._name, this._hairStyle, this._hairColor, this._face, Sex.values()[this._sex]);
        if (player == null) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED);
            return;
        }
        player.setCurrentCp(0.0D);
        player.setCurrentHp(player.getMaxHp());
        player.setCurrentMp(player.getMaxMp());
        sendPacket(CharCreateOk.STATIC_PACKET);
        World.getInstance().addObject(player);
        player.getPosition().set(template.getRandomSpawn());
        player.setTitle("");
        player.getShortcutList().addShortcut(new Shortcut(0, 0, ShortcutType.ACTION, 2, -1, 1));
        player.getShortcutList().addShortcut(new Shortcut(3, 0, ShortcutType.ACTION, 5, -1, 1));
        player.getShortcutList().addShortcut(new Shortcut(10, 0, ShortcutType.ACTION, 0, -1, 1));
        for (ItemTemplateHolder holder : template.getItems()) {
            ItemInstance item = player.getInventory().addItem("Init", holder.getId(), holder.getValue(), player, null);
            if (holder.getId() == 5588)
                player.getShortcutList().addShortcut(new Shortcut(11, 0, ShortcutType.ITEM, item.getObjectId(), -1, 1));
            if (item.isEquipable() && holder.isEquipped())
                player.getInventory().equipItemAndRecord(item);
        }
        for (GeneralSkillNode skill : player.getAvailableAutoGetSkills()) {
            if (skill.getId() == 1001 || skill.getId() == 1177)
                player.getShortcutList().addShortcut(new Shortcut(1, 0, ShortcutType.SKILL, skill.getId(), 1, 1), false);
            if (skill.getId() == 1216)
                player.getShortcutList().addShortcut(new Shortcut(9, 0, ShortcutType.SKILL, skill.getId(), 1, 1), false);
        }
        if (!Config.DISABLE_TUTORIAL)
            if (player.getQuestState("Tutorial") == null) {
                Quest quest = ScriptData.getInstance().getQuest("TutorialQuest");
                if (quest != null)
                    quest.newQuestState(player).setState((byte) 1);
            }
        EngineModsManager.onCreateCharacter(player);
        player.setOnlineStatus(true, false);
        player.deleteMe();
        CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), (getClient().getSessionId()).playOkID1);
        sendPacket(csi);
        getClient().setCharSelectSlot(csi.getCharacterSlots());
    }
}
