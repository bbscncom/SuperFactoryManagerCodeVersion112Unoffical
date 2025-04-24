package ca.teamdman.sfml.ast;

import java.util.Objects;

import static ca.teamdman.sfml.ast.ResourceQuantity.IdExpansionBehaviour;

public class Limit implements ASTNode {
    public static final Limit MAX_QUANTITY_NO_RETENTION = new Limit(
            new ResourceQuantity(new Number(Long.MAX_VALUE), ResourceQuantity.IdExpansionBehaviour.NO_EXPAND),
            new ResourceQuantity(new Number(0), ResourceQuantity.IdExpansionBehaviour.NO_EXPAND)
    );
    public static final Limit MAX_QUANTITY_MAX_RETENTION = new Limit(
            new ResourceQuantity(new Number(Long.MAX_VALUE), IdExpansionBehaviour.NO_EXPAND),
            new ResourceQuantity(new Number(Long.MAX_VALUE), IdExpansionBehaviour.NO_EXPAND)
    );
    public static final Limit UNSET = new Limit(
            ResourceQuantity.UNSET,
            ResourceQuantity.UNSET
    );

    private final ResourceQuantity quantity;
    private final ResourceQuantity retention;

    public Limit(ResourceQuantity quantity, ResourceQuantity retention) {
        this.quantity = quantity;
        this.retention = retention;
    }

    public ResourceQuantity quantity() {
        return quantity;
    }

    public ResourceQuantity retention() {
        return retention;
    }

    public Limit withDefaults(Limit limit) {
        if (quantity == ResourceQuantity.UNSET && retention == ResourceQuantity.UNSET) {
            return limit;
        } else if (quantity == ResourceQuantity.UNSET) {
            return new Limit(limit.quantity(), retention);
        } else if (retention == ResourceQuantity.UNSET) {
            return new Limit(quantity, limit.retention());
        }
        return this;
    }

    @Override
    public String toString() {
        return quantity + " RETAIN " + retention;
    }

    public String toStringCondensed(Limit defaults) {
        StringBuilder sb = new StringBuilder();
        if (!quantity.number().equals(defaults.quantity().number())) {
            sb.append(quantity);
        }
        if (!retention.number().equals(defaults.retention().number())) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("RETAIN ").append(retention);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Limit)) return false;
        Limit limit = (Limit) o;
        return Objects.equals(quantity, limit.quantity) &&
               Objects.equals(retention, limit.retention);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, retention);
    }
}
