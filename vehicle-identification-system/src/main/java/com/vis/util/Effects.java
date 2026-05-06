package com.vis.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Utility class for reusable JavaFX animations and visual effects.
 */
public class Effects {

    private Effects() {}

    /** Continuous fade in/out animation */
    public static FadeTransition createFadeLoop(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(1200), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);
        return fade;
    }

    public static FadeTransition fadeIn(Node node, double millis) {
        FadeTransition fade = new FadeTransition(Duration.millis(millis), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
        return fade;
    }

    /** One-shot fade-out */
    public static FadeTransition fadeOut(Node node, double millis) {
        FadeTransition fade = new FadeTransition(Duration.millis(millis), node);
        fade.setFromValue(node.getOpacity());
        fade.setToValue(0.0);
        fade.setOnFinished(e -> node.setVisible(false));
        fade.play();
        return fade;
    }

    /** Slide in from left */
    public static TranslateTransition slideInLeft(Node node, double millis) {
        node.setTranslateX(-60);
        TranslateTransition tt = new TranslateTransition(Duration.millis(millis), node);
        tt.setFromX(-60);
        tt.setToX(0);
        tt.play();
        return tt;
    }

    /** Scale-in "pop" entrance */
    public static ScaleTransition popIn(Node node) {
        node.setScaleX(0.7);
        node.setScaleY(0.7);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
        return st;
    }

    /** Apply DropShadow effect to a node */
    public static void applyDropShadow(Node node, Color color, double radius, double offsetX, double offsetY) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(radius);
        shadow.setOffsetX(offsetX);
        shadow.setOffsetY(offsetY);
        node.setEffect(shadow);
    }

    /** Pulse animation (scale up/down) */
    public static Timeline createPulse(Node node) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,      new KeyValue(node.scaleXProperty(), 1.0),
                                              new KeyValue(node.scaleYProperty(), 1.0)),
            new KeyFrame(Duration.millis(600), new KeyValue(node.scaleXProperty(), 1.05),
                                               new KeyValue(node.scaleYProperty(), 1.05)),
            new KeyFrame(Duration.millis(1200), new KeyValue(node.scaleXProperty(), 1.0),
                                                new KeyValue(node.scaleYProperty(), 1.0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    /** Parallel fade + slide combo */
    public static void fadeInAndSlide(Node node, double millis) {
        node.setOpacity(0);
        node.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
        ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(millis), node);
        tt.setToY(0);
        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.play();
    }

    /** Progress bar fill animation */
    public static Timeline animateProgress(javafx.scene.control.ProgressBar bar, double targetValue) {
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,         new KeyValue(bar.progressProperty(), 0)),
            new KeyFrame(Duration.millis(1500),  new KeyValue(bar.progressProperty(), targetValue))
        );
        tl.play();
        return tl;
    }
}
