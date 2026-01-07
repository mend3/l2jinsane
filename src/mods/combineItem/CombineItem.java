package mods.combineItem;

import net.sf.l2j.gameserver.data.CombineDataXML;

import java.util.ArrayList;
import java.util.List;

public final class CombineItem {
    private static final List<CombineEntry> _combinations = new ArrayList<>();

    private static ArrayList<CombineDataXML.CombineRecipe> _recipes;

    public static void RellenarCombineEntry() {
        if (_recipes.isEmpty() || _recipes == null)
            return;
        try {
            for (CombineDataXML.CombineRecipe r : _recipes)
                _combinations.add(new CombineEntry(r.item1(), r.count1(), r.item2(), r.count2(), r.result(), r.countResult(), r.item3(), r.count3(), r.chance()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<CombineEntry> getCombinations() {
        return _combinations;
    }

    public static CombineItem getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        _recipes = CombineDataXML.getInstance().getRecipesAsList();
        RellenarCombineEntry();
    }

    private static class SingletonHolder {
        private static final CombineItem INSTANCE = new CombineItem();
    }
}
