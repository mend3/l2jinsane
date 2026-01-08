package net.sf.l2j.gameserver.model.item.kind;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.IconsTable;
import net.sf.l2j.gameserver.enums.items.*;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.skills.basefuncs.FuncTemplate;
import net.sf.l2j.gameserver.skills.conditions.Condition;

import java.util.*;
import java.util.logging.Logger;

public abstract class Item {
    public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
    public static final int TYPE1_SHIELD_ARMOR = 1;
    public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
    public static final int TYPE2_WEAPON = 0;
    public static final int TYPE2_SHIELD_ARMOR = 1;
    public static final int TYPE2_ACCESSORY = 2;
    public static final int TYPE2_QUEST = 3;
    public static final int TYPE2_MONEY = 4;
    public static final int TYPE2_OTHER = 5;
    public static final int SLOT_NONE = 0;
    public static final int SLOT_UNDERWEAR = 1;
    public static final int SLOT_R_EAR = 2;
    public static final int SLOT_L_EAR = 4;
    public static final int SLOT_LR_EAR = 6;
    public static final int SLOT_NECK = 8;
    public static final int SLOT_R_FINGER = 16;
    public static final int SLOT_L_FINGER = 32;
    public static final int SLOT_LR_FINGER = 48;
    public static final int SLOT_HEAD = 64;
    public static final int SLOT_R_HAND = 128;
    public static final int SLOT_L_HAND = 256;
    public static final int SLOT_GLOVES = 512;
    public static final int SLOT_CHEST = 1024;
    public static final int SLOT_LEGS = 2048;
    public static final int SLOT_FEET = 4096;
    public static final int SLOT_BACK = 8192;
    public static final int SLOT_LR_HAND = 16384;
    public static final int SLOT_FULL_ARMOR = 32768;
    public static final int SLOT_FACE = 65536;
    public static final int SLOT_ALLDRESS = 131072;
    public static final int SLOT_HAIR = 262144;
    public static final int SLOT_HAIRALL = 524288;
    public static final int SLOT_WOLF = -100;
    public static final int SLOT_HATCHLING = -101;
    public static final int SLOT_STRIDER = -102;
    public static final int SLOT_BABYPET = -103;
    public static final int SLOT_ALLWEAPON = 16512;
    public static final Map<String, Integer> _slots = new HashMap<>();
    protected static final Logger _log = Logger.getLogger(Item.class.getName());

    static {
        _slots.put("chest", 1024);
        _slots.put("fullarmor", 32768);
        _slots.put("alldress", 131072);
        _slots.put("head", 64);
        _slots.put("hair", 262144);
        _slots.put("face", 65536);
        _slots.put("hairall", 524288);
        _slots.put("underwear", 1);
        _slots.put("back", 8192);
        _slots.put("neck", 8);
        _slots.put("legs", 2048);
        _slots.put("feet", 4096);
        _slots.put("gloves", 512);
        _slots.put("chest,legs", 3072);
        _slots.put("rhand", 128);
        _slots.put("lhand", 256);
        _slots.put("lrhand", 16384);
        _slots.put("rear;lear", 6);
        _slots.put("rfinger;lfinger", 48);
        _slots.put("none", 0);
        _slots.put("wolf", -100);
        _slots.put("hatchling", -101);
        _slots.put("strider", -102);
        _slots.put("babypet", -103);
    }

    private final int _itemId;
    private final String _name;
    private final int _weight;
    private final boolean _stackable;
    private final MaterialType _materialType;
    private final CrystalType _crystalType;
    private final int _duration;
    private final int _bodyPart;
    private final int _referencePrice;
    private final int _crystalCount;
    private final boolean _sellable;
    private final boolean _dropable;
    private final boolean _destroyable;
    private final boolean _tradable;
    private final boolean _depositable;
    private final boolean _heroItem;
    private final boolean _isOlyRestricted;
    private final ActionType _defaultAction;
    protected int _type1;
    protected int _type2;
    protected List<FuncTemplate> _funcTemplates;
    protected List<Condition> _preConditions;
    private IntIntHolder[] _skillHolder;
    private List<Quest> _questEvents = Collections.emptyList();

