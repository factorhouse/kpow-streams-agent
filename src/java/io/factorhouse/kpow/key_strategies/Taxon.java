package io.factorhouse.kpow.key_strategies;

public class Taxon {
    private final String domain;
    private final String id;

    private final String object;
    private final String objectId;

    public Taxon(String domain, String id, String object, String objectId) {
        this.id = id;
        this.domain = domain;
        this.object = object;
        this.objectId = objectId;
    }

    public String getDomain() {
        return domain;
    }

    public String getObject() {
        return object;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getId() {
        return id;
    }
}
