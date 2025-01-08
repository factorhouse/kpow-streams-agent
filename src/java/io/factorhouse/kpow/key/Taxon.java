package io.factorhouse.kpow.key;

/**
 * Taxon is an internal class built to resolve the IDs of the KeyStrategy.
 */
class Taxon {

    private final String domain;
    private final String domainId;
    private final String object;
    private final String objectId;

    public Taxon(
        String domain,
        String domainId,
        String object,
        String objectId
    ) {
        this.domainId = domainId;
        this.domain = domain;
        this.object = object;
        this.objectId = objectId;
    }

    /**
     * Returns the domain of the Taxon.
     *
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns the object of the Taxon.
     *
     * @return the object
     */
    public String getObject() {
        return object;
    }

    /**
     * Returns the object ID of the Taxon.
     *
     * @return the object ID
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Returns the domain ID of the Taxon.
     *
     * @return the domain ID
     */
    public String getDomainId() {
        return domainId;
    }
}
