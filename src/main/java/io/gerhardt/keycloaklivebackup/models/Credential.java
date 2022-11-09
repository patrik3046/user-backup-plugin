package io.gerhardt.keycloaklivebackup.models;

public class Credential {
    private final int hashIterations;
    private final String algorithm;
    private final String type;
    private final String salt;
    private final String value;

    public Credential(int hashIterations, String algorithm, String type, String salt, String value) {
        this.hashIterations = hashIterations;
        this.algorithm = algorithm;
        this.type = type;
        this.salt = salt;
        this.value = value;
    }

    public int getHashIterations() {
        return hashIterations;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getSalt() {
        return salt;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
