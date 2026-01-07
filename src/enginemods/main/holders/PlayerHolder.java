package enginemods.main.holders;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerHolder {
    private final String _accountName;
    private final AtomicInteger _maestriasPoints;
    private final AtomicInteger _statsPoints;
    private final Map<Stats, Integer> _statsCustom;
    private final Map<Integer, AuctionItemHolder> _auctionsSell;
    private final Map<Integer, AuctionItemHolder> _auctionsSold;
    public volatile String _answerRight;
    public volatile int _kills;
    public int _attempts;
    private int _objectId;
    private String _name;
    private boolean _isOffline;
    private boolean _isSellBuff;
    private int _sellBuffPrice;
    private boolean _isAio;
    private long _aioExpireDate;
    private boolean _isVip;
    private long _vipExpireDate;
    private boolean _isFake;
    private Location _posToFarm;
    private int _rebirth;
    private boolean _hasVote;

    private long _lastVote;

    public PlayerHolder(int objectId, String name, String accountName) {
        this._isOffline = false;
        this._isSellBuff = false;
        this._isAio = false;
        this._aioExpireDate = 0L;
        this._isVip = false;
        this._vipExpireDate = 0L;
        this._isFake = false;
        this._posToFarm = null;
        this._rebirth = 0;
        this._maestriasPoints = new AtomicInteger(0);
        this._statsPoints = new AtomicInteger(0);
        this._statsCustom = new HashMap<>();
        this._statsCustom.put(Stats.STAT_STR, Integer.valueOf(0));
        this._statsCustom.put(Stats.STAT_CON, Integer.valueOf(0));
        this._statsCustom.put(Stats.STAT_DEX, Integer.valueOf(0));
        this._statsCustom.put(Stats.STAT_INT, Integer.valueOf(0));
        this._statsCustom.put(Stats.STAT_WIT, Integer.valueOf(0));
        this._statsCustom.put(Stats.STAT_MEN, Integer.valueOf(0));
        this._auctionsSell = new LinkedHashMap<>(100);
        this._auctionsSold = new LinkedHashMap<>(100);
        this._kills = 0;
        this._attempts = 3;
        this._objectId = objectId;
        this._name = name;
        this._accountName = accountName;
    }

    public int getObjectId() {
        return this._objectId;
    }

    public void setObjectId(int objectId) {
        this._objectId = objectId;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getAccountName() {
        return this._accountName;
    }

    public boolean isOffline() {
        return this._isOffline;
    }

    public void setOffline(boolean mode) {
        this._isOffline = mode;
    }

    public boolean isSellBuff() {
        return this._isSellBuff;
    }

    public void setSellBuff(boolean isSellBuff) {
        this._isSellBuff = isSellBuff;
    }

    public int getSellBuffPrice() {
        return this._sellBuffPrice;
    }

    public void setSellBuffPrice(int sellBuffPrice) {
        this._sellBuffPrice = sellBuffPrice;
    }

    public boolean isAio() {
        return this._isAio;
    }

    public void setAio(boolean isAio) {
        this._isAio = isAio;
    }

    public String getAioExpireDateFormat() {
        return (new SimpleDateFormat("dd-MMM-yyyy")).format(new Date(this._aioExpireDate));
    }

    public long getAioExpireDate() {
        return this._aioExpireDate;
    }

    public void setAioExpireDate(long dayTime) {
        this._aioExpireDate = dayTime;
    }

    public boolean isVip() {
        return this._isVip;
    }

    public void setVip(boolean isVip) {
        this._isVip = isVip;
    }

    public String getVipExpireDateFormat() {
        return (new SimpleDateFormat("dd-MMM-yyyy")).format(new Date(this._vipExpireDate));
    }

    public long getVipExpireDate() {
        return this._vipExpireDate;
    }

    public void setVipExpireDate(long dayTime) {
        this._vipExpireDate = dayTime;
    }

    public boolean isFake() {
        return this._isFake;
    }

    public void setFake(boolean isFake) {
        this._isFake = isFake;
    }

    public void setPosToFarm(int x, int y, int z) {
        this._posToFarm = new Location(x, y, z);
    }

    public Location getPosToFarm() {
        return this._posToFarm;
    }

    public void setPosToFarm(String pos) {
        int x = Integer.parseInt(pos.split(",")[0]);
        int y = Integer.parseInt(pos.split(",")[1]);
        int z = Integer.parseInt(pos.split(",")[2]);
        this._posToFarm = new Location(x, y, z);
    }

    public int getRebirth() {
        return this._rebirth;
    }

    public void setRebirth(int rebirth) {
        this._rebirth = rebirth;
    }

    public void increaseRebirth() {
        this._rebirth++;
    }

    public AtomicInteger getMaestriasPoints() {
        return this._maestriasPoints;
    }

    public AtomicInteger getStatsPoints() {
        return this._statsPoints;
    }

    public int getCustomStat(Stats stat) {
        return this._statsCustom.get(stat);
    }

    public void addCustomStat(Stats stat, int value) {
        int oldValue = getCustomStat(stat);
        this._statsCustom.put(stat, Integer.valueOf(oldValue + value));
    }

    public Map<Integer, AuctionItemHolder> getAuctionsSell() {
        return this._auctionsSell;
    }

    public void addAuctionSell(int id, AuctionItemHolder auction) {
        this._auctionsSell.put(Integer.valueOf(id), auction);
    }

    public void removeAuctionSell(int key) {
        this._auctionsSell.remove(Integer.valueOf(key));
    }

    public Map<Integer, AuctionItemHolder> getAuctionsSold() {
        return this._auctionsSold;
    }

    public void addAuctionSold(int id, AuctionItemHolder auction) {
        this._auctionsSold.put(Integer.valueOf(id), auction);
    }

    public void removeAuctionSold(int id) {
        this._auctionsSold.remove(Integer.valueOf(id));
    }

    public boolean isAnswerRight(String bypas) {
        return this._answerRight.equals(bypas);
    }

    public void setAnswerRight(String anserRight) {
        this._answerRight = anserRight;
    }

    public int getKills() {
        return this._kills;
    }

    public void increaseKills() {
        this._kills++;
    }

    public void resetKills() {
        this._kills = 0;
    }

    public int getAttempts() {
        return this._attempts;
    }

    public void decreaseAttempts() {
        this._attempts--;
    }

    public void resetAttempts() {
        this._attempts = 3;
    }

    public boolean isHasVote() {
        return this._hasVote;
    }

    public void setHasVote(boolean hasVote) {
        this._hasVote = hasVote;
    }

    public long getLastVote() {
        return this._lastVote;
    }

    public void setLastVote(long lastVote) {
        this._lastVote = lastVote;
    }
}
