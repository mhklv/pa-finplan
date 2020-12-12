package org.mchklv.finplan.client;

import javafx.scene.Node;
import javafx.stage.Screen;

public class DPICustomUtils {
    public static double fromPtToPx(double pt) {
        double dpi = Screen.getPrimary().getDpi();
        double px = pt * dpi / 72;
        return px;
    }
    
    public static double screenWidthFractionToPx(double screenFraction) {
        return Screen.getPrimary().getBounds().getWidth() * screenFraction;
    }

    public static double screenHeightFractionToPx(double screenFraction) {
        return Screen.getPrimary().getBounds().getHeight() * screenFraction;
    }

    public static void setNodeFontSize(Node node, double fontSizePx) {
        node.setStyle("-fx-font-size: ?px;".replace("?", String.format("%.2f", fontSizePx)));
    }

}
