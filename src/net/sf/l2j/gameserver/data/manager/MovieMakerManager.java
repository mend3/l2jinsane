/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;

import java.util.HashMap;
import java.util.Map;

public class MovieMakerManager {
    protected final Map<Integer, Sequence> _sequences = new HashMap<>();

    public static MovieMakerManager getInstance() {
        return MovieMakerManager.SingletonHolder.INSTANCE;
    }

    public void mainHtm(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        if (this._sequences.isEmpty()) {
            html.setFile("data/html/admin/movie/main_empty.htm");
        } else {
            StringBuilder sb = new StringBuilder();

            for (Sequence sequence : this._sequences.values()) {
                StringUtil.append(sb, "<tr><td>", sequence._sequenceId, ": (", sequence._dist, ", ", sequence._yaw, ", ", sequence._pitch, ", ", sequence._time, ", ", sequence._duration, ", ", sequence._turn, ", ", sequence._rise, ", ", sequence._widescreen, ")</td></tr>");
            }

            html.setFile("data/html/admin/movie/main_notempty.htm");
            html.replace("%sequences%", sb.toString());
        }

        player.sendPacket(html);
    }

    public void playSequence(int id, Player player) {
        Sequence sequence = this._sequences.get(id);
        if (sequence == null) {
            player.sendMessage("Wrong sequence id.");
            this.mainHtm(player);
        } else {
            player.sendPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
        }
    }

    public void broadcastSequence(int id, Player player) {
        Sequence sequence = this._sequences.get(id);
        if (sequence == null) {
            player.sendMessage("Wrong sequence id.");
            this.mainHtm(player);
        } else {
            player.broadcastPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
        }
    }

    public void playSequence(Player player, int objid, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int screen) {
        player.sendPacket(new SpecialCamera(objid, dist, yaw, pitch, time, duration, turn, rise, screen, 0));
    }

    public void addSequence(Player player, int seqId, int objid, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int screen) {
        if (this._sequences.containsKey(seqId)) {
            player.sendMessage("This sequence already exists.");
        } else {
            Sequence sequence = new Sequence();
            sequence._sequenceId = seqId;
            sequence._objid = objid;
            sequence._dist = dist;
            sequence._yaw = yaw;
            sequence._pitch = pitch;
            sequence._time = time;
            sequence._duration = duration;
            sequence._turn = turn;
            sequence._rise = rise;
            sequence._widescreen = screen;
            this._sequences.put(seqId, sequence);
        }

        this.mainHtm(player);
    }

    public void addSequence(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/movie/add_sequence.htm");
        player.sendPacket(html);
    }

    public void editSequence(int id, Player player) {
        Sequence sequence = this._sequences.get(id);
        if (sequence == null) {
            player.sendMessage("The sequence couldn't be updated.");
            this.mainHtm(player);
        } else {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/admin/movie/edit_sequence.htm");
            html.replace("%sId%", sequence._sequenceId);
            html.replace("%sDist%", sequence._dist);
            html.replace("%sYaw%", sequence._yaw);
            html.replace("%sPitch%", sequence._pitch);
            html.replace("%sTime%", sequence._time);
            html.replace("%sDuration%", sequence._duration);
            html.replace("%sTurn%", sequence._turn);
            html.replace("%sRise%", sequence._rise);
            html.replace("%sWidescreen%", sequence._widescreen);
            player.sendPacket(html);
        }
    }

    public void updateSequence(Player player, int seqId, int objid, int dist, int yaw, int pitch, int time, int duration, int turn, int rise, int screen) {
        Sequence sequence = this._sequences.get(seqId);
        if (sequence == null) {
            player.sendMessage("This sequence doesn't exist.");
        } else {
            sequence._objid = objid;
            sequence._dist = dist;
            sequence._yaw = yaw;
            sequence._pitch = pitch;
            sequence._time = time;
            sequence._duration = duration;
            sequence._turn = turn;
            sequence._rise = rise;
            sequence._widescreen = screen;
        }

        this.mainHtm(player);
    }

    public void deleteSequence(int id, Player player) {
        if (this._sequences.remove(id) == null) {
            player.sendMessage("This sequence id doesn't exist.");
        }

        this.mainHtm(player);
    }

    public void playMovie(int broadcast, Player player) {
        if (this._sequences.isEmpty()) {
            player.sendMessage("There is nothing to play.");
            this.mainHtm(player);
        } else {
            ThreadPool.schedule(new Play(1, broadcast, player), 500L);
        }
    }

    private static class SingletonHolder {
        protected static final MovieMakerManager INSTANCE = new MovieMakerManager();
    }

    protected static class Sequence {
        protected int _sequenceId;
        protected int _objid;
        protected int _dist;
        protected int _yaw;
        protected int _pitch;
        protected int _time;
        protected int _duration;
        protected int _turn;
        protected int _rise;
        protected int _widescreen;
    }

    private class Play implements Runnable {
        private final int _id;
        private final int _broad;
        private final Player _player;

        public Play(int id, int broadcast, Player player) {
            this._id = id;
            this._broad = broadcast;
            this._player = player;
        }

        public void run() {
            Sequence sequence = MovieMakerManager.this._sequences.get(this._id);
            if (sequence == null) {
                this._player.sendMessage("Movie ended on sequence: " + (this._id - 1) + ".");
                MovieMakerManager.this.mainHtm(this._player);
            } else {
                if (this._broad == 1) {
                    this._player.broadcastPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
                } else {
                    this._player.sendPacket(new SpecialCamera(sequence._objid, sequence._dist, sequence._yaw, sequence._pitch, sequence._time, sequence._duration, sequence._turn, sequence._rise, sequence._widescreen, 0));
                }

                ThreadPool.schedule(MovieMakerManager.this.new Play(this._id + 1, this._broad, this._player), sequence._duration - 100);
            }
        }
    }
}