    protected Item(StatSet set) {
        this._itemId = set.getInteger("item_id");
        this._name = set.getString("name");
        this._weight = set.getInteger("weight", 0);
        this._materialType = set.getEnum("material", MaterialType.class, MaterialType.STEEL);
        this._duration = set.getInteger("duration", -1);
        this._bodyPart = _slots.get(set.getString("bodypart", "none"));
        this._referencePrice = set.getInteger("price", 0);
        this._crystalType = set.getEnum("crystal_type", CrystalType.class, CrystalType.NONE);
        this._crystalCount = set.getInteger("crystal_count", 0);
        this._stackable = set.getBool("is_stackable", false);
        this._sellable = set.getBool("is_sellable", true);
        this._dropable = set.getBool("is_dropable", true);
        this._destroyable = set.getBool("is_destroyable", true);
        this._tradable = set.getBool("is_tradable", true);
        this._depositable = set.getBool("is_depositable", true);
        this._heroItem = this._itemId >= 6611 && this._itemId <= 6621 || this._itemId == 6842;
        this._isOlyRestricted = set.getBool("is_oly_restricted", false);
        this._defaultAction = set.getEnum("default_action", ActionType.class, ActionType.none);
        String skills = set.getString("item_skill", null);
        if (skills != null) {
            String[] skillsSplit = skills.split(";");
            this._skillHolder = new IntIntHolder[skillsSplit.length];
            int used = 0;

            for (String element : skillsSplit) {
                try {
                    String[] skillSplit = element.split("-");
                    int id = Integer.parseInt(skillSplit[0]);
                    int level = Integer.parseInt(skillSplit[1]);
                    if (id == 0) {
                        _log.info("Ignoring item_skill(" + element + ") for item " + this + ". Skill id is 0.");
                    } else if (level == 0) {
                        _log.info("Ignoring item_skill(" + element + ") for item " + this + ". Skill level is 0.");
                    } else {
                        this._skillHolder[used] = new IntIntHolder(id, level);
                        ++used;
                    }
                } catch (Exception var12) {
                    _log.warning("Failed to parse item_skill(" + element + ") for item " + this + ". The used format is wrong.");
                }
            }

            if (used != this._skillHolder.length) {
                IntIntHolder[] skillHolder = new IntIntHolder[used];
                System.arraycopy(this._skillHolder, 0, skillHolder, 0, used);
                this._skillHolder = skillHolder;
            }
        }

    }

    public static String getItemIcon(int itemId) {
        return IconsTable.getIcon(itemId);
    }

    public static String getItemNameById(int itemId) {
        Item item = ItemTable.getInstance().getTemplate(itemId);
        String itemName = "NoName";
        if (itemId != 0) {
            itemName = item.getName();
        }

        return itemName;
    }

    public abstract ItemType getItemType();

    public final int getDuration() {
        return this._duration;
    }

    public final int getItemId() {
        return this._itemId;
    }

    public abstract int getItemMask();

    public final MaterialType getMaterialType() {
        return this._materialType;
    }

    public final int getType2() {
        return this._type2;
    }

    public final int getWeight() {
        return this._weight;
    }

    public final boolean isCrystallizable() {
        return this._crystalType != CrystalType.NONE && this._crystalCount > 0;
    }

    public final CrystalType getCrystalType() {
        return this._crystalType;
    }

    public final int getCrystalItemId() {
        return this._crystalType.getCrystalId();
    }

    public final int getCrystalCount() {
        return this._crystalCount;
    }

    public final int getCrystalCount(int enchantLevel) {
        if (enchantLevel > 3) {
            return switch (this._type2) {
                case 0 ->
                        this._crystalCount + this.getCrystalType().getCrystalEnchantBonusWeapon() * (2 * enchantLevel - 3);
                case 1, 2 ->
                        this._crystalCount + this.getCrystalType().getCrystalEnchantBonusArmor() * (3 * enchantLevel - 6);
                default -> this._crystalCount;
            };
        } else if (enchantLevel > 0) {
            return switch (this._type2) {
                case 0 -> this._crystalCount + this.getCrystalType().getCrystalEnchantBonusWeapon() * enchantLevel;
                case 1, 2 -> this._crystalCount + this.getCrystalType().getCrystalEnchantBonusArmor() * enchantLevel;
                default -> this._crystalCount;
            };
        } else {
            return this._crystalCount;
        }
    }

    public final String getName() {
        return this._name;
    }

    public final int getBodyPart() {
        return this._bodyPart;
    }

    public final int getType1() {
        return this._type1;
    }

