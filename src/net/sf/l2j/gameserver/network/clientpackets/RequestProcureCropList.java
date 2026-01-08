/**/
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.actor.instance.ManorManagerNpc;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RequestProcureCropList extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 16;
    private List<RequestProcureCropList.CropHolder> _items;

    protected void readImpl() {
        int count = this.readD();
        if (count > 0 && count <= Config.MAX_ITEM_IN_PACKET && count * 16 == this._buf.remaining()) {
            this._items = new ArrayList<>(count);

            for (int i = 0; i < count; ++i) {
                int objId = this.readD();
                int itemId = this.readD();
                int manorId = this.readD();
                int cnt = this.readD();
                if (objId < 1 || itemId < 1 || manorId < 0 || cnt < 0) {
                    this._items = null;
                    return;
                }

                this._items.add(new CropHolder(this, objId, itemId, cnt, manorId));
            }

        }
    }

    protected void runImpl() {
        if (this._items != null) {
            Player player = this.getClient().getPlayer();
            if (player != null) {
                CastleManorManager manor = CastleManorManager.getInstance();
                if (manor.isUnderMaintenance()) {
                    this.sendPacket(ActionFailed.STATIC_PACKET);
                } else {
                    Folk folk = player.getCurrentFolk();
                    if (folk instanceof ManorManagerNpc && folk.canInteract(player)) {
                        int castleId = folk.getCastle().getCastleId();
                        int slots = 0;
                        int weight = 0;
                        Iterator<CropHolder> var7 = this._items.iterator();

                        RequestProcureCropList.CropHolder i;
                        while (var7.hasNext()) {
                            i = var7.next();
                            ItemInstance item = player.getInventory().getItemByObjectId(i.getObjectId());
                            if (item != null && item.getCount() >= i.getValue() && item.getItemId() == i.getId()) {
                                CropProcure cp = i.getCropProcure();
                                if (cp != null && cp.getAmount() >= i.getValue()) {
                                    Item template = ItemTable.getInstance().getTemplate(i.getRewardId());
                                    weight += i.getValue() * template.getWeight();
                                    if (!template.isStackable()) {
                                        slots += i.getValue();
                                    } else if (player.getInventory().getItemByItemId(i.getRewardId()) == null) {
                                        ++slots;
                                    }
                                    continue;
                                }

                                this.sendPacket(ActionFailed.STATIC_PACKET);
                                return;
                            }

                            this.sendPacket(ActionFailed.STATIC_PACKET);
                            return;
                        }

                        if (!player.getInventory().validateWeight(weight)) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
                        } else if (!player.getInventory().validateCapacity(slots)) {
                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
                        } else {
                            var7 = this._items.iterator();

                            while (true) {
                                while (true) {
                                    int rewardPrice;
                                    do {
                                        if (!var7.hasNext()) {
                                            return;
                                        }

                                        i = var7.next();
                                        rewardPrice = ItemTable.getInstance().getTemplate(i.getRewardId()).getReferencePrice();
                                    } while (rewardPrice == 0);

                                    int rewardItemCount = i.getPrice() / rewardPrice;
                                    if (rewardItemCount < 1) {
                                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1).addItemName(i.getId()).addItemNumber(i.getValue()));
                                    } else {
                                        int fee = castleId == i.getManorId() ? 0 : (int) ((double) i.getPrice() * 0.05D);
                                        if (fee != 0 && player.getAdena() < fee) {
                                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1).addItemName(i.getId()).addItemNumber(i.getValue()));
                                            this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
                                        } else {
                                            CropProcure cp = i.getCropProcure();
                                            if (cp.decreaseAmount(i.getValue()) && (fee <= 0 || player.reduceAdena("Manor", fee, folk, true)) && player.destroyItem("Manor", i.getObjectId(), i.getValue(), folk, true)) {
                                                player.addItem("Manor", i.getRewardId(), rewardItemCount, folk, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        this.sendPacket(ActionFailed.STATIC_PACKET);
                    }
                }
            }
        }
    }

    private static final class CropHolder extends IntIntHolder {
        private final int _manorId;
        private final int _objectId;
        private CropProcure _cp;
        private int _rewardId = 0;

        public CropHolder(final RequestProcureCropList param1, int id, int count, int objectId, int manorId) {
            super(id, count);
            this._objectId = objectId;
            this._manorId = manorId;
        }

        public int getManorId() {
            return this._manorId;
        }

        public int getObjectId() {
            return this._objectId;
        }

        public int getPrice() {
            return this.getValue() * this._cp.getPrice();
        }

        public CropProcure getCropProcure() {
            if (this._cp == null) {
                this._cp = CastleManorManager.getInstance().getCropProcure(this._manorId, this.getId(), false);
            }

            return this._cp;
        }

        public int getRewardId() {
            if (this._rewardId == 0) {
                this._rewardId = CastleManorManager.getInstance().getSeedByCrop(this._cp.getId()).getReward(this._cp.getReward());
            }

            return this._rewardId;
        }
    }
}