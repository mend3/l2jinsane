package net.sf.l2j.gameserver.network.serverpackets;

import mods.dressme.DressMeData;
import mods.dressme.SkinPackage;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.enums.PolyType;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;

import java.util.Iterator;

public class UserInfo extends L2GameServerPacket {
    private final Player _player;

    private int _relation;

    public UserInfo(Player player) {
        this._player = player;
        this._relation = this._player.isClanLeader() ? 64 : 0;
        if (this._player.getSiegeState() == 1)
            this._relation |= 0x180;
        if (this._player.getSiegeState() == 2)
            this._relation |= 0x80;
    }

    protected final void writeImpl() {
        writeC(4);
        writeD(this._player.getX());
        writeD(this._player.getY());
        writeD(this._player.getZ());
        writeD(this._player.getHeading());
        writeD(this._player.getObjectId());
        writeS((this._player.getPolyTemplate() != null) ? this._player.getPolyTemplate().getName() : this._player.getName());
        writeD(this._player.getRace().ordinal());
        writeD(this._player.getAppearance().getSex().ordinal());
        writeD((this._player.getClassIndex() == 0) ? this._player.getClassId().getId() : this._player.getBaseClass());
        writeD(this._player.getLevel());
        writeQ(this._player.getExp());
        writeD(this._player.getSTR());
        writeD(this._player.getDEX());
        writeD(this._player.getCON());
        writeD(this._player.getINT());
        writeD(this._player.getWIT());
        writeD(this._player.getMEN());
        writeD(this._player.getMaxHp());
        writeD((int) this._player.getCurrentHp());
        writeD(this._player.getMaxMp());
        writeD((int) this._player.getCurrentMp());
        writeD(this._player.getSp());
        writeD(this._player.getCurrentLoad());
        writeD(this._player.getMaxLoad());
        writeD((this._player.getActiveWeaponItem() != null) ? 40 : 20);
        writeD(this._player.getInventory().getPaperdollObjectId(16));
        writeD(this._player.getInventory().getPaperdollObjectId(2));
        writeD(this._player.getInventory().getPaperdollObjectId(1));
        writeD(this._player.getInventory().getPaperdollObjectId(3));
        writeD(this._player.getInventory().getPaperdollObjectId(5));
        writeD(this._player.getInventory().getPaperdollObjectId(4));
        writeD(this._player.getInventory().getPaperdollObjectId(6));
        if (Config.ALLOW_DRESS_ME_SYSTEM) {
            if (this._player.getWeaponSkinOption() > 0 && getWeaponOption(this._player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() : this._player.getInventory().getPaperdollObjectId(7));
            } else {
                writeD(this._player.getInventory().getPaperdollObjectId(7));
            }
            if (this._player.getShieldSkinOption() > 0 && getShieldOption(this._player.getShieldSkinOption()) != null) {
                writeD((getShieldOption(this._player.getShieldSkinOption()).getShieldId() != 0) ? getShieldOption(this._player.getShieldSkinOption()).getShieldId() : this._player.getInventory().getPaperdollObjectId(8));
            } else {
                writeD(this._player.getInventory().getPaperdollObjectId(8));
            }
            if (this._player.getArmorSkinOption() > 0 && getArmorOption(this._player.getArmorSkinOption()) != null) {
                writeD((getArmorOption(this._player.getArmorSkinOption()).getGlovesId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getGlovesId() : this._player.getInventory().getPaperdollObjectId(9));
                writeD((getArmorOption(this._player.getArmorSkinOption()).getChestId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getChestId() : this._player.getInventory().getPaperdollObjectId(10));
                writeD((getArmorOption(this._player.getArmorSkinOption()).getLegsId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getLegsId() : this._player.getInventory().getPaperdollObjectId(11));
                writeD((getArmorOption(this._player.getArmorSkinOption()).getFeetId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getFeetId() : this._player.getInventory().getPaperdollObjectId(12));
            } else {
                writeD(this._player.getInventory().getPaperdollObjectId(9));
                writeD(this._player.getInventory().getPaperdollObjectId(10));
                writeD(this._player.getInventory().getPaperdollObjectId(11));
                writeD(this._player.getInventory().getPaperdollObjectId(12));
            }
            writeD(this._player.getInventory().getPaperdollObjectId(13));
            if (this._player.getWeaponSkinOption() > 0 && getWeaponOption(this._player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() : this._player.getInventory().getPaperdollObjectId(7));
            } else {
                writeD(this._player.getInventory().getPaperdollObjectId(7));
            }
            if (this._player.getHairSkinOption() > 0 && getHairOption(this._player.getHairSkinOption()) != null) {
                writeD((getHairOption(this._player.getHairSkinOption()).getHairId() != 0) ? getHairOption(this._player.getHairSkinOption()).getHairId() : this._player.getInventory().getPaperdollObjectId(15));
            } else {
                writeD(this._player.getInventory().getPaperdollObjectId(15));
            }
            if (this._player.getFaceSkinOption() > 0 && getFaceOption(this._player.getFaceSkinOption()) != null) {
                writeD((getFaceOption(this._player.getFaceSkinOption()).getFaceId() != 0) ? getFaceOption(this._player.getFaceSkinOption()).getFaceId() : this._player.getInventory().getPaperdollObjectId(14));
            } else {
                writeD(this._player.getInventory().getPaperdollObjectId(14));
            }
        } else {
            writeD(this._player.getInventory().getPaperdollObjectId(7));
            writeD(this._player.getInventory().getPaperdollObjectId(8));
            writeD(this._player.getInventory().getPaperdollObjectId(9));
            writeD(this._player.getInventory().getPaperdollObjectId(10));
            writeD(this._player.getInventory().getPaperdollObjectId(11));
            writeD(this._player.getInventory().getPaperdollObjectId(12));
            writeD(this._player.getInventory().getPaperdollObjectId(13));
            writeD(this._player.getInventory().getPaperdollObjectId(7));
            writeD(this._player.getInventory().getPaperdollObjectId(15));
            writeD(this._player.getInventory().getPaperdollObjectId(14));
        }
        writeD(this._player.getInventory().getPaperdollItemId(16));
        writeD(this._player.getInventory().getPaperdollItemId(2));
        writeD(this._player.getInventory().getPaperdollItemId(1));
        writeD(this._player.getInventory().getPaperdollItemId(3));
        writeD(this._player.getInventory().getPaperdollItemId(5));
        writeD(this._player.getInventory().getPaperdollItemId(4));
        writeD(this._player.getInventory().getPaperdollItemId(6));
        if (Config.ALLOW_DRESS_ME_SYSTEM) {
            if (this._player.getWeaponSkinOption() > 0 && getWeaponOption(this._player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() : this._player.getInventory().getPaperdollItemId(7));
            } else {
                writeD(this._player.getInventory().getPaperdollItemId(7));
            }
            if (this._player.getShieldSkinOption() > 0 && getShieldOption(this._player.getShieldSkinOption()) != null) {
                writeD((getShieldOption(this._player.getShieldSkinOption()).getShieldId() != 0) ? getShieldOption(this._player.getShieldSkinOption()).getShieldId() : this._player.getInventory().getPaperdollItemId(8));
            } else {
                writeD(this._player.getInventory().getPaperdollItemId(8));
            }
            if (this._player.getArmorSkinOption() > 0 && getArmorOption(this._player.getArmorSkinOption()) != null) {
                writeD((getArmorOption(this._player.getArmorSkinOption()).getGlovesId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getGlovesId() : this._player.getInventory().getPaperdollItemId(9));
                writeD((getArmorOption(this._player.getArmorSkinOption()).getChestId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getChestId() : this._player.getInventory().getPaperdollItemId(10));
                writeD((getArmorOption(this._player.getArmorSkinOption()).getLegsId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getLegsId() : this._player.getInventory().getPaperdollItemId(11));
                writeD((getArmorOption(this._player.getArmorSkinOption()).getFeetId() != 0) ? getArmorOption(this._player.getArmorSkinOption()).getFeetId() : this._player.getInventory().getPaperdollItemId(12));
            } else {
                writeD(this._player.getInventory().getPaperdollItemId(9));
                writeD(this._player.getInventory().getPaperdollItemId(10));
                writeD(this._player.getInventory().getPaperdollItemId(11));
                writeD(this._player.getInventory().getPaperdollItemId(12));
            }
            writeD(this._player.getInventory().getPaperdollItemId(13));
            if (this._player.getWeaponSkinOption() > 0 && getWeaponOption(this._player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(this._player.getWeaponSkinOption()).getWeaponId() : this._player.getInventory().getPaperdollItemId(7));
            } else {
                writeD(this._player.getInventory().getPaperdollItemId(7));
            }
            if (this._player.getHairSkinOption() > 0 && getHairOption(this._player.getHairSkinOption()) != null) {
                writeD((getHairOption(this._player.getHairSkinOption()).getHairId() != 0) ? getHairOption(this._player.getHairSkinOption()).getHairId() : this._player.getInventory().getPaperdollItemId(15));
            } else {
                writeD(this._player.getInventory().getPaperdollItemId(15));
            }
            if (this._player.getFaceSkinOption() > 0 && getFaceOption(this._player.getFaceSkinOption()) != null) {
                writeD((getFaceOption(this._player.getFaceSkinOption()).getFaceId() != 0) ? getFaceOption(this._player.getFaceSkinOption()).getFaceId() : this._player.getInventory().getPaperdollItemId(14));
            } else {
                writeD(this._player.getInventory().getPaperdollItemId(14));
            }
        } else {
            writeD(this._player.getInventory().getPaperdollItemId(7));
            writeD(this._player.getInventory().getPaperdollItemId(8));
            writeD(this._player.getInventory().getPaperdollItemId(9));
            writeD(this._player.getInventory().getPaperdollItemId(10));
            writeD(this._player.getInventory().getPaperdollItemId(11));
            writeD(this._player.getInventory().getPaperdollItemId(12));
            writeD(this._player.getInventory().getPaperdollItemId(13));
            writeD(this._player.getInventory().getPaperdollItemId(7));
            writeD(this._player.getInventory().getPaperdollItemId(15));
            writeD(this._player.getInventory().getPaperdollItemId(14));
        }
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeD(this._player.getInventory().getPaperdollAugmentationId(7));
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeD(this._player.getInventory().getPaperdollAugmentationId(8));
        writeH(0);
        writeH(0);
        writeH(0);
        writeH(0);
        writeD(this._player.getPAtk(null));
        writeD(this._player.getPAtkSpd());
        writeD(this._player.getPDef(null));
        writeD(this._player.getEvasionRate(null));
        writeD(this._player.getAccuracy());
        writeD(this._player.getCriticalHit(null, null));
        writeD(this._player.getMAtk(null, null));
        writeD(this._player.getMAtkSpd());
        writeD(this._player.getPAtkSpd());
        writeD(this._player.getMDef(null, null));
        writeD(this._player.getPvpFlag());
        writeD(this._player.getKarma());
        int runSpd = this._player.getStat().getBaseRunSpeed();
        int walkSpd = this._player.getStat().getBaseWalkSpeed();
        int swimSpd = this._player.getStat().getBaseSwimSpeed();
        writeD(runSpd);
        writeD(walkSpd);
        writeD(swimSpd);
        writeD(swimSpd);
        writeD(0);
        writeD(0);
        writeD(this._player.isFlying() ? runSpd : 0);
        writeD(this._player.isFlying() ? walkSpd : 0);
        writeF(this._player.getStat().getMovementSpeedMultiplier());
        writeF(this._player.getStat().getAttackSpeedMultiplier());
        Summon summon = this._player.getSummon();
        if (this._player.getMountType() != 0 && summon != null) {
            writeF(summon.getCollisionRadius());
            writeF(summon.getCollisionHeight());
        } else {
            writeF(this._player.getCollisionRadius());
            writeF(this._player.getCollisionHeight());
        }
        writeD(this._player.getAppearance().getHairStyle());
        writeD(this._player.getAppearance().getHairColor());
        writeD(this._player.getAppearance().getFace());
        writeD(this._player.isGM() ? 1 : 0);
        writeS((this._player.getPolyType() != PolyType.DEFAULT) ? "Morphed" : this._player.getTitle());
        writeD(this._player.getClanId());
        writeD(this._player.getClanCrestId());
        writeD(this._player.getAllyId());
        writeD(this._player.getAllyCrestId());
        writeD(this._relation);
        writeC(this._player.getMountType());
        writeC(this._player.getStoreType().getId());
        writeC(this._player.hasDwarvenCraft() ? 1 : 0);
        writeD(this._player.getPkKills());
        writeD(this._player.getPvpKills());
        writeH(this._player.getCubics().size());
        for (Iterator<Integer> iterator = this._player.getCubics().keySet().iterator(); iterator.hasNext(); ) {
            int id = iterator.next();
            writeH(id);
        }
        writeC(this._player.isInPartyMatchRoom() ? 1 : 0);
        writeD((this._player.getAppearance().getInvisible() && this._player.isGM()) ? (this._player.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask()) : this._player.getAbnormalEffect());
        writeC(0);
        writeD(this._player.getClanPrivileges());
        writeH(this._player.getRecomLeft());
        writeH(this._player.getRecomHave());
        writeD((this._player.getMountNpcId() > 0) ? (this._player.getMountNpcId() + 1000000) : 0);
        writeH(this._player.getInventoryLimit());
        writeD(this._player.getClassId().getId());
        writeD(0);
        writeD(this._player.getMaxCp());
        writeD((int) this._player.getCurrentCp());
        writeC(this._player.isMounted() ? 0 : this._player.getEnchantEffect());
        writeC((Config.PLAYER_SPAWN_PROTECTION > 0 && this._player.isSpawnProtected()) ? TeamType.BLUE.getId() : this._player.getTeam().getId());
        writeD(this._player.getClanCrestLargeId());
        writeC(this._player.isNoble() ? 1 : 0);
        writeC((this._player.isHero() || (this._player.isGM() && Config.GM_HERO_AURA) || this._player.getIsPVPHero()) ? 1 : 0);
        writeC(this._player.isFishing() ? 1 : 0);
        writeLoc(this._player.getFishingStance().getLoc());
        writeD(this._player.getAppearance().getNameColor());
        writeC(this._player.isRunning() ? 1 : 0);
        writeD(this._player.getPledgeClass());
        writeD(this._player.getPledgeType());
        writeD(this._player.getAppearance().getTitleColor());
        writeD(CursedWeaponManager.getInstance().getCurrentStage(this._player.getCursedWeaponEquippedId()));
    }

    public SkinPackage getArmorOption(int option) {
        return DressMeData.getInstance().getArmorSkinsPackage(option);
    }

    public SkinPackage getWeaponOption(int option) {
        return DressMeData.getInstance().getWeaponSkinsPackage(option);
    }

    public SkinPackage getHairOption(int option) {
        return DressMeData.getInstance().getHairSkinsPackage(option);
    }

    public SkinPackage getFaceOption(int option) {
        return DressMeData.getInstance().getFaceSkinsPackage(option);
    }

    public SkinPackage getShieldOption(int option) {
        return DressMeData.getInstance().getShieldSkinsPackage(option);
    }
}
