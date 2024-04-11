package net.jchad.server.model.cryptography.tagUnit;

public enum Tag {
    LENGTH_96(96),
    LENGTH_104(104),
    LENGTH_112(112),
    LENGTH_120(120),
    LENGTH_128(128),
    DEFAULT(LENGTH_128.getValue());

    public final int value;

    private Tag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}