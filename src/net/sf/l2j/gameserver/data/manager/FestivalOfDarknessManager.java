/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.FestivalType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.FestivalMonster;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.type.PeaceZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class FestivalOfDarknessManager {
    public static final long FESTIVAL_SIGNUP_TIME;
    public static final int FESTIVAL_COUNT = 5;
    public static final int FESTIVAL_OFFERING_ID = 5901;
    public static final int FESTIVAL_OFFERING_VALUE = 5;
    public static final int[][] FESTIVAL_DAWN_PLAYER_SPAWNS;
    public static final int[][] FESTIVAL_DUSK_PLAYER_SPAWNS;
    protected static final int[][] FESTIVAL_DAWN_WITCH_SPAWNS;
    protected static final int[][] FESTIVAL_DUSK_WITCH_SPAWNS;
    protected static final int[][][] FESTIVAL_DAWN_PRIMARY_SPAWNS;
    protected static final int[][][] FESTIVAL_DUSK_PRIMARY_SPAWNS;
    protected static final int[][][] FESTIVAL_DAWN_SECONDARY_SPAWNS;
    protected static final int[][][] FESTIVAL_DUSK_SECONDARY_SPAWNS;
    protected static final int[][][] FESTIVAL_DAWN_CHEST_SPAWNS;
    protected static final int[][][] FESTIVAL_DUSK_CHEST_SPAWNS;
    private static final CLogger LOGGER = new CLogger(FestivalOfDarknessManager.class.getName());
    private static final String RESTORE_FESTIVAL = "SELECT festivalId, cabal, cycle, date, score, members FROM seven_signs_festival";
    private static final String RESTORE_FESTIVAL_2 = "SELECT festival_cycle, accumulated_bonus0, accumulated_bonus1, accumulated_bonus2, accumulated_bonus3, accumulated_bonus4 FROM seven_signs_status WHERE id=0";
    private static final String UPDATE = "UPDATE seven_signs_festival SET date=?, score=?, members=? WHERE cycle=? AND cabal=? AND festivalId=?";
    private static final String INSERT = "INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members) VALUES (?,?,?,?,?,?)";
    private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";
    private static final int FESTIVAL_MAX_OFFSET_X = 230;
    private static final int FESTIVAL_MAX_OFFSET_Y = 230;
    private static final int FESTIVAL_DEFAULT_RESPAWN = 60;

    static {
        FESTIVAL_SIGNUP_TIME = Config.ALT_FESTIVAL_CYCLE_LENGTH - Config.ALT_FESTIVAL_LENGTH - 60000L;
        FESTIVAL_DAWN_PLAYER_SPAWNS = new int[][]{{-79187, 113186, -4895, 0}, {-75918, 110137, -4895, 0}, {-73835, 111969, -4895, 0}, {-76170, 113804, -4895, 0}, {-78927, 109528, -4895, 0}};
        FESTIVAL_DUSK_PLAYER_SPAWNS = new int[][]{{-77200, 88966, -5151, 0}, {-76941, 85307, -5151, 0}, {-74855, 87135, -5151, 0}, {-80208, 88222, -5151, 0}, {-79954, 84697, -5151, 0}};
        FESTIVAL_DAWN_WITCH_SPAWNS = new int[][]{{-79183, 113052, -4891, 0, 31132}, {-75916, 110270, -4891, 0, 31133}, {-73979, 111970, -4891, 0, 31134}, {-76174, 113663, -4891, 0, 31135}, {-78930, 109664, -4891, 0, 31136}};
        FESTIVAL_DUSK_WITCH_SPAWNS = new int[][]{{-77199, 88830, -5147, 0, 31142}, {-76942, 85438, -5147, 0, 31143}, {-74990, 87135, -5147, 0, 31144}, {-80207, 88222, -5147, 0, 31145}, {-79952, 84833, -5147, 0, 31146}};
        FESTIVAL_DAWN_PRIMARY_SPAWNS = new int[][][]{{{-78537, 113839, -4895, -1, 18009}, {-78466, 113852, -4895, -1, 18010}, {-78509, 113899, -4895, -1, 18010}, {-78481, 112557, -4895, -1, 18009}, {-78559, 112504, -4895, -1, 18010}, {-78489, 112494, -4895, -1, 18010}, {-79803, 112543, -4895, -1, 18012}, {-79854, 112492, -4895, -1, 18013}, {-79886, 112557, -4895, -1, 18014}, {-79821, 113811, -4895, -1, 18015}, {-79857, 113896, -4895, -1, 18017}, {-79878, 113816, -4895, -1, 18018}, {-79190, 113660, -4895, -1, 18011}, {-78710, 113188, -4895, -1, 18011}, {-79190, 112730, -4895, -1, 18016}, {-79656, 113188, -4895, -1, 18016}}, {{-76558, 110784, -4895, -1, 18019}, {-76607, 110815, -4895, -1, 18020}, {-76559, 110820, -4895, -1, 18020}, {-75277, 110792, -4895, -1, 18019}, {-75225, 110801, -4895, -1, 18020}, {-75262, 110832, -4895, -1, 18020}, {-75249, 109441, -4895, -1, 18022}, {-75278, 109495, -4895, -1, 18023}, {-75223, 109489, -4895, -1, 18024}, {-76556, 109490, -4895, -1, 18025}, {-76607, 109469, -4895, -1, 18027}, {-76561, 109450, -4895, -1, 18028}, {-76399, 110144, -4895, -1, 18021}, {-75912, 110606, -4895, -1, 18021}, {-75444, 110144, -4895, -1, 18026}, {-75930, 109665, -4895, -1, 18026}}, {{-73184, 111319, -4895, -1, 18029}, {-73135, 111294, -4895, -1, 18030}, {-73185, 111281, -4895, -1, 18030}, {-74477, 111321, -4895, -1, 18029}, {-74523, 111293, -4895, -1, 18030}, {-74481, 111280, -4895, -1, 18030}, {-74489, 112604, -4895, -1, 18032}, {-74491, 112660, -4895, -1, 18033}, {-74527, 112629, -4895, -1, 18034}, {-73197, 112621, -4895, -1, 18035}, {-73142, 112631, -4895, -1, 18037}, {-73182, 112656, -4895, -1, 18038}, {-73834, 112430, -4895, -1, 18031}, {-74299, 111959, -4895, -1, 18031}, {-73841, 111491, -4895, -1, 18036}, {-73363, 111959, -4895, -1, 18036}}, {{-75543, 114461, -4895, -1, 18039}, {-75514, 114493, -4895, -1, 18040}, {-75488, 114456, -4895, -1, 18040}, {-75521, 113158, -4895, -1, 18039}, {-75504, 113110, -4895, -1, 18040}, {-75489, 113142, -4895, -1, 18040}, {-76809, 113143, -4895, -1, 18042}, {-76860, 113138, -4895, -1, 18043}, {-76831, 113112, -4895, -1, 18044}, {-76831, 114441, -4895, -1, 18045}, {-76840, 114490, -4895, -1, 18047}, {-76864, 114455, -4895, -1, 18048}, {-75703, 113797, -4895, -1, 18041}, {-76180, 114263, -4895, -1, 18041}, {-76639, 113797, -4895, -1, 18046}, {-76180, 113337, -4895, -1, 18046}}, {{-79576, 108881, -4895, -1, 18049}, {-79592, 108835, -4895, -1, 18050}, {-79614, 108871, -4895, -1, 18050}, {-79586, 110171, -4895, -1, 18049}, {-79589, 110216, -4895, -1, 18050}, {-79620, 110177, -4895, -1, 18050}, {-78825, 110182, -4895, -1, 18052}, {-78238, 110182, -4895, -1, 18053}, {-78266, 110218, -4895, -1, 18054}, {-78275, 108883, -4895, -1, 18055}, {-78267, 108839, -4895, -1, 18057}, {-78241, 108871, -4895, -1, 18058}, {-79394, 109538, -4895, -1, 18051}, {-78929, 109992, -4895, -1, 18051}, {-78454, 109538, -4895, -1, 18056}, {-78929, 109053, -4895, -1, 18056}}};
        FESTIVAL_DUSK_PRIMARY_SPAWNS = new int[][][]{{{-76542, 89653, -5151, -1, 18009}, {-76509, 89637, -5151, -1, 18010}, {-76548, 89614, -5151, -1, 18010}, {-76539, 88326, -5151, -1, 18009}, {-76512, 88289, -5151, -1, 18010}, {-76546, 88287, -5151, -1, 18010}, {-77879, 88308, -5151, -1, 18012}, {-77886, 88310, -5151, -1, 18013}, {-77879, 88278, -5151, -1, 18014}, {-77857, 89605, -5151, -1, 18015}, {-77858, 89658, -5151, -1, 18017}, {-77891, 89633, -5151, -1, 18018}, {-76728, 88962, -5151, -1, 18011}, {-77194, 88494, -5151, -1, 18011}, {-77660, 88896, -5151, -1, 18016}, {-77195, 89438, -5151, -1, 18016}}, {{-77585, 84650, -5151, -1, 18019}, {-77628, 84643, -5151, -1, 18020}, {-77607, 84613, -5151, -1, 18020}, {-76603, 85946, -5151, -1, 18019}, {-77606, 85994, -5151, -1, 18020}, {-77638, 85959, -5151, -1, 18020}, {-76301, 85960, -5151, -1, 18022}, {-76257, 85972, -5151, -1, 18023}, {-76286, 85992, -5151, -1, 18024}, {-76281, 84667, -5151, -1, 18025}, {-76291, 84611, -5151, -1, 18027}, {-76257, 84616, -5151, -1, 18028}, {-77419, 85307, -5151, -1, 18021}, {-76952, 85768, -5151, -1, 18021}, {-76477, 85312, -5151, -1, 18026}, {-76942, 84832, -5151, -1, 18026}}, {{-74211, 86494, -5151, -1, 18029}, {-74200, 86449, -5151, -1, 18030}, {-74167, 86464, -5151, -1, 18030}, {-75495, 86482, -5151, -1, 18029}, {-75540, 86473, -5151, -1, 18030}, {-75509, 86445, -5151, -1, 18030}, {-75509, 87775, -5151, -1, 18032}, {-75518, 87826, -5151, -1, 18033}, {-75542, 87780, -5151, -1, 18034}, {-74214, 87789, -5151, -1, 18035}, {-74169, 87801, -5151, -1, 18037}, {-74198, 87827, -5151, -1, 18038}, {-75324, 87135, -5151, -1, 18031}, {-74852, 87606, -5151, -1, 18031}, {-74388, 87146, -5151, -1, 18036}, {-74856, 86663, -5151, -1, 18036}}, {{-79560, 89007, -5151, -1, 18039}, {-79521, 89016, -5151, -1, 18040}, {-79544, 89047, -5151, -1, 18040}, {-79552, 87717, -5151, -1, 18039}, {-79552, 87673, -5151, -1, 18040}, {-79510, 87702, -5151, -1, 18040}, {-80866, 87719, -5151, -1, 18042}, {-80897, 87689, -5151, -1, 18043}, {-80850, 87685, -5151, -1, 18044}, {-80848, 89013, -5151, -1, 18045}, {-80887, 89051, -5151, -1, 18047}, {-80891, 89004, -5151, -1, 18048}, {-80205, 87895, -5151, -1, 18041}, {-80674, 88350, -5151, -1, 18041}, {-80209, 88833, -5151, -1, 18046}, {-79743, 88364, -5151, -1, 18046}}, {{-80624, 84060, -5151, -1, 18049}, {-80621, 84007, -5151, -1, 18050}, {-80590, 84039, -5151, -1, 18050}, {-80605, 85349, -5151, -1, 18049}, {-80639, 85363, -5151, -1, 18050}, {-80611, 85385, -5151, -1, 18050}, {-79311, 85353, -5151, -1, 18052}, {-79277, 85384, -5151, -1, 18053}, {-79273, 85539, -5151, -1, 18054}, {-79297, 84054, -5151, -1, 18055}, {-79285, 84006, -5151, -1, 18057}, {-79260, 84040, -5151, -1, 18058}, {-79945, 85171, -5151, -1, 18051}, {-79489, 84707, -5151, -1, 18051}, {-79952, 84222, -5151, -1, 18056}, {-80423, 84703, -5151, -1, 18056}}};
        FESTIVAL_DAWN_SECONDARY_SPAWNS = new int[][][]{{{-78757, 112834, -4895, -1, 18016}, {-78581, 112834, -4895, -1, 18016}, {-78822, 112526, -4895, -1, 18011}, {-78822, 113702, -4895, -1, 18011}, {-78822, 113874, -4895, -1, 18011}, {-79524, 113546, -4895, -1, 18011}, {-79693, 113546, -4895, -1, 18011}, {-79858, 113546, -4895, -1, 18011}, {-79545, 112757, -4895, -1, 18016}, {-79545, 112586, -4895, -1, 18016}}, {{-75565, 110580, -4895, -1, 18026}, {-75565, 110740, -4895, -1, 18026}, {-75577, 109776, -4895, -1, 18021}, {-75413, 109776, -4895, -1, 18021}, {-75237, 109776, -4895, -1, 18021}, {-76274, 109468, -4895, -1, 18021}, {-76274, 109635, -4895, -1, 18021}, {-76274, 109795, -4895, -1, 18021}, {-76351, 110500, -4895, -1, 18056}, {-76528, 110500, -4895, -1, 18056}}, {{-74191, 111527, -4895, -1, 18036}, {-74191, 111362, -4895, -1, 18036}, {-73495, 111611, -4895, -1, 18031}, {-73327, 111611, -4895, -1, 18031}, {-73154, 111611, -4895, -1, 18031}, {-73473, 112301, -4895, -1, 18031}, {-73473, 112475, -4895, -1, 18031}, {-73473, 112649, -4895, -1, 18031}, {-74270, 112326, -4895, -1, 18036}, {-74443, 112326, -4895, -1, 18036}}, {{-75738, 113439, -4895, -1, 18046}, {-75571, 113439, -4895, -1, 18046}, {-75824, 114141, -4895, -1, 18041}, {-75824, 114309, -4895, -1, 18041}, {-75824, 114477, -4895, -1, 18041}, {-76513, 114158, -4895, -1, 18041}, {-76683, 114158, -4895, -1, 18041}, {-76857, 114158, -4895, -1, 18041}, {-76535, 113357, -4895, -1, 18056}, {-76535, 113190, -4895, -1, 18056}}, {{-79350, 109894, -4895, -1, 18056}, {-79534, 109894, -4895, -1, 18056}, {-79285, 109187, -4895, -1, 18051}, {-79285, 109019, -4895, -1, 18051}, {-79285, 108860, -4895, -1, 18051}, {-78587, 109172, -4895, -1, 18051}, {-78415, 109172, -4895, -1, 18051}, {-78249, 109172, -4895, -1, 18051}, {-78575, 109961, -4895, -1, 18056}, {-78575, 110130, -4895, -1, 18056}}};
        FESTIVAL_DUSK_SECONDARY_SPAWNS = new int[][][]{{{-76844, 89304, -5151, -1, 18011}, {-76844, 89479, -5151, -1, 18011}, {-76844, 89649, -5151, -1, 18011}, {-77544, 89326, -5151, -1, 18011}, {-77716, 89326, -5151, -1, 18011}, {-77881, 89326, -5151, -1, 18011}, {-77561, 88530, -5151, -1, 18016}, {-77561, 88364, -5151, -1, 18016}, {-76762, 88615, -5151, -1, 18016}, {-76594, 88615, -5151, -1, 18016}}, {{-77307, 84969, -5151, -1, 18021}, {-77307, 84795, -5151, -1, 18021}, {-77307, 84623, -5151, -1, 18021}, {-76614, 84944, -5151, -1, 18021}, {-76433, 84944, -5151, -1, 18021}, {-7627, 84944, -5151, -1, 18021}, {-76594, 85745, -5151, -1, 18026}, {-76594, 85910, -5151, -1, 18026}, {-77384, 85660, -5151, -1, 18026}, {-77555, 85660, -5151, -1, 18026}}, {{-74517, 86782, -5151, -1, 18031}, {-74344, 86782, -5151, -1, 18031}, {-74185, 86782, -5151, -1, 18031}, {-74496, 87464, -5151, -1, 18031}, {-74496, 87636, -5151, -1, 18031}, {-74496, 87815, -5151, -1, 18031}, {-75298, 87497, -5151, -1, 18036}, {-75460, 87497, -5151, -1, 18036}, {-75219, 86712, -5151, -1, 18036}, {-75219, 86531, -5151, -1, 18036}}, {{-79851, 88703, -5151, -1, 18041}, {-79851, 88868, -5151, -1, 18041}, {-79851, 89040, -5151, -1, 18041}, {-80548, 88722, -5151, -1, 18041}, {-80711, 88722, -5151, -1, 18041}, {-80883, 88722, -5151, -1, 18041}, {-80565, 87916, -5151, -1, 18046}, {-80565, 87752, -5151, -1, 18046}, {-79779, 87996, -5151, -1, 18046}, {-79613, 87996, -5151, -1, 18046}}, {{-79271, 84330, -5151, -1, 18051}, {-79448, 84330, -5151, -1, 18051}, {-79601, 84330, -5151, -1, 18051}, {-80311, 84367, -5151, -1, 18051}, {-80311, 84196, -5151, -1, 18051}, {-80311, 84015, -5151, -1, 18051}, {-80556, 85049, -5151, -1, 18056}, {-80384, 85049, -5151, -1, 18056}, {-79598, 85127, -5151, -1, 18056}, {-79598, 85303, -5151, -1, 18056}}};
        FESTIVAL_DAWN_CHEST_SPAWNS = new int[][][]{{{-78999, 112957, -4927, -1, 18109}, {-79153, 112873, -4927, -1, 18109}, {-79256, 112873, -4927, -1, 18109}, {-79368, 112957, -4927, -1, 18109}, {-79481, 113124, -4927, -1, 18109}, {-79481, 113275, -4927, -1, 18109}, {-79364, 113398, -4927, -1, 18109}, {-79213, 113500, -4927, -1, 18109}, {-79099, 113500, -4927, -1, 18109}, {-78960, 113398, -4927, -1, 18109}, {-78882, 113235, -4927, -1, 18109}, {-78882, 113099, -4927, -1, 18109}}, {{-76119, 110383, -4927, -1, 18110}, {-75980, 110442, -4927, -1, 18110}, {-75848, 110442, -4927, -1, 18110}, {-75720, 110383, -4927, -1, 18110}, {-75625, 110195, -4927, -1, 18110}, {-75625, 110063, -4927, -1, 18110}, {-75722, 109908, -4927, -1, 18110}, {-75863, 109832, -4927, -1, 18110}, {-75989, 109832, -4927, -1, 18110}, {-76130, 109908, -4927, -1, 18110}, {-76230, 110079, -4927, -1, 18110}, {-76230, 110215, -4927, -1, 18110}}, {{-74055, 111781, -4927, -1, 18111}, {-74144, 111938, -4927, -1, 18111}, {-74144, 112075, -4927, -1, 18111}, {-74055, 112173, -4927, -1, 18111}, {-73885, 112289, -4927, -1, 18111}, {-73756, 112289, -4927, -1, 18111}, {-73574, 112141, -4927, -1, 18111}, {-73511, 112040, -4927, -1, 18111}, {-73511, 111912, -4927, -1, 18111}, {-73574, 111772, -4927, -1, 18111}, {-73767, 111669, -4927, -1, 18111}, {-73899, 111669, -4927, -1, 18111}}, {{-76008, 113566, -4927, -1, 18112}, {-76159, 113485, -4927, -1, 18112}, {-76267, 113485, -4927, -1, 18112}, {-76386, 113566, -4927, -1, 18112}, {-76482, 113748, -4927, -1, 18112}, {-76482, 113885, -4927, -1, 18112}, {-76371, 114029, -4927, -1, 18112}, {-76220, 114118, -4927, -1, 18112}, {-76092, 114118, -4927, -1, 18112}, {-75975, 114029, -4927, -1, 18112}, {-75861, 11384, -4927, -1, 18112}, {-75861, 113713, -4927, -1, 18112}}, {{-79100, 109782, -4927, -1, 18113}, {-78962, 109853, -4927, -1, 18113}, {-78851, 109853, -4927, -1, 18113}, {-78721, 109782, -4927, -1, 18113}, {-78615, 109596, -4927, -1, 18113}, {-78615, 109453, -4927, -1, 18113}, {-78746, 109300, -4927, -1, 18113}, {-78881, 109203, -4927, -1, 18113}, {-79027, 109203, -4927, -1, 18113}, {-79159, 109300, -4927, -1, 18113}, {-79240, 109480, -4927, -1, 18113}, {-79240, 109615, -4927, -1, 18113}}};
        FESTIVAL_DUSK_CHEST_SPAWNS = new int[][][]{{{-77016, 88726, -5183, -1, 18114}, {-77136, 88646, -5183, -1, 18114}, {-77247, 88646, -5183, -1, 18114}, {-77380, 88726, -5183, -1, 18114}, {-77512, 88883, -5183, -1, 18114}, {-77512, 89053, -5183, -1, 18114}, {-77378, 89287, -5183, -1, 18114}, {-77254, 89238, -5183, -1, 18114}, {-77095, 89238, -5183, -1, 18114}, {-76996, 89287, -5183, -1, 18114}, {-76901, 89025, -5183, -1, 18114}, {-76901, 88891, -5183, -1, 18114}}, {{-77128, 85553, -5183, -1, 18115}, {-77036, 85594, -5183, -1, 18115}, {-76919, 85594, -5183, -1, 18115}, {-76755, 85553, -5183, -1, 18115}, {-76635, 85392, -5183, -1, 18115}, {-76635, 85216, -5183, -1, 18115}, {-76761, 85025, -5183, -1, 18115}, {-76908, 85004, -5183, -1, 18115}, {-77041, 85004, -5183, -1, 18115}, {-77138, 85025, -5183, -1, 18115}, {-77268, 85219, -5183, -1, 18115}, {-77268, 85410, -5183, -1, 18115}}, {{-75150, 87303, -5183, -1, 18116}, {-75150, 87175, -5183, -1, 18116}, {-75150, 87175, -5183, -1, 18116}, {-75150, 87303, -5183, -1, 18116}, {-74943, 87433, -5183, -1, 18116}, {-74767, 87433, -5183, -1, 18116}, {-74556, 87306, -5183, -1, 18116}, {-74556, 87184, -5183, -1, 18116}, {-74556, 87184, -5183, -1, 18116}, {-74556, 87306, -5183, -1, 18116}, {-74757, 86830, -5183, -1, 18116}, {-74927, 86830, -5183, -1, 18116}}, {{-80010, 88128, -5183, -1, 18117}, {-80113, 88066, -5183, -1, 18117}, {-80220, 88066, -5183, -1, 18117}, {-80359, 88128, -5183, -1, 18117}, {-80467, 88267, -5183, -1, 18117}, {-80467, 88436, -5183, -1, 18117}, {-80381, 88639, -5183, -1, 18117}, {-80278, 88577, -5183, -1, 18117}, {-80142, 88577, -5183, -1, 18117}, {-80028, 88639, -5183, -1, 18117}, {-79915, 88466, -5183, -1, 18117}, {-79915, 88322, -5183, -1, 18117}}, {{-80153, 84947, -5183, -1, 18118}, {-80003, 84962, -5183, -1, 18118}, {-79848, 84962, -5183, -1, 18118}, {-79742, 84947, -5183, -1, 18118}, {-79668, 84772, -5183, -1, 18118}, {-79668, 84619, -5183, -1, 18118}, {-79772, 84471, -5183, -1, 18118}, {-79888, 84414, -5183, -1, 18118}, {-80023, 84414, -5183, -1, 18118}, {-80166, 84471, -5183, -1, 18118}, {-80253, 84600, -5183, -1, 18118}, {-80253, 84780, -5183, -1, 18118}}};
    }

    protected final List<Integer> _accumulatedBonuses = new ArrayList<>();
    protected final Map<Integer, List<Integer>> _dawnFestivalParticipants = new HashMap<>();
    protected final Map<Integer, List<Integer>> _duskFestivalParticipants = new HashMap<>();
    protected final Map<Integer, List<Integer>> _dawnPreviousParticipants = new HashMap<>();
    protected final Map<Integer, List<Integer>> _duskPreviousParticipants = new HashMap<>();
    private final Map<Integer, Integer> _dawnFestivalScores = new HashMap<>();
    private final Map<Integer, Integer> _duskFestivalScores = new HashMap<>();
    private final Map<Integer, Map<Integer, StatSet>> _festivalData = new HashMap<>();
    protected FestivalOfDarknessManager.FestivalManager _managerInstance;
    protected ScheduledFuture<?> _managerScheduledTask;
    protected int _signsCycle = SevenSignsManager.getInstance().getCurrentCycle();
    protected int _festivalCycle;
    protected long _nextFestivalCycleStart;
    protected long _nextFestivalStart;
    protected boolean _festivalInitialized;
    protected boolean _festivalInProgress;
    boolean _noPartyRegister;
    private List<PeaceZone> _dawnPeace;
    private List<PeaceZone> _duskPeace;

    protected FestivalOfDarknessManager() {
    }

    protected static boolean isFestivalArcher(int npcId) {
        if (npcId >= 18009 && npcId <= 18108) {
            int identifier = npcId % 10;
            return identifier == 4 || identifier == 9;
        } else {
            return false;
        }
    }

    protected static boolean isFestivalChest(int npcId) {
        return npcId < 18109 || npcId > 18118;
    }

    private static void addReputationPointsForPartyMemberClan(String playerName) {
        Player player = World.getInstance().getPlayer(playerName);
        if (player != null) {
            if (player.getClan() != null) {
                player.getClan().addReputationScore(100);
                player.getClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION).addString(playerName).addNumber(100));
            }
        } else {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)");

                    try {
                        ps.setString(1, playerName);
                        ResultSet rs = ps.executeQuery();

                        try {
                            if (rs.next()) {
                                String clanName = rs.getString("clan_name");
                                if (clanName != null) {
                                    Clan clan = ClanTable.getInstance().getClanByName(clanName);
                                    if (clan != null) {
                                        clan.addReputationScore(100);
                                        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION).addString(playerName).addNumber(100));
                                    }
                                }
                            }
                        } catch (Throwable var10) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var9) {
                                    var10.addSuppressed(var9);
                                }
                            }

                            throw var10;
                        }

                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Throwable var11) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var8) {
                                var11.addSuppressed(var8);
                            }
                        }

                        throw var11;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var12) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var7) {
                            var12.addSuppressed(var7);
                        }
                    }

                    throw var12;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var13) {
                LOGGER.error("Couldn't get clan name of {}.", var13, playerName);
            }
        }

    }

    public static FestivalOfDarknessManager getInstance() {
        return FestivalOfDarknessManager.SingletonHolder.INSTANCE;
    }

    public void init() {

        this.restoreFestivalData();
        if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
            LOGGER.info("Seven Signs Festival initialization was bypassed due to Seal Validation being under effect.");
        } else {
            this.startFestivalManager();
        }
    }

    public final ScheduledFuture<?> getFestivalManagerSchedule() {
        if (this._managerScheduledTask == null) {
            this.startFestivalManager();
        }

        return this._managerScheduledTask;
    }

    public void startFestivalManager() {
        FestivalOfDarknessManager.FestivalManager fm = new FestivalOfDarknessManager.FestivalManager();
        this.setNextFestivalStart(Config.ALT_FESTIVAL_MANAGER_START + FESTIVAL_SIGNUP_TIME);
        this._managerScheduledTask = ThreadPool.scheduleAtFixedRate(fm, Config.ALT_FESTIVAL_MANAGER_START, Config.ALT_FESTIVAL_CYCLE_LENGTH);
        LOGGER.info("The first Festival of Darkness cycle begins in {} minute(s).", Config.ALT_FESTIVAL_MANAGER_START / 60000L);
    }

    protected void restoreFestivalData() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT festivalId, cabal, cycle, date, score, members FROM seven_signs_festival");

                ResultSet rs;
                int i;
                try {
                    rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            i = rs.getInt("cycle");
                            int festivalId = rs.getInt("festivalId");
                            String cabal = rs.getString("cabal");
                            StatSet set = new StatSet();
                            set.set("festivalId", festivalId);
                            set.set("cabal", Enum.valueOf(CabalType.class, cabal));
                            set.set("cycle", i);
                            set.set("date", rs.getString("date"));
                            set.set("score", rs.getInt("score"));
                            set.set("members", rs.getString("members"));
                            if (cabal.equalsIgnoreCase("dawn")) {
                                festivalId += 5;
                            }

                            Map<Integer, StatSet> map = this._festivalData.get(i);
                            if (map == null) {
                                map = new HashMap<>();
                            }

                            map.put(festivalId, set);
                            this._festivalData.put(i, map);
                        }
                    } catch (Throwable var16) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var13) {
                                var16.addSuppressed(var13);
                            }
                        }

                        throw var16;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var17) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var12) {
                            var17.addSuppressed(var12);
                        }
                    }

                    throw var17;
                }

                if (ps != null) {
                    ps.close();
                }

                ps = con.prepareStatement("SELECT festival_cycle, accumulated_bonus0, accumulated_bonus1, accumulated_bonus2, accumulated_bonus3, accumulated_bonus4 FROM seven_signs_status WHERE id=0");

                try {
                    rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this._festivalCycle = rs.getInt("festival_cycle");

                            for (i = 0; i < 5; ++i) {
                                this._accumulatedBonuses.add(i, rs.getInt("accumulated_bonus" + i));
                            }
                        }
                    } catch (Throwable var14) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var11) {
                                var14.addSuppressed(var11);
                            }
                        }

                        throw var14;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var15) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var10) {
                            var15.addSuppressed(var10);
                        }
                    }

                    throw var15;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var18) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var9) {
                        var18.addSuppressed(var9);
                    }
                }

                throw var18;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var19) {
            LOGGER.error("Couldn't load Seven Signs Festival data.", var19);
        }

    }

    public void saveFestivalData(boolean updateSettings) {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE seven_signs_festival SET date=?, score=?, members=? WHERE cycle=? AND cabal=? AND festivalId=?");

                try {
                    PreparedStatement ps2 = con.prepareStatement("INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members) VALUES (?,?,?,?,?,?)");

                    try {

                        for (Map<Integer, StatSet> map : this._festivalData.values()) {
                            for (StatSet set : map.values()) {
                                int festivalCycle = set.getInteger("cycle");
                                int festivalId = set.getInteger("festivalId");
                                String cabal = set.getString("cabal");
                                ps.setLong(1, Long.parseLong(set.getString("date")));
                                ps.setInt(2, set.getInteger("score"));
                                ps.setString(3, set.getString("members"));
                                ps.setInt(4, festivalCycle);
                                ps.setString(5, cabal);
                                ps.setInt(6, festivalId);
                                if (ps.executeUpdate() <= 0) {
                                    ps2.setInt(1, festivalId);
                                    ps2.setString(2, cabal);
                                    ps2.setInt(3, festivalCycle);
                                    ps2.setLong(4, Long.parseLong(set.getString("date")));
                                    ps2.setInt(5, set.getInteger("score"));
                                    ps2.setString(6, set.getString("members"));
                                    ps2.execute();
                                    ps2.clearParameters();
                                }
                            }
                        }

                        if (updateSettings) {
                            SevenSignsManager.getInstance().saveSevenSignsStatus();
                        }
                    } catch (Throwable var15) {
                        if (ps2 != null) {
                            try {
                                ps2.close();
                            } catch (Throwable var14) {
                                var15.addSuppressed(var14);
                            }
                        }

                        throw var15;
                    }

                    if (ps2 != null) {
                        ps2.close();
                    }
                } catch (Throwable var16) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var13) {
                            var16.addSuppressed(var13);
                        }
                    }

                    throw var16;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var17) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var12) {
                        var17.addSuppressed(var12);
                    }
                }

                throw var17;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var18) {
            LOGGER.error("Couldn't save Seven Signs Festival data.", var18);
        }

    }

    public void rewardHighestRanked() {
        for (int i = 0; i < 5; ++i) {
            StatSet set = this.getOverallHighestScoreData(i);
            if (set != null) {
                String[] var3 = set.getString("members").split(",");
                int var4 = var3.length;

                for (String playerName : var3) {
                    addReputationPointsForPartyMemberClan(playerName);
                }
            }
        }

    }

    public void resetFestivalData(boolean updateSettings) {
        this._festivalCycle = 0;
        this._signsCycle = SevenSignsManager.getInstance().getCurrentCycle();

        for (int i = 0; i < 5; ++i) {
            this._accumulatedBonuses.set(i, 0);
        }

        this._dawnFestivalParticipants.clear();
        this._duskFestivalParticipants.clear();
        this._dawnPreviousParticipants.clear();
        this._duskPreviousParticipants.clear();
        this._dawnFestivalScores.clear();
        this._duskFestivalScores.clear();
        Map<Integer, StatSet> map = new HashMap<>();

        for (int i = 0; i < 10; ++i) {
            int festivalId = i;
            if (i >= 5) {
                festivalId = i - 5;
            }

            StatSet set = new StatSet();
            set.set("festivalId", festivalId);
            set.set("cycle", this._signsCycle);
            set.set("date", "0");
            set.set("score", 0);
            set.set("members", "");
            if (i >= 5) {
                set.set("cabal", CabalType.DAWN);
            } else {
                set.set("cabal", CabalType.DUSK);
            }

            map.put(i, set);
        }

        this._festivalData.put(this._signsCycle, map);
        this.saveFestivalData(updateSettings);

        for (Player player : World.getInstance().getPlayers()) {
            ItemInstance bloodOfferings = player.getInventory().getItemByItemId(5901);
            if (bloodOfferings != null) {
                player.destroyItem("SevenSigns", bloodOfferings, null, false);
            }
        }

        LOGGER.info("Reinitialized Seven Signs Festival for next competition period.");
    }

    public final int getCurrentFestivalCycle() {
        return this._festivalCycle;
    }

    public final boolean isFestivalInitialized() {
        return this._festivalInitialized;
    }

    public final boolean isFestivalInProgress() {
        return this._festivalInProgress;
    }

    public void setNextCycleStart() {
        this._nextFestivalCycleStart = System.currentTimeMillis() + Config.ALT_FESTIVAL_CYCLE_LENGTH;
    }

    public void setNextFestivalStart(long milliFromNow) {
        this._nextFestivalStart = System.currentTimeMillis() + milliFromNow;
    }

    public final int getMinsToNextCycle() {
        return SevenSignsManager.getInstance().isSealValidationPeriod() ? -1 : Math.round((float) ((this._nextFestivalCycleStart - System.currentTimeMillis()) / 60000L));
    }

    public final int getMinsToNextFestival() {
        return SevenSignsManager.getInstance().isSealValidationPeriod() ? -1 : Math.round((float) ((this._nextFestivalStart - System.currentTimeMillis()) / 60000L)) + 1;
    }

    public final String getTimeToNextFestivalStr() {
        return SevenSignsManager.getInstance().isSealValidationPeriod() ? "<font color=\"FF0000\">This is the Seal Validation period. Festivals will resume next week.</font>" : "<font color=\"FF0000\">The next festival will begin in " + this.getMinsToNextFestival() + " minute(s).</font>";
    }

    public final int[] getFestivalForPlayer(Player player) {
        int[] playerFestivalInfo = new int[]{-1, -1};

        for (int festivalId = 0; festivalId < 5; ++festivalId) {
            List<Integer> participants = this._dawnFestivalParticipants.get(festivalId);
            if (participants != null && participants.contains(player.getObjectId())) {
                playerFestivalInfo[0] = CabalType.DAWN.ordinal();
                playerFestivalInfo[1] = festivalId;
                return playerFestivalInfo;
            }

            ++festivalId;
            participants = this._duskFestivalParticipants.get(festivalId);
            if (participants != null && participants.contains(player.getObjectId())) {
                playerFestivalInfo[0] = CabalType.DUSK.ordinal();
                playerFestivalInfo[1] = festivalId;
                return playerFestivalInfo;
            }
        }

        return playerFestivalInfo;
    }

    public final boolean isParticipant(Player player) {
        if (SevenSignsManager.getInstance().isSealValidationPeriod()) {
            return false;
        } else if (this._managerInstance == null) {
            return false;
        } else {
            Iterator<List<Integer>> var2 = this._dawnFestivalParticipants.values().iterator();

            List participants;
            do {
                if (!var2.hasNext()) {
                    var2 = this._duskFestivalParticipants.values().iterator();

                    do {
                        if (!var2.hasNext()) {
                            return false;
                        }

                        participants = var2.next();
                    } while (participants == null || !participants.contains(player.getObjectId()));

                    return true;
                }

                participants = var2.next();
            } while (participants == null || !participants.contains(player.getObjectId()));

            return true;
        }
    }

    public final List<Integer> getParticipants(CabalType oracle, int festivalId) {
        return oracle == CabalType.DAWN ? this._dawnFestivalParticipants.get(festivalId) : this._duskFestivalParticipants.get(festivalId);
    }

    public final List<Integer> getPreviousParticipants(CabalType oracle, int festivalId) {
        return oracle == CabalType.DAWN ? this._dawnPreviousParticipants.get(festivalId) : this._duskPreviousParticipants.get(festivalId);
    }

    public void setParticipants(CabalType oracle, int festivalId, Party festivalParty) {
        List<Integer> participants = null;
        if (festivalParty != null) {
            participants = new ArrayList<>(festivalParty.getMembersCount());

            for (Player player : festivalParty.getMembers()) {
                participants.add(player.getObjectId());
            }
        }

        if (oracle == CabalType.DAWN) {
            this._dawnFestivalParticipants.put(festivalId, participants);
        } else {
            this._duskFestivalParticipants.put(festivalId, participants);
        }

    }

    public void updateParticipants(Player player, Party festivalParty) {
        if (this.isParticipant(player)) {
            int[] playerFestInfo = this.getFestivalForPlayer(player);
            CabalType oracle = CabalType.VALUES[playerFestInfo[0]];
            int festivalId = playerFestInfo[1];
            if (festivalId > -1) {
                if (this._festivalInitialized) {
                    FestivalOfDarknessManager.L2DarknessFestival festivalInst = this._managerInstance.getFestivalInstance(oracle, festivalId);
                    if (festivalParty == null) {

                        for (int partyMemberObjId : this.getParticipants(oracle, festivalId)) {
                            Player partyMember = World.getInstance().getPlayer(partyMemberObjId);
                            if (partyMember != null) {
                                festivalInst.relocatePlayer(partyMember, true);
                            }
                        }
                    } else {
                        festivalInst.relocatePlayer(player, true);
                    }
                }

                this.setParticipants(oracle, festivalId, festivalParty);
                if (festivalParty != null && festivalParty.getMembersCount() < Config.ALT_FESTIVAL_MIN_PLAYER) {
                    this.updateParticipants(player, null);
                    festivalParty.removePartyMember(player, MessageType.EXPELLED);
                }
            }

        }
    }

    public final int getFinalScore(CabalType oracle, int festivalId) {
        return oracle == CabalType.DAWN ? this._dawnFestivalScores.get(festivalId) : this._duskFestivalScores.get(festivalId);
    }

    public final int getHighestScore(CabalType oracle, int festivalId) {
        return this.getHighestScoreData(oracle, festivalId).getInteger("score");
    }

    public final StatSet getHighestScoreData(CabalType oracle, int festivalId) {
        int offsetId = festivalId;
        if (oracle == CabalType.DAWN) {
            offsetId = festivalId + 5;
        }

        return (StatSet) ((Map<?, ?>) this._festivalData.get(this._signsCycle)).get(offsetId);
    }

    public final StatSet getOverallHighestScoreData(int festivalId) {
        StatSet set = null;
        int highestScore = 0;

        for (Map<Integer, StatSet> map : this._festivalData.values()) {
            for (StatSet setToTest : map.values()) {
                int currFestID = setToTest.getInteger("festivalId");
                int festivalScore = setToTest.getInteger("score");
                if (currFestID == festivalId && festivalScore > highestScore) {
                    highestScore = festivalScore;
                    set = setToTest;
                }
            }
        }

        return set;
    }

    public boolean setFinalScore(Player player, CabalType oracle, FestivalType festival, int offeringScore) {
        int festivalId = festival.ordinal();
        int currDawnHighScore = this.getHighestScore(CabalType.DAWN, festivalId);
        int currDuskHighScore = this.getHighestScore(CabalType.DUSK, festivalId);
        int thisCabalHighScore = 0;
        int otherCabalHighScore = 0;
        if (oracle == CabalType.DAWN) {
            thisCabalHighScore = currDawnHighScore;
            otherCabalHighScore = currDuskHighScore;
            this._dawnFestivalScores.put(festivalId, offeringScore);
        } else {
            thisCabalHighScore = currDuskHighScore;
            otherCabalHighScore = currDawnHighScore;
            this._duskFestivalScores.put(festivalId, offeringScore);
        }

        StatSet set = this.getHighestScoreData(oracle, festivalId);
        if (offeringScore <= thisCabalHighScore) {
            return false;
        } else if (thisCabalHighScore < otherCabalHighScore) {
            return false;
        } else {
            List<String> partyMembers = new ArrayList<>();

            for (int partyMember : this.getPreviousParticipants(oracle, festivalId)) {
                partyMembers.add(PlayerInfoTable.getInstance().getPlayerName(partyMember));
            }

            set.set("date", String.valueOf(System.currentTimeMillis()));
            set.set("score", offeringScore);
            set.set("members", String.join(",", partyMembers));
            if (offeringScore > otherCabalHighScore) {
                SevenSignsManager.getInstance().addFestivalScore(oracle, festival.getMaxScore());
            }

            this.saveFestivalData(true);
            return true;
        }
    }

    public final int getAccumulatedBonus(int festivalId) {
        return this._accumulatedBonuses.get(festivalId);
    }

    public final int getTotalAccumulatedBonus() {
        int totalAccumBonus = 0;

        int accumBonus;
        for (Iterator<Integer> var2 = this._accumulatedBonuses.iterator(); var2.hasNext(); totalAccumBonus += accumBonus) {
            accumBonus = var2.next();
        }

        return totalAccumBonus;
    }

    public void addAccumulatedBonus(int festivalId, int stoneType, int stoneAmount) {
        int eachStoneBonus = switch (stoneType) {
            case 6360 -> 3;
            case 6361 -> 5;
            case 6362 -> 10;
            default -> 0;
        };

        int newTotalBonus = this._accumulatedBonuses.get(festivalId) + stoneAmount * eachStoneBonus;
        this._accumulatedBonuses.set(festivalId, newTotalBonus);
    }

    public final int distribAccumulatedBonus(Player player) {
        if (SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId()) != SevenSignsManager.getInstance().getCabalHighestScore()) {
            return 0;
        } else {
            Map<Integer, StatSet> map = this._festivalData.get(this._signsCycle);
            if (map == null) {
                return 0;
            } else {
                String playerName = player.getName();
                int playerBonus = 0;

                for (StatSet set : map.values()) {
                    String members = set.getString("members");
                    if (members.contains(playerName)) {
                        int festivalId = set.getInteger("festivalId");
                        int numPartyMembers = members.split(",").length;
                        int totalAccumBonus = this._accumulatedBonuses.get(festivalId);
                        playerBonus = totalAccumBonus / numPartyMembers;
                        this._accumulatedBonuses.set(festivalId, totalAccumBonus - playerBonus);
                        break;
                    }
                }

                return playerBonus;
            }
        }
    }

    public final boolean increaseChallenge(CabalType oracle, int festivalId) {
        return this._managerInstance.getFestivalInstance(oracle, festivalId).increaseChallenge();
    }

    public void addPeaceZone(PeaceZone zone, boolean dawn) {
        if (dawn) {
            if (this._dawnPeace == null) {
                this._dawnPeace = new ArrayList<>(2);
            }

            if (!this._dawnPeace.contains(zone)) {
                this._dawnPeace.add(zone);
            }
        } else {
            if (this._duskPeace == null) {
                this._duskPeace = new ArrayList<>(2);
            }

            if (!this._duskPeace.contains(zone)) {
                this._duskPeace.add(zone);
            }
        }

    }

    public void sendMessageToAll(String senderName, String message) {
        CreatureSay cs = new CreatureSay(0, 1, senderName, message);
        Iterator var4;
        PeaceZone zone;
        if (this._dawnPeace != null) {
            var4 = this._dawnPeace.iterator();

            while (var4.hasNext()) {
                zone = (PeaceZone) var4.next();
                zone.broadcastPacket(cs);
            }
        }

        if (this._duskPeace != null) {
            var4 = this._duskPeace.iterator();

            while (var4.hasNext()) {
                zone = (PeaceZone) var4.next();
                zone.broadcastPacket(cs);
            }
        }

    }

    private static class SingletonHolder {
        protected static final FestivalOfDarknessManager INSTANCE = new FestivalOfDarknessManager();
    }

    private static class FestivalSpawn {
        protected final int _x;
        protected final int _y;
        protected final int _z;
        protected final int _heading;
        protected final int _npcId;

        protected FestivalSpawn(int x, int y, int z, int heading) {
            this._x = x;
            this._y = y;
            this._z = z;
            this._heading = heading < 0 ? Rnd.get(65536) : heading;
            this._npcId = -1;
        }

        protected FestivalSpawn(int[] spawnData) {
            this._x = spawnData[0];
            this._y = spawnData[1];
            this._z = spawnData[2];
            this._heading = spawnData[3] < 0 ? Rnd.get(65536) : spawnData[3];
            if (spawnData.length > 4) {
                this._npcId = spawnData[4];
            } else {
                this._npcId = -1;
            }

        }
    }

    public class FestivalManager implements Runnable {
        protected final Map<Integer, FestivalOfDarknessManager.L2DarknessFestival> _festivalInstances = new HashMap<>();

        public FestivalManager() {
            FestivalOfDarknessManager.this._managerInstance = this;
            ++FestivalOfDarknessManager.this._festivalCycle;
            FestivalOfDarknessManager.this.setNextCycleStart();
            FestivalOfDarknessManager.this.setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FestivalOfDarknessManager.FESTIVAL_SIGNUP_TIME);
        }

        public synchronized void run() {
            if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                if (SevenSignsManager.getInstance().getMilliToPeriodChange() >= Config.ALT_FESTIVAL_CYCLE_LENGTH) {
                    if (FestivalOfDarknessManager.this.getMinsToNextFestival() == 2) {
                        FestivalOfDarknessManager.this.sendMessageToAll("Festival Guide", "The main event will start in 2 minutes. Please register now.");
                    }

                    try {
                        this.wait(FestivalOfDarknessManager.FESTIVAL_SIGNUP_TIME);
                    } catch (InterruptedException ignored) {
                    }

                    FestivalOfDarknessManager.this._dawnPreviousParticipants.clear();
                    FestivalOfDarknessManager.this._duskPreviousParticipants.clear();
                    Iterator<L2DarknessFestival> var1 = this._festivalInstances.values().iterator();

                    FestivalOfDarknessManager.L2DarknessFestival festivalInst;
                    while (var1.hasNext()) {
                        festivalInst = var1.next();
                        festivalInst.unspawnMobs();
                    }

                    FestivalOfDarknessManager.this._noPartyRegister = true;

                    while (true) {
                        while (FestivalOfDarknessManager.this._noPartyRegister) {
                            if (FestivalOfDarknessManager.this._duskFestivalParticipants.isEmpty() && FestivalOfDarknessManager.this._dawnFestivalParticipants.isEmpty()) {
                                try {
                                    FestivalOfDarknessManager.this.setNextCycleStart();
                                    FestivalOfDarknessManager.this.setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FestivalOfDarknessManager.FESTIVAL_SIGNUP_TIME);
                                    this.wait(Config.ALT_FESTIVAL_CYCLE_LENGTH - FestivalOfDarknessManager.FESTIVAL_SIGNUP_TIME);
                                    var1 = this._festivalInstances.values().iterator();

                                    while (var1.hasNext()) {
                                        festivalInst = var1.next();
                                        if (!festivalInst._npcInsts.isEmpty()) {
                                            festivalInst.unspawnMobs();
                                        }
                                    }
                                } catch (InterruptedException ignored) {
                                }
                            } else {
                                FestivalOfDarknessManager.this._noPartyRegister = false;
                            }
                        }

                        long elapsedTime = 0L;

                        for (int i = 0; i < 5; ++i) {
                            if (FestivalOfDarknessManager.this._duskFestivalParticipants.get(i) != null) {
                                this._festivalInstances.put(10 + i, FestivalOfDarknessManager.this.new L2DarknessFestival(CabalType.DUSK, i));
                            }

                            if (FestivalOfDarknessManager.this._dawnFestivalParticipants.get(i) != null) {
                                this._festivalInstances.put(20 + i, FestivalOfDarknessManager.this.new L2DarknessFestival(CabalType.DAWN, i));
                            }
                        }

                        FestivalOfDarknessManager.this._festivalInitialized = true;
                        FestivalOfDarknessManager.this.setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH);
                        FestivalOfDarknessManager.this.sendMessageToAll("Festival Guide", "The main event is now starting.");

                        try {
                            this.wait(Config.ALT_FESTIVAL_FIRST_SPAWN);
                        } catch (InterruptedException ignored) {
                        }

                        elapsedTime = Config.ALT_FESTIVAL_FIRST_SPAWN;
                        FestivalOfDarknessManager.this._festivalInProgress = true;
                        Iterator<L2DarknessFestival> var16 = this._festivalInstances.values().iterator();

                        FestivalOfDarknessManager.L2DarknessFestival festivalInstx;
                        while (var16.hasNext()) {
                            festivalInstx = var16.next();
                            festivalInstx.festivalStart();
                            festivalInstx.sendMessageToParticipants("The main event is now starting.");
                        }

                        try {
                            this.wait(Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN);
                        } catch (InterruptedException ignored) {
                        }

                        elapsedTime += Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN;
                        var16 = this._festivalInstances.values().iterator();

                        while (var16.hasNext()) {
                            festivalInstx = var16.next();
                            festivalInstx.moveMonstersToCenter();
                        }

                        try {
                            this.wait(Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM);
                        } catch (InterruptedException ignored) {
                        }

                        var16 = this._festivalInstances.values().iterator();

                        while (var16.hasNext()) {
                            festivalInstx = var16.next();
                            festivalInstx.spawnFestivalMonsters(30, 2);
                            long end = (Config.ALT_FESTIVAL_LENGTH - Config.ALT_FESTIVAL_SECOND_SPAWN) / 60000L;
                            festivalInstx.sendMessageToParticipants("The Festival of Darkness will end in " + end + " minute(s).");
                        }

                        elapsedTime += Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM;

                        try {
                            this.wait(Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN);
                        } catch (InterruptedException ignored) {
                        }

                        var16 = this._festivalInstances.values().iterator();

                        while (var16.hasNext()) {
                            festivalInstx = var16.next();
                            festivalInstx.moveMonstersToCenter();
                        }

                        elapsedTime += Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN;

                        try {
                            this.wait(Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM);
                        } catch (InterruptedException ignored) {
                        }

                        var16 = this._festivalInstances.values().iterator();

                        while (var16.hasNext()) {
                            festivalInstx = var16.next();
                            festivalInstx.spawnFestivalMonsters(60, 3);
                            festivalInstx.sendMessageToParticipants("The chests have spawned! Be quick, the festival will end soon.");
                        }

                        elapsedTime += Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM;

                        try {
                            this.wait(Config.ALT_FESTIVAL_LENGTH - elapsedTime);
                        } catch (InterruptedException ignored) {
                        }

                        FestivalOfDarknessManager.this._festivalInProgress = false;
                        var16 = this._festivalInstances.values().iterator();

                        while (var16.hasNext()) {
                            festivalInstx = var16.next();
                            festivalInstx.festivalEnd();
                        }

                        FestivalOfDarknessManager.this._dawnFestivalParticipants.clear();
                        FestivalOfDarknessManager.this._duskFestivalParticipants.clear();
                        FestivalOfDarknessManager.this._festivalInitialized = false;
                        FestivalOfDarknessManager.this.sendMessageToAll("Festival Witch", "That will do! I'll move you to the outside soon.");
                        return;
                    }
                }
            }
        }

        public final FestivalOfDarknessManager.L2DarknessFestival getFestivalInstance(CabalType oracle, int festivalId) {
            if (!FestivalOfDarknessManager.this.isFestivalInitialized()) {
                return null;
            } else {
                festivalId += oracle == CabalType.DUSK ? 10 : 20;
                return this._festivalInstances.get(festivalId);
            }
        }
    }

    public class L2DarknessFestival {
        protected final CabalType _cabal;
        protected final int _levelRange;
        protected final List<FestivalMonster> _npcInsts;
        private final Map<Integer, FestivalOfDarknessManager.FestivalSpawn> _originalLocations;
        private final FestivalOfDarknessManager.FestivalSpawn _startLocation;
        private final FestivalOfDarknessManager.FestivalSpawn _witchSpawn;
        protected boolean _challengeIncreased;
        private Npc _witchInst;
        private List<Integer> _participants;

        protected L2DarknessFestival(CabalType cabal, int levelRange) {
            this._cabal = cabal;
            this._levelRange = levelRange;
            this._originalLocations = new HashMap<>();
            this._npcInsts = new ArrayList<>();
            if (cabal == CabalType.DAWN) {
                this._participants = FestivalOfDarknessManager.this._dawnFestivalParticipants.get(levelRange);
                this._witchSpawn = new FestivalOfDarknessManager.FestivalSpawn(FestivalOfDarknessManager.FESTIVAL_DAWN_WITCH_SPAWNS[levelRange]);
                this._startLocation = new FestivalOfDarknessManager.FestivalSpawn(FestivalOfDarknessManager.FESTIVAL_DAWN_PLAYER_SPAWNS[levelRange]);
            } else {
                this._participants = FestivalOfDarknessManager.this._duskFestivalParticipants.get(levelRange);
                this._witchSpawn = new FestivalOfDarknessManager.FestivalSpawn(FestivalOfDarknessManager.FESTIVAL_DUSK_WITCH_SPAWNS[levelRange]);
                this._startLocation = new FestivalOfDarknessManager.FestivalSpawn(FestivalOfDarknessManager.FESTIVAL_DUSK_PLAYER_SPAWNS[levelRange]);
            }

            if (this._participants == null) {
                this._participants = new ArrayList<>();
            }

            this.festivalInit();
        }

        protected void festivalInit() {
            if (this._participants != null && !this._participants.isEmpty()) {

                for (int participantObjId : this._participants) {
                    Player participant = World.getInstance().getPlayer(participantObjId);
                    if (participant != null) {
                        this._originalLocations.put(participantObjId, new FestivalSpawn(participant.getX(), participant.getY(), participant.getZ(), participant.getHeading()));
                        int x = this._startLocation._x;
                        int y = this._startLocation._y;
                        boolean isPositive = Rnd.get(2) == 1;
                        if (isPositive) {
                            x += Rnd.get(230);
                            y += Rnd.get(230);
                        } else {
                            x -= Rnd.get(230);
                            y -= Rnd.get(230);
                        }

                        participant.getAI().setIntention(IntentionType.IDLE);
                        participant.teleportTo(x, y, this._startLocation._z, 20);
                        participant.stopAllEffectsExceptThoseThatLastThroughDeath();
                        ItemInstance bloodOfferings = participant.getInventory().getItemByItemId(5901);
                        if (bloodOfferings != null) {
                            participant.destroyItem("SevenSigns", bloodOfferings, null, true);
                        }
                    }
                }
            }

            NpcTemplate witchTemplate = NpcData.getInstance().getTemplate(this._witchSpawn._npcId);

            try {
                L2Spawn npcSpawn = new L2Spawn(witchTemplate);
                npcSpawn.setLoc(this._witchSpawn._x, this._witchSpawn._y, this._witchSpawn._z, this._witchSpawn._heading);
                npcSpawn.setRespawnDelay(1);
                npcSpawn.setRespawnState(true);
                SpawnTable.getInstance().addSpawn(npcSpawn, false);
                this._witchInst = npcSpawn.doSpawn(false);
            } catch (Exception var8) {
                FestivalOfDarknessManager.LOGGER.error("Couldn't properly spawn Festival Witch {}.", var8, this._witchSpawn._npcId);
            }

            MagicSkillUse msu = new MagicSkillUse(this._witchInst, this._witchInst, 2003, 1, 1, 0);
            this._witchInst.broadcastPacket(msu);
            msu = new MagicSkillUse(this._witchInst, this._witchInst, 2133, 1, 1, 0);
            this._witchInst.broadcastPacket(msu);
            this.sendMessageToParticipants("The festival will begin in 2 minutes.");
        }

        protected void festivalStart() {
            this.spawnFestivalMonsters(60, 0);
        }

        protected void moveMonstersToCenter() {
            Iterator<FestivalMonster> var1 = this._npcInsts.iterator();

            while (true) {
                FestivalMonster festivalMob;
                IntentionType currIntention;
                do {
                    do {
                        if (!var1.hasNext()) {
                            return;
                        }

                        festivalMob = var1.next();
                    } while (festivalMob.isDead());

                    currIntention = festivalMob.getAI().getDesire().getIntention();
                } while (currIntention != IntentionType.IDLE && currIntention != IntentionType.ACTIVE);

                int x = this._startLocation._x;
                int y = this._startLocation._y;
                if (Rnd.nextBoolean()) {
                    x += Rnd.get(230);
                    y += Rnd.get(230);
                } else {
                    x -= Rnd.get(230);
                    y -= Rnd.get(230);
                }

                festivalMob.setRunning();
                festivalMob.getAI().setIntention(IntentionType.MOVE_TO, new Location(x, y, this._startLocation._z));
            }
        }

        protected void spawnFestivalMonsters(int respawnDelay, int spawnType) {
            int[][] _npcSpawns = switch (spawnType) {
                case 0, 1 ->
                        this._cabal == CabalType.DAWN ? FestivalOfDarknessManager.FESTIVAL_DAWN_PRIMARY_SPAWNS[this._levelRange] : FestivalOfDarknessManager.FESTIVAL_DUSK_PRIMARY_SPAWNS[this._levelRange];
                case 2 ->
                        this._cabal == CabalType.DAWN ? FestivalOfDarknessManager.FESTIVAL_DAWN_SECONDARY_SPAWNS[this._levelRange] : FestivalOfDarknessManager.FESTIVAL_DUSK_SECONDARY_SPAWNS[this._levelRange];
                case 3 ->
                        this._cabal == CabalType.DAWN ? FestivalOfDarknessManager.FESTIVAL_DAWN_CHEST_SPAWNS[this._levelRange] : FestivalOfDarknessManager.FESTIVAL_DUSK_CHEST_SPAWNS[this._levelRange];
                default -> null;
            };

            if (_npcSpawns != null) {
                int[][] var4 = _npcSpawns;
                int var5 = _npcSpawns.length;

                for (int var6 = 0; var6 < var5; ++var6) {
                    int[] _npcSpawn = var4[var6];
                    FestivalOfDarknessManager.FestivalSpawn currSpawn = new FestivalOfDarknessManager.FestivalSpawn(_npcSpawn);
                    if (spawnType != 1 || !FestivalOfDarknessManager.isFestivalArcher(currSpawn._npcId)) {
                        NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(currSpawn._npcId);

                        try {
                            L2Spawn npcSpawn = new L2Spawn(npcTemplate);
                            npcSpawn.setLoc(currSpawn._x, currSpawn._y, currSpawn._z, Rnd.get(65536));
                            npcSpawn.setRespawnDelay(respawnDelay);
                            npcSpawn.setRespawnState(true);
                            SpawnTable.getInstance().addSpawn(npcSpawn, false);
                            FestivalMonster festivalMob = (FestivalMonster) npcSpawn.doSpawn(false);
                            if (spawnType == 1) {
                                festivalMob.setOfferingBonus(2);
                            } else if (spawnType == 3) {
                                festivalMob.setOfferingBonus(5);
                            }

                            this._npcInsts.add(festivalMob);
                        } catch (Exception var12) {
                            FestivalOfDarknessManager.LOGGER.error("Couldn't properly spawn Npc {}.", var12, currSpawn._npcId);
                        }
                    }
                }
            }

        }

        protected boolean increaseChallenge() {
            if (this._challengeIncreased) {
                return false;
            } else {
                this._challengeIncreased = true;
                this.spawnFestivalMonsters(60, 1);
                return true;
            }
        }

        public void sendMessageToParticipants(String message) {
            if (this._participants != null && !this._participants.isEmpty()) {
                this._witchInst.broadcastPacket(new CreatureSay(this._witchInst.getObjectId(), 0, "Festival Witch", message));
            }

        }

        protected void festivalEnd() {
            if (this._participants != null && !this._participants.isEmpty()) {

                for (int participantObjId : this._participants) {
                    Player participant = World.getInstance().getPlayer(participantObjId);
                    if (participant != null) {
                        this.relocatePlayer(participant, false);
                        participant.sendMessage("The festival has ended. Your party leader must now register your score before the next festival takes place.");
                    }
                }

                if (this._cabal == CabalType.DAWN) {
                    FestivalOfDarknessManager.this._dawnPreviousParticipants.put(this._levelRange, this._participants);
                } else {
                    FestivalOfDarknessManager.this._duskPreviousParticipants.put(this._levelRange, this._participants);
                }
            }

            this._participants = null;
            this.unspawnMobs();
        }

        protected void unspawnMobs() {
            if (this._witchInst != null) {
                this._witchInst.getSpawn().setRespawnState(false);
                this._witchInst.deleteMe();
                SpawnTable.getInstance().deleteSpawn(this._witchInst.getSpawn(), false);
            }

            if (this._npcInsts != null) {

                for (FestivalMonster monsterInst : this._npcInsts) {
                    if (monsterInst != null) {
                        monsterInst.getSpawn().setRespawnState(false);
                        monsterInst.deleteMe();
                        SpawnTable.getInstance().deleteSpawn(monsterInst.getSpawn(), false);
                    }
                }
            }

        }

        public void relocatePlayer(Player participant, boolean isRemoving) {
            if (participant != null) {
                try {
                    FestivalOfDarknessManager.FestivalSpawn origPosition = this._originalLocations.get(participant.getObjectId());
                    if (isRemoving) {
                        this._originalLocations.remove(participant.getObjectId());
                    }

                    participant.getAI().setIntention(IntentionType.IDLE);
                    participant.teleportTo(origPosition._x, origPosition._y, origPosition._z, 20);
                    participant.sendMessage("You have been removed from the festival arena.");
                } catch (Exception var4) {
                    participant.teleportTo(TeleportType.TOWN);
                    participant.sendMessage("You have been removed from the festival arena.");
                }

            }
        }
    }
}