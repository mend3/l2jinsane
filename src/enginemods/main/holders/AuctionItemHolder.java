package enginemods.main.holders;

public class AuctionItemHolder {
    private final int _ownerId;
    private final int _itemObjId;
    private final int _itemId;
    private final int _itemCount;
    private final int _itemEnchantLevel;
    private final int _itemPriceCount;
    private final int _itemPriceId;
    private int _key;

    public AuctionItemHolder(String auction) {
        String[] auctions = auction.split(" ");
        this._key = Integer.parseInt(auctions[0]);
        this._ownerId = Integer.parseInt(auctions[1]);
        this._itemObjId = Integer.parseInt(auctions[2]);
        this._itemId = Integer.parseInt(auctions[3]);
        this._itemCount = Integer.parseInt(auctions[4]);
        this._itemEnchantLevel = Integer.parseInt(auctions[5]);
        this._itemPriceCount = Integer.parseInt(auctions[6]);
        this._itemPriceId = Integer.parseInt(auctions[7]);
    }

    public AuctionItemHolder(int key, int ownerId, int itemObjId, int itemId, int itemCount, int itemEnchantLevel, int itemPriceCount, int itemPriceId) {
        this._key = key;
        this._ownerId = ownerId;
        this._itemObjId = itemObjId;
        this._itemId = itemId;
        this._itemCount = itemCount;
        this._itemEnchantLevel = itemEnchantLevel;
        this._itemPriceCount = itemPriceCount;
        this._itemPriceId = itemPriceId;
    }

    public int getkey() {
        return this._key;
    }

    public void setKey(int key) {
        this._key = key;
    }

    public int getOwnerId() {
        return this._ownerId;
    }

    public int getItemObjId() {
        return this._itemObjId;
    }

    public int getItemId() {
        return this._itemId;
    }

    public int getItemEnchantLevel() {
        return this._itemEnchantLevel;
    }

    public int getItemPriceCount() {
        return this._itemPriceCount;
    }

    public int getItemPriceId() {
        return this._itemPriceId;
    }

    public int getItemCount() {
        return this._itemCount;
    }

    public String toString() {
        return this._key + " " + this._key + " " + this._ownerId + " " + this._itemObjId + " " + this._itemId + " " + this._itemCount + " " + this._itemEnchantLevel + " " + this._itemPriceCount;
    }
}
