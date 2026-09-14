package guessmarket.ui.javafx.components;

import guessmarket.dto.EventStatusDto;
import javafx.scene.control.Label;

import java.util.List;

public abstract class StatusStyle {
    private static final List<String> STYLE_CLASSES =
            List.of("status-not-started", "status-active", "status-closed");

    public static void apply(Label badge, EventStatusDto status) {
        clear(badge);
        badge.getStyleClass().add(switch (status) {
            case ACTIVE -> "status-active";
            case CLOSED -> "status-closed";
            case NOT_STARTED -> "status-not-started";
        });
    }

    public static void clear(Label badge) {
        badge.getStyleClass().removeAll(STYLE_CLASSES);
    }
}