    public final boolean isStackable() {
        return this._stackable;
    }

    public boolean isConsumable() {
        return false;
    }

    public boolean isEquipable() {
        return this.getBodyPart() != 0 && !(this.getItemType() instanceof EtcItemType);
    }

    public final int getReferencePrice() {
        return this._referencePrice;
    }

    public final boolean isSellable() {
        return this._sellable;
    }

    public final boolean isDropable() {
        return this._dropable;
    }

    public final boolean isDestroyable() {
        return this._destroyable;
    }

    public final boolean isTradable() {
        return this._tradable;
    }

    public final boolean isDepositable() {
        return this._depositable;
    }

    public final List<Func> getStatFuncs(ItemInstance item, Creature player) {
        if (this._funcTemplates != null && !this._funcTemplates.isEmpty()) {
            List<Func> funcs = new ArrayList<>(this._funcTemplates.size());
            Env env = new Env();
            env.setCharacter(player);
            env.setTarget(player);
            env.setItem(item);

            for (FuncTemplate t : this._funcTemplates) {
                Func f = t.getFunc(env, item);
                if (f != null) {
                    funcs.add(f);
                }
            }

            return funcs;
        } else {
            return Collections.emptyList();
        }
    }

    public void attach(FuncTemplate f) {
        if (this._funcTemplates == null) {
            this._funcTemplates = new ArrayList<>(1);
        }

        this._funcTemplates.add(f);
    }

    public final void attach(Condition c) {
        if (this._preConditions == null) {
            this._preConditions = new ArrayList<>();
        }

        if (!this._preConditions.contains(c)) {
            this._preConditions.add(c);
        }

    }

    public final IntIntHolder[] getSkills() {
        return this._skillHolder;
    }

    public boolean checkCondition(Creature activeChar, WorldObject target, boolean sendMessage) {
        if ((this.isOlyRestrictedItem() || this.isHeroItem()) && activeChar instanceof Player && activeChar.getActingPlayer().isInOlympiadMode()) {
            if (this.isEquipable()) {
                activeChar.getActingPlayer().sendPacket(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT);
            } else {
                activeChar.getActingPlayer().sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            }

            return false;
        } else if (this._preConditions == null) {
            return true;
        } else {
            Env env = new Env();
            env.setCharacter(activeChar);
            if (target instanceof Creature) {
                env.setTarget((Creature) target);
            }

            for (Condition preCondition : this._preConditions) {
                if (preCondition != null && !preCondition.test(env)) {
                    if (activeChar instanceof Summon) {
                        activeChar.getActingPlayer().sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
                        return false;
                    }

                    if (sendMessage) {
                        String msg = preCondition.getMessage();
                        int msgId = preCondition.getMessageId();
                        if (msg != null) {
                            activeChar.sendMessage(msg);
                        } else if (msgId != 0) {
                            SystemMessage sm = SystemMessage.getSystemMessage(msgId);
                            if (preCondition.isAddName()) {
                                sm.addItemName(this._itemId);
                            }

                            activeChar.sendPacket(sm);
                        }
                    }

                    return false;
                }
            }

            return true;
        }
    }

    public boolean isConditionAttached() {
        return this._preConditions != null && !this._preConditions.isEmpty();
    }

    public boolean isQuestItem() {
        return this.getItemType() == EtcItemType.QUEST;
    }

    public final boolean isHeroItem() {
        return this._heroItem;
    }

    public boolean isOlyRestrictedItem() {
        return this._isOlyRestricted;
    }

    public boolean isPetItem() {
        return this.getItemType() == ArmorType.PET || this.getItemType() == WeaponType.PET;
    }

    public boolean isPotion() {
        return this.getItemType() == EtcItemType.POTION;
    }

    public boolean isElixir() {
        return this.getItemType() == EtcItemType.ELIXIR;
    }

    public ActionType getDefaultAction() {
        return this._defaultAction;
    }

    public String toString() {
        return this._name + " (" + this._itemId + ")";
    }

    public void addQuestEvent(Quest quest) {
        if (this._questEvents.isEmpty()) {
            this._questEvents = new ArrayList<>(3);
        }

        this._questEvents.add(quest);
    }

    public List<Quest> getQuestEvents() {
        return this._questEvents;
    }

    public final String getIcon() {
        return IconsTable.getIcon(this._itemId);
    }
}
