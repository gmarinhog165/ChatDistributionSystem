package pt.uminho.di;

import java.io.Serializable;
import java.util.Objects;

public class Dot implements Serializable {
    private static final long serialVersionUID = 1L;

    private String server;
    private int value;

    public Dot(String server, int value) {
        this.server = server;
        this.value = value;
    }

    public String getServer() {
        return server;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Dot)) return false;
        Dot other = (Dot) obj;
        return value == other.value && Objects.equals(server, other.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, value);
    }

    @Override
    public String toString() {
        return "(" + server + ", " + value + ")";
    }
}

