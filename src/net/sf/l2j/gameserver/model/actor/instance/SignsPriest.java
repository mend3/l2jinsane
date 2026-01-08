/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.Map;
import java.util.StringTokenizer;

public class SignsPriest extends Folk {
    public SignsPriest(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (player.getCurrentFolk() != null && player.getCurrentFolk().getObjectId() == this.getObjectId()) {
            if (command.startsWith("SevenSignsDesc")) {
                this.showChatWindow(player, Integer.parseInt(command.substring(15)), null, true);
            } else if (command.startsWith("SevenSigns")) {
                CabalType cabal = CabalType.NORMAL;
                int stoneType = 0;
                long ancientAdenaAmount = player.getAncientAdena();
                int val = Integer.parseInt(command.substring(11, 12).trim());
                if (command.length() > 12) {
                    val = Integer.parseInt(command.substring(11, 13).trim());
                }

                if (command.length() > 13) {
                    try {
                        cabal = CabalType.VALUES[Integer.parseInt(command.substring(14, 15).trim())];
                    } catch (Exception var54) {
                        try {
                            cabal = CabalType.VALUES[Integer.parseInt(command.substring(13, 14).trim())];
                        } catch (Exception var53) {
                            try {
                                StringTokenizer st = new StringTokenizer(command.trim());
                                st.nextToken();
                                cabal = CabalType.VALUES[Integer.parseInt(st.nextToken())];
                            } catch (Exception var52) {
                                LOGGER.warn("Failed to retrieve cabal from bypass command. NpcId: {}, command: {}.", new Object[]{this.getNpcId(), command});
                            }
                        }
                    }
                }

                switch (val) {
                    case 2:
                        if (!player.getInventory().validateCapacity(1)) {
                            player.sendPacket(SystemMessageId.SLOTS_FULL);
                        } else if (player.reduceAdena("SevenSigns", 500, this, true)) {
                            player.addItem("SevenSigns", 5707, 1, player, true);
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, val, "dawn", false);
                            } else {
                                this.showChatWindow(player, val, "dusk", false);
                            }
                        }
                        break;
                    case 3:
                    case 8:
                        this.showChatWindow(player, val, cabal.getShortName(), false);
                        break;
                    case 4:
                        SealType newSeal = SealType.VALUES[Integer.parseInt(command.substring(15))];
                        if (player.getClassId().level() >= 2) {
                            if (cabal == CabalType.DUSK && Config.ALT_GAME_CASTLE_DUSK && player.getClan() != null && player.getClan().hasCastle()) {
                                this.showChatWindow(player, "data/html/seven_signs/signs_33_dusk_no.htm");
                                return;
                            }

                            if (Config.ALT_GAME_CASTLE_DAWN && cabal == CabalType.DAWN) {
                                boolean allowJoinDawn = false;
                                if (player.getClan() != null && player.getClan().hasCastle()) {
                                    allowJoinDawn = true;
                                } else if (player.destroyItemByItemId("SevenSigns", 6388, 1, this, true)) {
                                    allowJoinDawn = true;
                                } else if (player.reduceAdena("SevenSigns", 50000, this, true)) {
                                    allowJoinDawn = true;
                                }

                                if (!allowJoinDawn) {
                                    this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn_fee.htm");
                                    return;
                                }
                            }
                        }

                        SevenSignsManager.getInstance().setPlayerInfo(player.getObjectId(), cabal, newSeal);
                        if (cabal == CabalType.DAWN) {
                            player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DAWN);
                        } else {
                            player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DUSK);
                        }

                        switch (newSeal) {
                            case AVARICE -> player.sendPacket(SystemMessageId.FIGHT_FOR_AVARICE);
                            case GNOSIS -> player.sendPacket(SystemMessageId.FIGHT_FOR_GNOSIS);
                            case STRIFE -> player.sendPacket(SystemMessageId.FIGHT_FOR_STRIFE);
                        }

