/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class ScriptData implements IXmlReader, Runnable {
    public static final int PERIOD = 300000;
    private final List<Quest> _quests = new ArrayList<>();
    private final List<ScheduledQuest> _scheduled = new LinkedList<>();

    public ScriptData() {
    }

    public static ScriptData getInstance() {
        return ScriptData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/scripts.xml");
        LOGGER.info("Loaded {} regular scripts and {} scheduled scripts.", this._quests.size(), this._scheduled.size());
        ThreadPool.scheduleAtFixedRate(this, 0L, 300000L);
    }

    public void parseDocument(Document doc, Path p) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "script", (scriptNode) -> {
                NamedNodeMap params = scriptNode.getAttributes();
                String path = this.parseString(params, "path");
                if (path == null) {
                    LOGGER.warn("One of the script path isn't defined.");
                } else {
                    try {
                        Quest instance = (Quest) Class.forName("net.sf.l2j.gameserver.scripting." + path).getDeclaredConstructor().newInstance();
                        this._quests.add(instance);
                        if (instance instanceof ScheduledQuest) {
                            String type = this.parseString(params, "schedule");
                            if (type == null) {
                                return;
                            }

                            String start = this.parseString(params, "start");
                            if (start == null) {
                                LOGGER.warn("Missing 'start' parameter for scheduled script '{}'.", path);
                                return;
                            }

                            String end = this.parseString(params, "end");
                            if (((ScheduledQuest) instance).setSchedule(type, start, end)) {
                                this._scheduled.add((ScheduledQuest) instance);
                            }
                        }
                    } catch (Exception var8) {
                        LOGGER.error("Script '{}' is missing.", var8, path);
                    }

                }
            });
        });
    }

    public void run() {
        long next = System.currentTimeMillis() + 300000L;

        for (ScheduledQuest script : this._scheduled) {
            long eta = next - script.getTimeNext();
            if (eta > 0L) {
                ThreadPool.schedule(new Scheduler(this, script), 300000L - eta);
            }
        }

    }

    public Quest getQuest(String questName) {
        return this._quests.stream().filter((q) -> {
            return q.getName().equalsIgnoreCase(questName);
        }).findFirst().orElse(null);
    }

    public Quest getQuest(int questId) {
        return this._quests.stream().filter((q) -> {
            return q.getQuestId() == questId;
        }).findFirst().orElse(null);
    }

    public List<Quest> getQuests() {
        return this._quests;
    }

    private static class SingletonHolder {
        protected static final ScriptData INSTANCE = new ScriptData();
    }

    private static final class Scheduler implements Runnable {
        private final ScheduledQuest _script;

        private Scheduler(final ScriptData param1, ScheduledQuest script) {
            this._script = script;
        }

        public void run() {
            this._script.notifyAndSchedule();
            long eta = System.currentTimeMillis() + 300000L - this._script.getTimeNext();
            if (eta > 0L) {
                ThreadPool.schedule(this, 300000L - eta);
            }

        }
    }
}