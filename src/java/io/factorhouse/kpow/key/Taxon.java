package io.factorhouse.kpow.key;

public class Taxon {
    private final String domain;
    private final String domainId;
    private final String object;
    private final String objectId;

    public Taxon(String domain, String domainId, String object, String objectId) {
        this.domainId = domainId;
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

    public String getDomainId() {
        return domainId;
    }
}
