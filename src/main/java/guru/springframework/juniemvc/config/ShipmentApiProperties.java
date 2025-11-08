package guru.springframework.juniemvc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shipment.api")
public class ShipmentApiProperties {
    /**
     * If true, GET by id will also enforce that the shipment belongs to the orderId in the URL.
     */
    private boolean enforceOrderMatchOnGet = true;

    public boolean isEnforceOrderMatchOnGet() {
        return enforceOrderMatchOnGet;
    }

    public void setEnforceOrderMatchOnGet(boolean enforceOrderMatchOnGet) {
        this.enforceOrderMatchOnGet = enforceOrderMatchOnGet;
    }
}