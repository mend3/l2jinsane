package enginemods.main.util;

import net.sf.l2j.gameserver.model.WorldObject;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static final String SEPARATOR = "-----------------------------------------------------------";

    public static <A> boolean areObjectType(Class<A> type, WorldObject... objects) {
        if (objects == null || objects.length <= 0)
            return false;
        for (WorldObject o : objects) {
            if (!type.isAssignableFrom(o.getClass()))
                return false;
        }
        return true;
    }

    public static boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static List<Integer> parseInt(String line, String split) {
        List<Integer> list = new ArrayList<>();
        for (String s : line.split(split))
            list.add(Integer.valueOf(Integer.parseInt(s)));
        return list;
    }

    public static String getJsonVariable(String cadena, String variable) {
        cadena.replaceAll("\\}", "");
        String[] cadena_separada = cadena.split("\\{");
        for (String a : cadena_separada) {
            if (a.contains(variable)) {
                String[] a_separada = a.split(",");
                for (String b : a_separada) {
                    if (b.contains(variable)) {
                        String[] b_separada = b.split(":");
                        return b_separada[1].replaceAll("\"", "");
                    }
                }
            }
        }
        return "";
    }
}
