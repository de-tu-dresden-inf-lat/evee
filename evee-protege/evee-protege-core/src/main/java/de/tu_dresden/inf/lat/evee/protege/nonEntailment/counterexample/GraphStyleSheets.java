package de.tu_dresden.inf.lat.evee.protege.nonEntailment.counterexample;

import java.util.HashMap;
import java.util.Map;

public class GraphStyleSheets {

    private static final String BLACK = "black;";
    private static final String WHITE = "white;";
    public static final String PROTEGE_BLUE = "#60B6C9;";
    public static final String PROTEGE_BLUE_1 = "#004956;";
    private static final String PROTEGE_BLUE_2 = "#01463f;";
    private static final String PROTEGE_YELLOW = "#7D6918;";
    private static final String PROTEGE_VIOLET = "#47485d;";
    private static final String PROTEGE_SOFT_VIOLET = "#dbdbdf;";
    private static final String SOFT_BLUE = "#475c6c;";
    private static final String SOFT_ORANGE = "#cd8b62;";
    private static final String SOFT_GRAY = "#8a8583;";
    private static final String SOFT_YELLOW = "#eed7a1;";
    private static final String RETRO_GREEN = "#1B9C85;";
    private static final String RETRO_ORANGE = "#FFCA47;";
    private static final String RETRO_GRAY = "#4C4C6D;";
    private static final String RETRO_WHITE = "#E8F6EF;";

    public static final String PLAIN = getStyleSheet(BLACK,
            BLACK,
            WHITE,
            BLACK,
            BLACK,
            BLACK);
    public static final String SOFT = getStyleSheet(SOFT_GRAY,
            SOFT_BLUE,
            SOFT_YELLOW,
            SOFT_GRAY,
            SOFT_ORANGE,
            SOFT_GRAY);
    public static final String RETRO = getStyleSheet(RETRO_GRAY,
            RETRO_GREEN,
            RETRO_WHITE,
            RETRO_GRAY,
            RETRO_ORANGE,
            RETRO_GRAY);
    public static final String PROTEGE = getStyleSheet(PROTEGE_BLUE_1,
            PROTEGE_BLUE,
            PROTEGE_SOFT_VIOLET,
            PROTEGE_VIOLET,
            PROTEGE_YELLOW,
            PROTEGE_VIOLET);
    public static   Map<String,String> styleMap;
    static {
        styleMap = new HashMap<>();
        styleMap.put("plain", PLAIN);
        styleMap.put("protege", PROTEGE);
        styleMap.put("soft", SOFT);
        styleMap.put("retro", RETRO);
    }
    public static String[] getStyleSheets() {

        return styleMap.keySet().toArray(new String[0]);
    }
    public static final String getStyleSheet(String edgeColor,
                                             String edgeLabelColor,
                                             String nodeFillColor,
                                             String nodeStrokeColor,
                                             String nodeLabelColor,
                                             String rootNodeColor) {

        return " edge {" +
//                "fill-color:" +edgeColor+
                "text-alignment: along;" +
//                "text-offset: -25, -25;" +
                "text-background-mode: plain;" +
//                "text-color:"+edgeLabelColor+
                "text-background-color:white;" +
                "text-size:13;" +
                "}" +
                "node {" +
                "text-offset: -25, -25;" +
                "text-background-mode: plain;" +
                "text-background-color: white;" +
                "text-size:15;" +
                "fill-color:"+nodeFillColor +
                "size: 20px;" +
                "stroke-mode: plain;" +
                "stroke-color:"+nodeStrokeColor +
                "}" +
                "sprite {" +
                "sprite-orientation:projection;"+
                "text-size:13;" +
//                "text-alignment:along;"+
                "text-background-mode: plain;" +
                "text-mode:normal;" +
                "text-color:"+nodeLabelColor+
                "text-offset: 0, 25;" +
                "fill-mode: none;" +
                "}" +

                "node.root {" +
                "fill-color:"+rootNodeColor +
                "}"
//                +
//        "sprite.edge {" +
//                "sprite-orientation:projection;"+
//                "text-size:13;" +
//                "text-background-mode: plain;" +
//                "text-mode:normal;" +
//                "text-color:"+edgeLabelColor+
//                "text-offset: 0, 25;" +
//                "fill-mode: none;" +
//                "}"
                ;
//        +
//                "sprite.selection {" +
//                "fill-color:rgba(0,0,0,0);" +
//                "stroke-color:rgba(0,0,0,0);" +
//                "fill-mode:plain;" +
//                "stroke-mode:plain;" +
//                "size:30px;" +
//                "}";
    }



//    public static final String PLAIN =
//            " edge {" +
//                    "text-alignment: along;" +
//                    "text-offset: -25, -25;" +
//                    "text-background-mode: plain;" +
//                    "text-background-color:" +
//                    "white;text-size:13;" +
//                    "}" +
//                    "node {" +
//                    "text-offset: -25, -25;" +
//                    "text-background-mode: plain;" +
//                    "text-background-color: white;" +
//                    "text-size:15;" +
//                    "fill-color: #FFFFFF;" +
//                    "size: 20px;" +
//                    "stroke-mode: plain;" +
//                    "stroke-color: #000000; " +
//                    "}" +
//                    "sprite {" +
//                    "text-size:13;" +
//                    "text-background-mode: plain;" +
//                    "text-mode:normal;" +
//                    "text-offset: 0, 25;" +
//                    "fill-mode: none;" +
//                    "}" +
//                    "node.root {" +
//                    "fill-color: #000000;" +
//                    "}" +
//                    "sprite.selection {" +
//                    "fill-color:rgba(0,0,0,0);" +
//                    "stroke-color:rgba(0,0,0,0);" +
//                    "fill-mode:plain;" +
//                    "stroke-mode:plain;" +
//                    "size:30px;" +
//                    "}";


}
