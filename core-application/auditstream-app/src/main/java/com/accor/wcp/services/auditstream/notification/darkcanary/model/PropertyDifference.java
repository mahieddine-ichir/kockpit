package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import lombok.Data;

@Data
public class PropertyDifference {

    private String propertyName;

    private String leftValue;

    private String rightValue;

    public void setLeftValue(Object leftValue) {
        if (leftValue != null) {
            this.leftValue = leftValue.toString();
        }
    }

    public void setRightValue(Object rightValue) {
        if (rightValue != null) {
            this.rightValue = rightValue.toString();
        }
    }

    public static PropertyDifference of(String propertyName) {
        PropertyDifference propertyDifference = new PropertyDifference();
        propertyDifference.setPropertyName(propertyName);
        return propertyDifference;
    }
}
