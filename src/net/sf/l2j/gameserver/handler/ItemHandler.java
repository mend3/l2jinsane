package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.handler.itemhandlers.*;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;

import java.util.HashMap;
import java.util.Map;

public class ItemHandler {
    private final Map<Integer, IItemHandler> _entries = new HashMap<>();

    protected ItemHandler() {
        registerHandler(new BeastSoulShot());
        registerHandler(new BeastSpice());
        registerHandler(new BeastSpiritShot());
        registerHandler(new BlessedSpiritShot());
        registerHandler(new Book());
        registerHandler(new Calculator());
        registerHandler(new Elixir());
        registerHandler(new EnchantScrolls());
        registerHandler(new FishShots());
        registerHandler(new Harvester());
        registerHandler(new ItemSkills());
        registerHandler(new Keys());
        registerHandler(new Maps());
        registerHandler(new MercTicket());
        registerHandler(new PaganKeys());
        registerHandler(new PetFood());
        registerHandler(new Recipes());
        registerHandler(new RollingDice());
        registerHandler(new ScrollOfResurrection());
        registerHandler(new SeedHandler());
        registerHandler(new SevenSignsRecord());
        registerHandler(new SoulShots());
        registerHandler(new SpecialXMas());
        registerHandler(new SoulCrystals());
        registerHandler(new SpiritShot());
        registerHandler(new SummonItems());
        registerHandler(new AdenaToGoldBar());
        registerHandler(new GoldBarToAdena());
        registerHandler(new ClanFull());
        registerHandler(new RewardBox());
        registerHandler(new AgathionItem());
        registerHandler(new DungeonReset());
        registerHandler(new Vip24h());
        registerHandler(new Vip7days());
        registerHandler(new Vip15days());
        registerHandler(new Vip30days());
        registerHandler(new NoblesItem());
        registerHandler(new GetInvisible());
    }

    public static ItemHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void registerHandler(IItemHandler handler) {
        this._entries.put(handler.getClass().getSimpleName().intern().hashCode(), handler);
    }

    public IItemHandler getHandler(EtcItem item) {
        if (item == null || item.getHandlerName() == null)
            return null;
        return this._entries.get(item.getHandlerName().hashCode());
    }

    public int size() {
        return this._entries.size();
    }

    private static class SingletonHolder {
        protected static final ItemHandler INSTANCE = new ItemHandler();
    }
}
