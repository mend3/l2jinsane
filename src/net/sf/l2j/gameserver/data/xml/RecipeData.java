/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.item.Recipe;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RecipeData implements IXmlReader {
    private final Map<Integer, Recipe> _recipes = new HashMap<>();

    protected RecipeData() {
    }

    public static RecipeData getInstance() {
        return RecipeData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/recipes.xml");
        LOGGER.info("Loaded {} recipes.", this._recipes.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "recipe", (recipeNode) -> {
                StatSet set = this.parseAttributes(recipeNode);
                this._recipes.put(set.getInteger("id"), new Recipe(set));
            });
        });
    }

    public Recipe getRecipeList(int listId) {
        return this._recipes.get(listId);
    }

    public Recipe getRecipeByItemId(int itemId) {
        return this._recipes.values().stream().filter((r) -> {
            return r.getRecipeId() == itemId;
        }).findFirst().orElse(null);
    }

    private static class SingletonHolder {
        protected static final RecipeData INSTANCE = new RecipeData();
    }
}