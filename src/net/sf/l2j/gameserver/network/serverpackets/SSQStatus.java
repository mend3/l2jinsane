/**/
package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.FestivalOfDarknessManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.FestivalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.Iterator;
import java.util.Map.Entry;

public class SSQStatus extends L2GameServerPacket {
    private final int _objectId;
    private final int _page;

    public SSQStatus(int objectId, int recordPage) {
        this._objectId = objectId;
        this._page = recordPage;
    }

    protected final void writeImpl() {
        CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
        int totalDawnMembers = SevenSignsManager.getInstance().getTotalMembers(CabalType.DAWN);
        int totalDuskMembers = SevenSignsManager.getInstance().getTotalMembers(CabalType.DUSK);
        this.writeC(245);
        this.writeC(this._page);
        this.writeC(SevenSignsManager.getInstance().getCurrentPeriod().ordinal());
        int dawnPercent = 0;
        int duskPercent = 0;
        Iterator var19;
        Entry entry;
        SealType seal;
        CabalType sealOwner;
        int dawnProportion;
        int duskProportion;
        switch (this._page) {
            case 1:
                this.writeD(SevenSignsManager.getInstance().getCurrentCycle());
                switch (SevenSignsManager.getInstance().getCurrentPeriod()) {
                    case RECRUITING:
                        this.writeD(SystemMessageId.INITIAL_PERIOD.getId());
                        this.writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
                        break;
                    case COMPETITION:
                        this.writeD(SystemMessageId.QUEST_EVENT_PERIOD.getId());
                        this.writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
                        break;
                    case RESULTS:
                        this.writeD(SystemMessageId.RESULTS_PERIOD.getId());
                        this.writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
                        break;
                    case SEAL_VALIDATION:
                        this.writeD(SystemMessageId.VALIDATION_PERIOD.getId());
                        this.writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
                }

                this.writeC(SevenSignsManager.getInstance().getPlayerCabal(this._objectId).ordinal());
                this.writeC(SevenSignsManager.getInstance().getPlayerSeal(this._objectId).ordinal());
                this.writeD(SevenSignsManager.getInstance().getPlayerStoneContrib(this._objectId));
                this.writeD(SevenSignsManager.getInstance().getPlayerAdenaCollect(this._objectId));
                double dawnStoneScore = SevenSignsManager.getInstance().getCurrentStoneScore(CabalType.DAWN);
                int dawnFestivalScore = SevenSignsManager.getInstance().getCurrentFestivalScore(CabalType.DAWN);
                double duskStoneScore = SevenSignsManager.getInstance().getCurrentStoneScore(CabalType.DUSK);
                int duskFestivalScore = SevenSignsManager.getInstance().getCurrentFestivalScore(CabalType.DUSK);
                double totalStoneScore = duskStoneScore + dawnStoneScore;
                int duskStoneScoreProp = 0;
                int dawnStoneScoreProp = 0;
                if (totalStoneScore != 0.0D) {
                    duskStoneScoreProp = Math.round((float) duskStoneScore / (float) totalStoneScore * 500.0F);
                    dawnStoneScoreProp = Math.round((float) dawnStoneScore / (float) totalStoneScore * 500.0F);
                }

                int duskTotalScore = SevenSignsManager.getInstance().getCurrentScore(CabalType.DUSK);
                int dawnTotalScore = SevenSignsManager.getInstance().getCurrentScore(CabalType.DAWN);
                int totalOverallScore = duskTotalScore + dawnTotalScore;
                if (totalOverallScore != 0) {
                    dawnPercent = Math.round((float) dawnTotalScore / (float) totalOverallScore * 100.0F);
                    duskPercent = Math.round((float) duskTotalScore / (float) totalOverallScore * 100.0F);
                }

                this.writeD(duskStoneScoreProp);
                this.writeD(duskFestivalScore);
                this.writeD(duskTotalScore);
                this.writeC(duskPercent);
                this.writeD(dawnStoneScoreProp);
                this.writeD(dawnFestivalScore);
                this.writeD(dawnTotalScore);
                this.writeC(dawnPercent);
                break;
            case 2:
                this.writeH(1);
                this.writeC(5);
                FestivalType[] var32 = FestivalType.VALUES;
                int var33 = var32.length;

                for (int var34 = 0; var34 < var33; ++var34) {
                    FestivalType level = var32[var34];
                    dawnProportion = level.ordinal();
                    this.writeC(dawnProportion + 1);
                    this.writeD(level.getMaxScore());
                    duskProportion = FestivalOfDarknessManager.getInstance().getHighestScore(CabalType.DUSK, dawnProportion);
                    int dawnScore = FestivalOfDarknessManager.getInstance().getHighestScore(CabalType.DAWN, dawnProportion);
                    this.writeD(duskProportion);
                    StatSet highScoreData = FestivalOfDarknessManager.getInstance().getHighestScoreData(CabalType.DUSK, dawnProportion);
                    String[] partyMembers = highScoreData.getString("members").split(",");
                    String[] var28;
                    int var29;
                    int var30;
                    String partyMember;
                    if (partyMembers != null) {
                        this.writeC(partyMembers.length);
                        var28 = partyMembers;
                        var29 = partyMembers.length;

                        for (var30 = 0; var30 < var29; ++var30) {
                            partyMember = var28[var30];
                            this.writeS(partyMember);
                        }
                    } else {
                        this.writeC(0);
                    }

                    this.writeD(dawnScore);
                    highScoreData = FestivalOfDarknessManager.getInstance().getHighestScoreData(CabalType.DAWN, dawnProportion);
                    partyMembers = highScoreData.getString("members").split(",");
                    if (partyMembers != null) {
                        this.writeC(partyMembers.length);
                        var28 = partyMembers;
                        var29 = partyMembers.length;

                        for (var30 = 0; var30 < var29; ++var30) {
                            partyMember = var28[var30];
                            this.writeS(partyMember);
                        }
                    } else {
                        this.writeC(0);
                    }
                }

                return;
            case 3:
                this.writeC(10);
                this.writeC(35);
                this.writeC(3);
                var19 = SevenSignsManager.getInstance().getSealOwners().entrySet().iterator();

                while (var19.hasNext()) {
                    entry = (Entry) var19.next();
                    seal = (SealType) entry.getKey();
                    sealOwner = (CabalType) entry.getValue();
                    dawnProportion = SevenSignsManager.getInstance().getSealProportion(seal, CabalType.DAWN);
                    duskProportion = SevenSignsManager.getInstance().getSealProportion(seal, CabalType.DUSK);
                    this.writeC(seal.ordinal());
                    this.writeC(sealOwner.ordinal());
                    if (totalDuskMembers == 0) {
                        if (totalDawnMembers == 0) {
                            this.writeC(0);
                            this.writeC(0);
                        } else {
                            this.writeC(0);
                            this.writeC(Math.round((float) dawnProportion / (float) totalDawnMembers * 100.0F));
                        }
                    } else if (totalDawnMembers == 0) {
                        this.writeC(Math.round((float) duskProportion / (float) totalDuskMembers * 100.0F));
                        this.writeC(0);
                    } else {
                        this.writeC(Math.round((float) duskProportion / (float) totalDuskMembers * 100.0F));
                        this.writeC(Math.round((float) dawnProportion / (float) totalDawnMembers * 100.0F));
                    }
                }

                return;
            case 4:
                this.writeC(winningCabal.ordinal());
                this.writeC(3);
                var19 = SevenSignsManager.getInstance().getSealOwners().entrySet().iterator();

                while (var19.hasNext()) {
                    entry = (Entry) var19.next();
                    seal = (SealType) entry.getKey();
                    sealOwner = (CabalType) entry.getValue();
                    dawnProportion = SevenSignsManager.getInstance().getSealProportion(seal, CabalType.DAWN);
                    duskProportion = SevenSignsManager.getInstance().getSealProportion(seal, CabalType.DUSK);
                    dawnPercent = Math.round((float) dawnProportion / (totalDawnMembers == 0 ? 1.0F : (float) totalDawnMembers) * 100.0F);
                    duskPercent = Math.round((float) duskProportion / (totalDuskMembers == 0 ? 1.0F : (float) totalDuskMembers) * 100.0F);
                    this.writeC(sealOwner.ordinal());
                    switch (sealOwner) {
                        case NORMAL:
                            switch (winningCabal) {
                                case NORMAL:
                                    this.writeC(CabalType.NORMAL.ordinal());
                                    this.writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
                                    continue;
                                case DAWN:
                                    if (dawnPercent >= 35) {
                                        this.writeC(CabalType.DAWN.ordinal());
                                        this.writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
                                    }
                                    continue;
                                case DUSK:
                                    if (duskPercent >= 35) {
                                        this.writeC(CabalType.DUSK.ordinal());
                                        this.writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
                                    }
                                default:
                                    continue;
                            }
                        case DAWN:
                            switch (winningCabal) {
                                case NORMAL:
                                    if (dawnPercent >= 10) {
                                        this.writeC(CabalType.DAWN.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
                                    }
                                    continue;
                                case DAWN:
                                    if (dawnPercent >= 10) {
                                        this.writeC(sealOwner.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                                    }
                                    continue;
                                case DUSK:
                                    if (duskPercent >= 35) {
                                        this.writeC(CabalType.DUSK.ordinal());
                                        this.writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                                    } else if (dawnPercent >= 10) {
                                        this.writeC(CabalType.DAWN.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                                    }
                                default:
                                    continue;
                            }
                        case DUSK:
                            switch (winningCabal) {
                                case NORMAL:
                                    if (duskPercent >= 10) {
                                        this.writeC(CabalType.DUSK.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
                                    }
                                    break;
                                case DAWN:
                                    if (dawnPercent >= 35) {
                                        this.writeC(CabalType.DAWN.ordinal());
                                        this.writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
                                    } else if (duskPercent >= 10) {
                                        this.writeC(sealOwner.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                                    }
                                    break;
                                case DUSK:
                                    if (duskPercent >= 10) {
                                        this.writeC(sealOwner.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
                                    } else {
                                        this.writeC(CabalType.NORMAL.ordinal());
                                        this.writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
                                    }
                            }
                    }
                }
        }

    }
}