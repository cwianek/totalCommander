package sample;

import javafx.scene.layout.FlowPane;

public class CustomFlowPane extends FlowPane {
    @Override
    protected double computePrefHeight(double forWidth) {
        this.setHeight(500);
        return super.computePrefHeight(forWidth);
    }
}
