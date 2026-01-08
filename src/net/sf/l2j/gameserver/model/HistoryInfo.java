package net.sf.l2j.gameserver.model;

public class HistoryInfo {
    private final int _raceId;
    private int _first;
    private int _second;
    private double _oddRate;

    public HistoryInfo(int raceId, int first, int second, double oddRate) {
        this._raceId = raceId;
        this._first = first;
        this._second = second;
        this._oddRate = oddRate;
    }

    public int getRaceId() {
        return this._raceId;
    }

    public int getFirst() {
        return this._first;
    }

    public void setFirst(int first) {
        this._first = first;
    }

    public int getSecond() {
        return this._second;
    }

    public void setSecond(int second) {
        this._second = second;
    }

    public double getOddRate() {
        return this._oddRate;
    }

    public void setOddRate(double oddRate) {
        this._oddRate = oddRate;
    }
}
