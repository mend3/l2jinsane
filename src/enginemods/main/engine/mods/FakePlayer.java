package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.data.FakePlayerData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.engine.ai.FakePlayerAI;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.util.UtilPlayer;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import enginemods.main.util.builders.html.HtmlBuilder.HtmlType;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.ClassType;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.serverpackets.TitleUpdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class FakePlayer extends AbstractMods {
    private static final int FAKE_MAX_LEVEL;
    private static final int FAKE_MAX_PVP;
    private static final int FAKE_MAX_PK;
    private static final int FAKE_CHANCE_SIT;
    private static final int FAKE_CHANCE_HAS_CLAN;
    private static final int SPAWN_RANGE;
    private static final int CHANCE_SELL_BUFF = 10;
    private static final int[] FAKE_SET_WARRIOR;
    private static final int[] FAKE_WEAPON_WARRIOR;
    private static final int[] FAKE_SET_MAGE;
    private static final int[] FAKE_WEAPON_MAGE;
    private static int _crestId;

    static {
        FAKE_MAX_LEVEL = ConfigData.FAKE_LEVEL;
        FAKE_MAX_PVP = ConfigData.FAKE_MAX_PVP;
        FAKE_MAX_PK = ConfigData.FAKE_MAX_PK;
        FAKE_CHANCE_SIT = ConfigData.FAKE_CHANCE_SIT;
        FAKE_CHANCE_HAS_CLAN = ConfigData.FAKE_CHANCE_HAS_CLAN;
        SPAWN_RANGE = ConfigData.SPAWN_RANGE;
        FAKE_SET_WARRIOR = new int[]{6379, 6373, 2392, 356};
        FAKE_WEAPON_WARRIOR = new int[]{6368, 6369, 6370, 6372};
        FAKE_SET_MAGE = new int[]{6383, 2409, 2407, 439};
        FAKE_WEAPON_MAGE = new int[]{6366, 7722, 6313, 5641};
        _crestId = 0;
    }

    public FakePlayer() {
        this.registerMod(ConfigData.ENABLE_fakePlayers);
    }

    private static boolean createShops() {
        boolean offlineShop = false;
        String type = "NONE";
        int chance = Rnd.get(100);
        if (chance < 10) {
            type = "SELL_BUFF";
            offlineShop = true;
        } else if (chance < 30) {
            type = "BUY";
            offlineShop = true;
        } else if (chance < 50) {
            type = "SELL";
            offlineShop = true;
        } else if (chance < 80) {
            type = "AI";
        }

        if (offlineShop) {
        }

        return true;
    }

    private static Player createPlayer(Player admin, int offSet, int templateId, int lvl) {
        String playerName;
        for (playerName = FakePlayerData.getName(); PlayerInfoTable.getInstance().getPlayerObjectId(playerName) > 0; playerName = FakePlayerData.getName()) {
        }

        String accountName = "fake" + Rnd.get(Integer.MAX_VALUE);
        Iterator var6 = PlayerData.getAllPlayers().iterator();

        while (var6.hasNext()) {
            PlayerHolder ph = (PlayerHolder) var6.next();
            if (ph.getAccountName().equals(accountName)) {
                accountName = "fake" + Rnd.get(Integer.MAX_VALUE);
            }
        }

        Sex sex = Sex.values()[Rnd.get(2)];
        byte hairStyle = (byte) (sex == Sex.MALE ? Rnd.get(5) : Rnd.get(7));
        byte face = (byte) Rnd.get(3);
        PlayerTemplate template = net.sf.l2j.gameserver.data.xml.PlayerData.getInstance().getTemplate(templateId);
        List<Integer> items = new ArrayList();
        int chestId = template.getClassId().getType() != ClassType.FIGHTER ? FAKE_SET_MAGE[Rnd.get(FAKE_SET_MAGE.length - 1)] : FAKE_SET_WARRIOR[Rnd.get(FAKE_SET_WARRIOR.length - 1)];
        if (ArmorSetData.getInstance().getSet(chestId) != null) {
            int[] var12 = ArmorSetData.getInstance().getSet(chestId).getSetItemsId();
            int var13 = var12.length;

            for (int var14 = 0; var14 < var13; ++var14) {
                int it = var12[var14];
                items.add(it);
            }
        } else {
            System.out.println("chestId-> " + chestId + " no tiene ningun set asignado");
        }

        items.add(template.getClassId().getType() != ClassType.FIGHTER ? FAKE_WEAPON_MAGE[Rnd.get(FAKE_WEAPON_MAGE.length)] : FAKE_WEAPON_WARRIOR[Rnd.get(FAKE_WEAPON_WARRIOR.length)]);
        return UtilPlayer.createPlayer(admin, playerName, accountName, template, lvl, sex, hairStyle, face, items, offSet);
    }

    private static void storeClan(Clan clan) {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) values (?,?,?,?,?,?,?,?,?,?)");

                try {
                    statement.setInt(1, clan.getClanId());
                    statement.setString(2, clan.getName());
                    statement.setInt(3, clan.getLevel());
                    statement.setInt(4, clan.getCastleId());
                    statement.setInt(5, clan.getAllyId());
                    statement.setString(6, clan.getAllyName());
                    statement.setInt(7, clan.getLeaderId());
                    statement.setInt(8, clan.getCrestId());
                    statement.setInt(9, clan.getCrestLargeId());
                    statement.setInt(10, clan.getAllyCrestId());
                    statement.execute();
                } catch (Throwable var7) {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (statement != null) {
                    statement.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }

    public static FakePlayer getInstance() {
        return FakePlayer.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.restoreAllFakePlayers();
            case END:
            default:
        }
    }

    private void restoreAllFakePlayers() {
        Iterator var1 = PlayerData.getAllPlayers().iterator();

        while (true) {
            PlayerHolder ph;
            String fake;
            do {
                if (!var1.hasNext()) {
                    return;
                }

                ph = (PlayerHolder) var1.next();
                fake = this.getValueDB(ph.getObjectId(), "fakePlayer");
            } while (fake == null);

            Player fakePlayer = UtilPlayer.spawnPlayer(ph.getObjectId());
            boolean insideCity = MapRegionData.getTown(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ()) != null;
            if (fake.equals("sitDown") && insideCity) {
                fakePlayer.sitDown();
            } else {
                String posToFarm = this.getValueDB(ph.getObjectId(), "posToFarm");
                if (posToFarm != null) {
                    ph.setPosToFarm(posToFarm);
                    fakePlayer.teleportTo(ph.getPosToFarm(), 0);
                } else if (!insideCity) {
                    ph.setPosToFarm(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ());
                }

                fakePlayer.detachAI();
                fakePlayer.setAI(new FakePlayerAI(fakePlayer));
            }

            fakePlayer.broadcastUserInfo();
            ph.setFake(true);
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        String event = st.nextToken();
        byte var7 = -1;
        switch (event.hashCode()) {
            case 1764164512:
                if (event.equals("deleteFake")) {
                    var7 = 1;
                }
                break;
            case 1774618269:
                if (event.equals("allFakes")) {
                    var7 = 0;
                }
        }

        switch (var7) {
            case 0:
                this.getAllFakePlayer(player, Integer.parseInt(st.nextToken()));
                break;
            case 1:
                int objId = Integer.parseInt(st.nextToken());
                int page = Integer.parseInt(st.nextToken());
                Player fake = World.getInstance().getPlayer(objId);
                if (fake != null) {
                    fake.deleteMe();
                }

                this.removeValueDB(objId, "fakePlayer");
                GameClient.deleteCharByObjId(objId);
                PlayerData.get(objId).setFake(false);
                this.getAllFakePlayer(player, page);
        }

    }

    public boolean onAdminCommand(Player player, String chat) {
        StringTokenizer st = new StringTokenizer(chat, " ");
        String var4 = st.nextToken();
        byte var5 = -1;
        switch (var4.hashCode()) {
            case -708504447:
                if (var4.equals("createRndFake")) {
                    var5 = 2;
                }
                break;
            case 323383276:
                if (var4.equals("formatFake")) {
                    var5 = 1;
                }
                break;
            case 1774618269:
                if (var4.equals("allFakes")) {
                    var5 = 0;
                }
        }

        switch (var5) {
            case 0:
                if (player.getTarget() != player) {
                    player.setTarget(player);
                }

                this.getAllFakePlayer(player, 1);
                return true;
            case 1:
                player.sendMessage("//createRndFake count Lvl pvp pk clan(1=true 0=false) template(0 118)");
                return true;
            case 2:
                if (player.getTarget() != player) {
                    player.setTarget(player);
                }

                int count = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                int lvl = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : FAKE_MAX_LEVEL;
                int pvp = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : FAKE_MAX_PVP;
                int pk = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : FAKE_MAX_PK;
                int clan = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : (Rnd.get(100) < FAKE_CHANCE_HAS_CLAN ? 1 : 0);
                int templateId = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : -1;

                try {
                    for (int i = 0; i < count; ++i) {
                        int rndTemplateId = templateId;
                        if (templateId < 0) {
                            if (Rnd.nextBoolean()) {
                                rndTemplateId = Rnd.get(0, 57);
                            } else {
                                rndTemplateId = Rnd.get(88, 118);
                            }
                        }

                        Player fakePlayer = createPlayer(player, SPAWN_RANGE, rndTemplateId, lvl);
                        fakePlayer = UtilPlayer.spawnPlayer(fakePlayer.getObjectId());
                        fakePlayer.getAvailableAutoGetSkills();
                        fakePlayer.setPkKills(pvp > 0 ? Rnd.get(pvp) : 0);
                        fakePlayer.setPvpKills(pk > 0 ? Rnd.get(pk) : 0);
                        String action = "stand";
                        if (Rnd.get(100) < FAKE_CHANCE_SIT && player.isInsideZone(ZoneId.PEACE)) {
                            fakePlayer.sitDown();
                            action = "sitDown";
                        } else {
                            PlayerData.get(fakePlayer).setPosToFarm(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ());
                            fakePlayer.detachAI();
                            fakePlayer.setAI(new FakePlayerAI(fakePlayer));
                        }

                        if (clan > 0 && this.defineClan(fakePlayer)) {
                        }

                        if (fakePlayer.getClanId() != 0) {
                            fakePlayer.setTitle(FakePlayerData.getTitle());
                        }

                        UtilPlayer.storeCharBase(fakePlayer);
                        PlayerData.get(fakePlayer).setFake(true);
                        this.setValueDB(fakePlayer, "fakePlayer", action);
                        int var10003 = fakePlayer.getX();
                        this.setValueDB(fakePlayer, "posToFarm", var10003 + "," + fakePlayer.getY() + "," + fakePlayer.getZ());
                        fakePlayer.broadcastUserInfo();
                        fakePlayer.broadcastPacket(new TitleUpdate(fakePlayer));
                        Thread.sleep(100L);
                    }
                } catch (Exception var16) {
                    player.sendMessage("format: //createRndFake count Lvl pvp pk clan(1=true 0=false) template(0 118)");
                    var16.printStackTrace();
                }

                return true;
            default:
                return false;
        }
    }

    public void getAllFakePlayer(Player player, int page) {
        if (player.getTarget() != player) {
            player.setTarget(player);
        }

        HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append("<br>");
        hb.append(Html.headHtml("All FAKE Players"));
        hb.append("<br>");
        hb.append("<table>");
        hb.append("<tr>");
        hb.append("<td width=200><font color=LEVEL>Player:</font></td>");
        hb.append("<td width=64><font color=LEVEL>Action:</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        int MAX_PER_PAGE = 12;
        int searchPage = MAX_PER_PAGE * (page - 1);
        int count = 0;
        int countFakes = 0;
        Iterator var8 = PlayerData.getAllPlayers().iterator();

        while (var8.hasNext()) {
            PlayerHolder ph = (PlayerHolder) var8.next();
            if (ph.isFake()) {
                ++countFakes;
                if (count < searchPage) {
                    ++count;
                } else if (count < searchPage + MAX_PER_PAGE) {
                    hb.append("<table", count % 2 == 0 ? " bgcolor=000000>" : ">");
                    hb.append("<tr>");
                    hb.append("<td width=200>", ph.getName(), "</td><td width=64><a action=\"bypass -h Engine FakePlayer deleteFake ", ph.getObjectId(), " ", page, "\">DELETE</a></td>");
                    hb.append("</tr>");
                    hb.append("</table>");
                    ++count;
                }
            }
        }

        hb.append("<center>");
        hb.append("<img src=", "L2UI.SquareGray", " width=264 height=1>");
        hb.append("<table bgcolor=CC99FF>");
        hb.append("<tr>");
        int currentPage = 1;

        for (int i = 0; i < countFakes; ++i) {
            if (i % MAX_PER_PAGE == 0) {
                hb.append("<td width=18 align=center><a action=\"bypass -h Engine FakePlayer allFakes ", currentPage, "\">", currentPage, "</a></td>");
                ++currentPage;
            }
        }

        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=", "L2UI.SquareGray", " width=264 height=1>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private boolean defineClan(Player player) {
        try {
            String clanName = FakePlayerData.getClanName();
            Clan clan = ClanTable.getInstance().getClanByName(clanName);
            if (clan == null) {
                clan = new Clan(IdFactory.getInstance().getNextId(), clanName);
                clan.setLevel(Rnd.get(3, 5));
                ClanMember leader = new ClanMember(clan, player);
                clan.setLeader(leader);
                leader.setPlayerInstance(player);
                storeClan(clan);
                player.setClan(clan);
                player.setPledgeClass(ClanMember.calculatePledgeClass(player));
                player.setClanPrivileges(8388606);
                ClanTable.getInstance().addNewClan(clan);
                ++_crestId;
                clan.changeClanCrest(_crestId);
            } else {
                clan.addClanMember(player);
                player.setClan(clan);
                player.setPledgeClass(ClanMember.calculatePledgeClass(player));
                player.setClanPrivileges(clan.getPriviledgesByRank(player.getPowerGrade()));
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return true;
    }

    private static class SingletonHolder {
        protected static final FakePlayer INSTANCE = new FakePlayer();
    }
}