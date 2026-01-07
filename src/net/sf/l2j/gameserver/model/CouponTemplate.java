package net.sf.l2j.gameserver.model;

public class CouponTemplate {
    private final int _id;
    private final int _count;
    private final int _price;

    public CouponTemplate(int id, int count, int price) {
        _id = id;
        _count = count;
        _price = price;
    }

    public int getId() {
        return _id;
    }

    public int getCount() {
        return _count;
    }

    public int getPrice() {
        return _price;
    }
}