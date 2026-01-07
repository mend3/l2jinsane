package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.ArrayList;
import java.util.List;

public abstract class SpawnZoneType extends ZoneType {
    private List<Location> _locs = null;

    private List<Location> _chaoticLocs = null;

    public SpawnZoneType(int id) {
        super(id);
    }

    public final void addLoc(Location loc, boolean isChaotic) {
        if (isChaotic) {
            if (this._chaoticLocs == null)
                this._chaoticLocs = new ArrayList<>();
            this._chaoticLocs.add(loc);
        } else {
            if (this._locs == null)
                this._locs = new ArrayList<>();
            this._locs.add(loc);
        }
    }

    public final List<Location> getLocs() {
        return this._locs;
    }

    public final Location getRandomLoc() {
        return Rnd.get(this._locs);
    }

    public final Location getRandomChaoticLoc() {
        return Rnd.get((this._chaoticLocs != null) ? this._chaoticLocs : this._locs);
    }
}
