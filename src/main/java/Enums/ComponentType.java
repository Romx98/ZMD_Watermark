package Enums;

/**
 * Enum for component type.
 */
public enum ComponentType {
    Y("Y"),
    CB("Cb"),
    CR("Cr");

    final String type;

    ComponentType(String type) {
        this.type = type;
    }
}