                        this.showChatWindow(player, 4, cabal.getShortName(), false);
                        break;
                    case 5:
                        if (this instanceof DawnPriest) {
                            if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.NORMAL) {
                                this.showChatWindow(player, val, "dawn_no", false);
                            } else {
                                this.showChatWindow(player, val, "dawn", false);
                            }
                        } else if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == CabalType.NORMAL) {
                            this.showChatWindow(player, val, "dusk_no", false);
                        } else {
                            this.showChatWindow(player, val, "dusk", false);
                        }
                        break;
                    case 6:
                        stoneType = Integer.parseInt(command.substring(13));
                        ItemInstance blueStones = player.getInventory().getItemByItemId(6360);
                        ItemInstance greenStones = player.getInventory().getItemByItemId(6361);
                        ItemInstance redStones = player.getInventory().getItemByItemId(6362);
                        int blueStoneCount = blueStones == null ? 0 : blueStones.getCount();
                        int greenStoneCount = greenStones == null ? 0 : greenStones.getCount();
                        int redStoneCount = redStones == null ? 0 : redStones.getCount();
                        int contribScore = SevenSignsManager.getInstance().getPlayerContribScore(player.getObjectId());
                        boolean stonesFound = false;
                        if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB) {
                            player.sendPacket(SystemMessageId.CONTRIB_SCORE_EXCEEDED);
                        } else {
                            int redContribCount = 0;
                            int greenContribCount = 0;
                            int blueContribCount = 0;
                            String contribStoneColor = null;
                            String stoneColorContr = null;
                            int stoneCountContr = 0;
                            int stoneIdContr = 0;
                            switch (stoneType) {
                                case 1:
                                    contribStoneColor = "Blue";
                                    stoneColorContr = "blue";
                                    stoneIdContr = 6360;
                                    stoneCountContr = blueStoneCount;
                                    break;
                                case 2:
                                    contribStoneColor = "Green";
                                    stoneColorContr = "green";
                                    stoneIdContr = 6361;
                                    stoneCountContr = greenStoneCount;
                                    break;
                                case 3:
                                    contribStoneColor = "Red";
                                    stoneColorContr = "red";
                                    stoneIdContr = 6362;
                                    stoneCountContr = redStoneCount;
                                    break;
                                case 4:
                                    redContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - contribScore) / 10;
                                    if (redContribCount > redStoneCount) {
                                        redContribCount = redStoneCount;
                                    }

                                    int redStonesAll = contribScore + redContribCount * 10;
                                    greenContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - redStonesAll) / 5;
                                    if (greenContribCount > greenStoneCount) {
                                        greenContribCount = greenStoneCount;
                                    }

                                    redStonesAll += greenContribCount * 5;
                                    blueContribCount = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - redStonesAll) / 3;
                                    if (blueContribCount > blueStoneCount) {
                                        blueContribCount = blueStoneCount;
                                    }

                                    if (redContribCount > 0) {
                                        stonesFound |= player.destroyItemByItemId("SevenSigns", 6362, redContribCount, this, true);
                                    }

                                    if (greenContribCount > 0) {
                                        stonesFound |= player.destroyItemByItemId("SevenSigns", 6361, greenContribCount, this, true);
                                    }

                                    if (blueContribCount > 0) {
                                        stonesFound |= player.destroyItemByItemId("SevenSigns", 6360, blueContribCount, this, true);
                                    }

                                    if (!stonesFound) {
                                        if (this instanceof DawnPriest) {
                                            this.showChatWindow(player, val, "dawn_no_stones", false);
                                        } else {
                                            this.showChatWindow(player, val, "dusk_no_stones", false);
                                        }
                                    } else {
                                        contribScore = SevenSignsManager.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContribCount, greenContribCount, redContribCount);
                                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addItemNumber(contribScore));
                                        if (this instanceof DawnPriest) {
                                            this.showChatWindow(player, 6, "dawn", false);
                                        } else {
                                            this.showChatWindow(player, 6, "dusk", false);
                                        }
                                    }

                                    return;
                            }

                            String path;
                            if (this instanceof DawnPriest) {
                                path = "data/html/seven_signs/signs_6_dawn_contribute.htm";
                            } else {
                                path = "data/html/seven_signs/signs_6_dusk_contribute.htm";
                            }

                            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                            html.setFile(path);
                            html.replace("%contribStoneColor%", contribStoneColor);
                            html.replace("%stoneColor%", stoneColorContr);
                            html.replace("%stoneCount%", stoneCountContr);
                            html.replace("%stoneItemId%", stoneIdContr);
                            html.replace("%objectId%", this.getObjectId());
                            player.sendPacket(html);
                        }
                        break;
                    case 7:
                        int ancientAdenaConvert = 0;

                        try {
                            ancientAdenaConvert = Integer.parseInt(command.substring(13).trim());
                        } catch (NumberFormatException var55) {
                            this.showChatWindow(player, "data/html/seven_signs/blkmrkt_3.htm");
                            break;
                        } catch (StringIndexOutOfBoundsException var56) {
                            this.showChatWindow(player, "data/html/seven_signs/blkmrkt_3.htm");
                            break;
                        }

                        if (ancientAdenaConvert < 1) {
                            this.showChatWindow(player, "data/html/seven_signs/blkmrkt_3.htm");
                        } else if (ancientAdenaAmount < (long) ancientAdenaConvert) {
                            this.showChatWindow(player, "data/html/seven_signs/blkmrkt_4.htm");
                        } else {
                            player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true);
                            player.addAdena("SevenSigns", ancientAdenaConvert, this, true);
                            this.showChatWindow(player, "data/html/seven_signs/blkmrkt_5.htm");
                        }
                        break;
                    case 9:
                        if (SevenSignsManager.getInstance().isSealValidationPeriod() && SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) == SevenSignsManager.getInstance().getCabalHighestScore()) {
                            int ancientAdenaReward = SevenSignsManager.getInstance().getAncientAdenaReward(player.getObjectId());
                            if (ancientAdenaReward < 3) {
                                if (this instanceof DawnPriest) {
                                    this.showChatWindow(player, 9, "dawn_b", false);
                                } else {
                                    this.showChatWindow(player, 9, "dusk_b", false);
                                }
                            } else {
                                player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
                                if (this instanceof DawnPriest) {
                                    this.showChatWindow(player, 9, "dawn_a", false);
                                } else {
                                    this.showChatWindow(player, 9, "dusk_a", false);
                                }
                            }
                        }
                        break;
                    case 10:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                    case 32:
                    default:
                        this.showChatWindow(player, val, null, false);
                        break;
                    case 11:
                        try {
                            String portInfo = command.substring(14).trim();
                            StringTokenizer st = new StringTokenizer(portInfo);
                            int x = Integer.parseInt(st.nextToken());
                            int y = Integer.parseInt(st.nextToken());
                            int z = Integer.parseInt(st.nextToken());
                            int ancientAdenaCost = Integer.parseInt(st.nextToken());
                            if (ancientAdenaCost <= 0 || player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true)) {
                                player.teleportTo(x, y, z, 0);
                            }
                        } catch (Exception e) {
                            LOGGER.error("An error occurred while teleporting a player.", e);
                        }
                        break;
                    case 16:
                        if (this instanceof DawnPriest) {
                            this.showChatWindow(player, val, "dawn", false);
                        } else {
                            this.showChatWindow(player, val, "dusk", false);
                        }
                        break;
                    case 17:
                        stoneType = Integer.parseInt(command.substring(14));
                        int stoneId = 0;
                        int stoneCount = 0;
                        int stoneValue = 0;
                        String stoneColor = null;
                        switch (stoneType) {
                            case 1:
                                stoneColor = "blue";
                                stoneId = 6360;
                                stoneValue = 3;
                                break;
                            case 2:
                                stoneColor = "green";
                                stoneId = 6361;
                                stoneValue = 5;
                                break;
                            case 3:
                                stoneColor = "red";
                                stoneId = 6362;
                                stoneValue = 10;
                                break;
                            case 4:
                                ItemInstance blueStonesAll = player.getInventory().getItemByItemId(6360);
                                ItemInstance greenStonesAll = player.getInventory().getItemByItemId(6361);
                                ItemInstance redStonesAll = player.getInventory().getItemByItemId(6362);
                                int blueStoneCountAll = blueStonesAll == null ? 0 : blueStonesAll.getCount();
                                int greenStoneCountAll = greenStonesAll == null ? 0 : greenStonesAll.getCount();
                                int redStoneCountAll = redStonesAll == null ? 0 : redStonesAll.getCount();
                                int ancientAdenaRewardAll = 0;
                                ancientAdenaRewardAll = SevenSignsManager.calcScore(blueStoneCountAll, greenStoneCountAll, redStoneCountAll);
                                if (ancientAdenaRewardAll == 0) {
                                    if (this instanceof DawnPriest) {
                                        this.showChatWindow(player, 18, "dawn_no_stones", false);
                                    } else {
                                        this.showChatWindow(player, 18, "dusk_no_stones", false);
                                    }

                                    return;
                                }

                                if (blueStoneCountAll > 0) {
                                    player.destroyItemByItemId("SevenSigns", 6360, blueStoneCountAll, this, true);
                                }

                                if (greenStoneCountAll > 0) {
                                    player.destroyItemByItemId("SevenSigns", 6361, greenStoneCountAll, this, true);
                                }

                                if (redStoneCountAll > 0) {
                                    player.destroyItemByItemId("SevenSigns", 6362, redStoneCountAll, this, true);
                                }

                                player.addAncientAdena("SevenSigns", ancientAdenaRewardAll, this, true);
                                if (this instanceof DawnPriest) {
                                    this.showChatWindow(player, 18, "dawn", false);
                                } else {
                                    this.showChatWindow(player, 18, "dusk", false);
                                }

                                return;
                        }

                        ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
                        if (stoneInstance != null) {
                            stoneCount = stoneInstance.getCount();
                        }

                        String path;
                        if (this instanceof DawnPriest) {
                            path = "data/html/seven_signs/signs_17_dawn.htm";
                        } else {
                            path = "data/html/seven_signs/signs_17_dusk.htm";
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile(path);
                        html.replace("%stoneColor%", stoneColor);
                        html.replace("%stoneValue%", stoneValue);
                        html.replace("%stoneCount%", stoneCount);
                        html.replace("%stoneItemId%", stoneId);
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                        break;
                    case 18:
                        int convertStoneId = Integer.parseInt(command.substring(14, 18));
                        int convertCount = 0;

                        try {
                            convertCount = Integer.parseInt(command.substring(19).trim());
                        } catch (Exception var57) {
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, 18, "dawn_failed", false);
                            } else {
                                this.showChatWindow(player, 18, "dusk_failed", false);
                            }
                            break;
                        }

                        ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
                        if (convertItem != null) {
                            int ancientAdenaReward = 0;
                            int totalCount = convertItem.getCount();
                            if (convertCount <= totalCount && convertCount > 0) {
                                switch (convertStoneId) {
                                    case 6360 -> ancientAdenaReward = SevenSignsManager.calcScore(convertCount, 0, 0);
                                    case 6361 -> ancientAdenaReward = SevenSignsManager.calcScore(0, convertCount, 0);
                                    case 6362 -> ancientAdenaReward = SevenSignsManager.calcScore(0, 0, convertCount);
                                }

                                if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true)) {
                                    player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true);
                                    if (this instanceof DawnPriest) {
                                        this.showChatWindow(player, 18, "dawn", false);
                                    } else {
                                        this.showChatWindow(player, 18, "dusk", false);
                                    }
                                }
                            } else if (this instanceof DawnPriest) {
                                this.showChatWindow(player, 18, "dawn_low_stones", false);
                            } else {
                                this.showChatWindow(player, 18, "dusk_low_stones", false);
                            }
                        } else if (this instanceof DawnPriest) {
                            this.showChatWindow(player, 18, "dawn_no_stones", false);
                        } else {
                            this.showChatWindow(player, 18, "dusk_no_stones", false);
                        }
                        break;
                    case 19:
                        SealType chosenSeal = SealType.VALUES[Integer.parseInt(command.substring(16))];
                        String var10000 = chosenSeal.getShortName();
                        String fileSuffix = var10000 + "_" + cabal.getShortName();
                        this.showChatWindow(player, val, fileSuffix, false);
                        break;
                    case 20:
                        html = getNpcHtmlMessage();
                        player.sendPacket(html);
                        break;
                    case 21:
                        int contribStoneId = Integer.parseInt(command.substring(14, 18));
                        ItemInstance contribBlueStones = player.getInventory().getItemByItemId(6360);
                        ItemInstance contribGreenStones = player.getInventory().getItemByItemId(6361);
                        ItemInstance contribRedStones = player.getInventory().getItemByItemId(6362);
                        int contribBlueStoneCount = contribBlueStones == null ? 0 : contribBlueStones.getCount();
                        int contribGreenStoneCount = contribGreenStones == null ? 0 : contribGreenStones.getCount();
                        int contribRedStoneCount = contribRedStones == null ? 0 : contribRedStones.getCount();
                        int score = SevenSignsManager.getInstance().getPlayerContribScore(player.getObjectId());
                        int contributionCount = 0;
                        boolean contribStonesFound = false;
                        int redContrib = 0;
                        int greenContrib = 0;
                        int blueContrib = 0;

                        try {
                            contributionCount = Integer.parseInt(command.substring(19).trim());
                        } catch (Exception var59) {
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, 6, "dawn_failure", false);
                            } else {
                                this.showChatWindow(player, 6, "dusk_failure", false);
                            }
                            break;
                        }

                        switch (contribStoneId) {
                            case 6360:
                                blueContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / 3;
                                if (blueContrib > contribBlueStoneCount) {
                                    blueContrib = contributionCount;
                                }
                                break;
                            case 6361:
                                greenContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / 5;
                                if (greenContrib > contribGreenStoneCount) {
                                    greenContrib = contributionCount;
                                }
                                break;
                            case 6362:
                                redContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / 10;
                                if (redContrib > contribRedStoneCount) {
                                    redContrib = contributionCount;
                                }
                        }

                        if (redContrib > 0) {
                            contribStonesFound |= player.destroyItemByItemId("SevenSigns", 6362, redContrib, this, true);
                        }

                        if (greenContrib > 0) {
                            contribStonesFound |= player.destroyItemByItemId("SevenSigns", 6361, greenContrib, this, true);
                        }

                        if (blueContrib > 0) {
                            contribStonesFound |= player.destroyItemByItemId("SevenSigns", 6360, blueContrib, this, true);
                        }

                        if (!contribStonesFound) {
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, 6, "dawn_low_stones", false);
                            } else {
                                this.showChatWindow(player, 6, "dusk_low_stones", false);
                            }
                        } else {
                            score = SevenSignsManager.getInstance().addPlayerStoneContrib(player.getObjectId(), blueContrib, greenContrib, redContrib);
                            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addItemNumber(score));
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, 6, "dawn", false);
                            } else {
                                this.showChatWindow(player, 6, "dusk", false);
                            }
                        }
                        break;
                    case 33:
                        CabalType oldCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
                        if (oldCabal != CabalType.NORMAL) {
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, val, "dawn_member", false);
                            } else {
                                this.showChatWindow(player, val, "dusk_member", false);
                            }

                            return;
                        }

                        if (player.getClassId().level() == 0) {
                            if (this instanceof DawnPriest) {
                                this.showChatWindow(player, val, "dawn_firstclass", false);
                            } else {
                                this.showChatWindow(player, val, "dusk_firstclass", false);
                            }

                            return;
                        }

                        if (cabal == CabalType.DUSK && Config.ALT_GAME_CASTLE_DUSK) {
                            if (player.getClan() != null && player.getClan().hasCastle()) {
                                this.showChatWindow(player, "data/html/seven_signs/signs_33_dusk_no.htm");
                                break;
                            }
                        } else if (cabal == CabalType.DAWN && Config.ALT_GAME_CASTLE_DAWN && (player.getClan() == null || !player.getClan().hasCastle())) {
                            this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn_fee.htm");
                            break;
                        }

                        if (this instanceof DawnPriest) {
                            this.showChatWindow(player, val, "dawn", false);
                        } else {
                            this.showChatWindow(player, val, "dusk", false);
                        }
                        break;
                    case 34:
                        ItemInstance adena = player.getInventory().getItemByItemId(57);
                        ItemInstance certif = player.getInventory().getItemByItemId(6388);
                        boolean fee = true;
                        if (player.getClassId().level() < 2 || adena != null && adena.getCount() >= 50000 || certif != null && certif.getCount() >= 1) {
                            fee = false;
                        }

                        if (fee) {
                            this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn_no.htm");
                        } else {
                            this.showChatWindow(player, "data/html/seven_signs/signs_33_dawn.htm");
                        }
                }
            } else {
                super.onBypassFeedback(player, command);
            }

        }
    }

    private NpcHtmlMessage getNpcHtmlMessage() {
        StringBuilder sb = new StringBuilder();
        if (this instanceof DawnPriest) {
            sb.append("<html><body>Priest of Dawn:<br><font color=\"LEVEL\">[ Seal Status ]</font><br>");
        } else {
            sb.append("<html><body>Dusk Priestess:<br><font color=\"LEVEL\">[ Status of the Seals ]</font><br>");
        }

        for (Map.Entry<SealType, CabalType> entry : SevenSignsManager.getInstance().getSealOwners().entrySet()) {
            SealType seal = entry.getKey();
            CabalType sealOwner = entry.getValue();
            if (sealOwner != CabalType.NORMAL) {
                String var10001 = seal.getFullName();
                sb.append("[").append(var10001).append(": ").append(sealOwner.getFullName()).append("]<br>");
            } else {
                sb.append("[").append(seal.getFullName()).append(": Nothingness]<br>");
            }
        }

        sb.append("<a action=\"bypass -h npc_").append(this.getObjectId()).append("_Chat 0\">Go back.</a></body></html>");
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setHtml(sb.toString());
        return html;
    }

    public void showChatWindow(Player player, int val) {
        int npcId = this.getTemplate().getNpcId();
        String filename = "data/html/seven_signs/";
        CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
        CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
        switch (npcId) {
            case 31092:
                filename = filename + "blkmrkt_1.htm";
                break;
            case 31113:
                CabalType sealAvariceOwner = SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE);
                switch (winningCabal) {
                    case DAWN:
                        if (playerCabal != winningCabal || playerCabal != sealAvariceOwner) {
                            player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
                            player.sendPacket(ActionFailed.STATIC_PACKET);
                            return;
                        }
                        break;
                    case DUSK:
                        if (playerCabal != winningCabal || playerCabal != sealAvariceOwner) {
                            player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                            player.sendPacket(ActionFailed.STATIC_PACKET);
                            return;
                        }
                        break;
                    default:
                        player.sendPacket(SystemMessageId.QUEST_EVENT_PERIOD);
                        return;
                }

                filename = filename + "mammmerch_1.htm";
                break;
            case 31126:
                CabalType sealGnosisOwner = SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS);
                switch (winningCabal) {
                    case DAWN:
                        if (playerCabal != winningCabal || playerCabal != sealGnosisOwner) {
                            player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
                            player.sendPacket(ActionFailed.STATIC_PACKET);
                            return;
                        }
                        break;
                    case DUSK:
                        if (playerCabal != winningCabal || playerCabal != sealGnosisOwner) {
                            player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
                            player.sendPacket(ActionFailed.STATIC_PACKET);
                            return;
                        }
                }

                filename = filename + "mammblack_1.htm";
                break;
            default:
                filename = this.getHtmlPath(npcId, val);
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    private void showChatWindow(Player player, int val, String suffix, boolean isDescription) {
        String filename = "data/html/seven_signs/";
        filename = filename + (isDescription ? "desc_" + val : "signs_" + val);
        filename = filename + (suffix != null ? "_" + suffix + ".htm" : ".htm");
        this.showChatWindow(player, filename);
    }
}
