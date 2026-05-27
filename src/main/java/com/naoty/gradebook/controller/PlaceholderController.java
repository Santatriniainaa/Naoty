package com.naoty.gradebook.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PlaceholderController {

    @FXML private Label titleLabel;
    @FXML private Label descLabel;

    public void setContent(String title, String description) {
        titleLabel.setText(title);
        descLabel.setText(description);
    }
}
