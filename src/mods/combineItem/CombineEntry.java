package mods.combineItem;

public class CombineEntry {
    private final int _item1;

    private final int _count1;

    private final int _item2;

    private final int _count2;

    private final int _result;

    private final int _countResutl;

    private final int _adena;

    private final int _adenaCount;

    private final double _chance;

    private final int _failureItem;

    public CombineEntry(int item1, int count1, int item2, int count2, int result, int countResutl, int adena, int adenaCount, double chance, int failureItem) {
        this._item1 = item1;
        this._count1 = count1;
        this._item2 = item2;
        this._count2 = count2;
        this._result = result;
        this._countResutl = countResutl;
        this._adena = adena;
        this._adenaCount = adenaCount;
        this._chance = chance;
        this._failureItem = failureItem;
    }

    public CombineEntry(int item1, int count1, int item2, int count2, int result, int countResutl, int adena, int adenaCount, double chance) {
        this(item1, count1, item2, count2, result, countResutl, adena, adenaCount, chance, item1);
    }

    public int getItem1() {
        return this._item1;
    }

    public int getCount1() {
        return this._count1;
    }

    public int getItem2() {
        return this._item2;
    }

    public int getCount2() {
        return this._count2;
    }

    public int getResult() {
        return this._result;
    }

    public int getCounResult() {
        return this._countResutl;
    }

    public int getAdena() {
        return this._adena;
    }

    public int getCounAdena() {
        return this._adenaCount;
    }

    public double getChance() {
        return this._chance;
    }

    public int getFailureItem() {
        return this._failureItem;
    }
}
