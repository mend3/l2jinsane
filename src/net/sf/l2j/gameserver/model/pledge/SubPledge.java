package net.sf.l2j.gameserver.model.pledge;

public class SubPledge {
    private final int _id;

    private String _subPledgeName;

    private int _leaderId;

    public SubPledge(int id, String name, int leaderId) {
        this._id = id;
        this._subPledgeName = name;
        this._leaderId = leaderId;
    }

    public int getId() {
        return this._id;
    }

    public String getName() {
        return this._subPledgeName;
    }

    public void setName(String name) {
        this._subPledgeName = name;
    }

    public int getLeaderId() {
        return this._leaderId;
    }

    public void setLeaderId(int leaderId) {
        this._leaderId = leaderId;
    }
}
