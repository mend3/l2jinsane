package enginemods.main.util.builders.html;

public class Html {
    public static final String HTML_START = "<html><body>";

    public static final String HTML_END = "</body></html>";

    public static String newFontColor(String color, int text) {
        return newFontColor(color, "" + text);
    }

    public static String newFontColor(String color, String text) {
        return "<font color=" + color + ">" + text + "</font>";
    }

    public static String newImage(String image, int width, int height) {
        return "<img src=" + image + " width=" + width + " height=" + height + ">";
    }

    public static String headHtml(String name) {
        HtmlBuilder hb = new HtmlBuilder();
        hb.append("<center>");
        hb.append("<br>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table");
        hb.append("<tr>");
        hb.append("<td width=32><img src=L2UI_CH3.QuestBtn_light width=32 height=32></td>");
        hb.append("<td width=200><button value=\"", name, "\" action=\"\" width=200 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td><br>");
        hb.append("<td width=32><img src=L2UI_CH3.QuestBtn_light width=32 height=32></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("</center>");
        return hb.toString();
    }

    public static String htmlHeadCommunity(String name) {
        HtmlBuilder hb = new HtmlBuilder();
        hb.append("<center>");
        hb.append("<br>");
        hb.append("<img src=L2UI.SquareGray width=600 height=1>");
        hb.append("<table>");
        hb.append("<tr>");
        hb.append("<td width=32><img src=L2UI_CH3.QuestBtn_light width=32 height=32></td>");
        hb.append("<td width=536><button value=\"", name, "\" action=\"\" width=536 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
        hb.append("<td width=32><img src=L2UI_CH3.QuestBtn_light width=32 height=32></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=600 height=1>");
        hb.append("</center>");
        return hb.toString();
    }

    public static String formatAdena(int amount) {
        StringBuilder s = new StringBuilder();
        int rem = amount % 1000;
        s = new StringBuilder(Integer.toString(rem));
        amount = (amount - rem) / 1000;
        while (amount > 0) {
            if (rem < 99)
                s.insert(0, "0");
            if (rem < 9)
                s.insert(0, "0");
            rem = amount % 1000;
            s = new StringBuilder(rem + "," + rem);
            amount = (amount - rem) / 1000;
        }
        return s.toString();
    }
}
