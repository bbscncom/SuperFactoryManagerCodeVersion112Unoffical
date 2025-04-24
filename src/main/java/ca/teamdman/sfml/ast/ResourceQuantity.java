package ca.teamdman.sfml.ast;

import java.util.Objects;

public class ResourceQuantity implements ASTNode {
    @SuppressWarnings("DataFlowIssue")
    public static final ResourceQuantity UNSET = new ResourceQuantity(null, IdExpansionBehaviour.NO_EXPAND);
    public static final ResourceQuantity MAX_QUANTITY = new ResourceQuantity(
            new Number(Long.MAX_VALUE),
            IdExpansionBehaviour.NO_EXPAND
    );

    private final Number number;
    private final IdExpansionBehaviour idExpansionBehaviour;

    public ResourceQuantity(Number number, IdExpansionBehaviour idExpansionBehaviour) {
        this.number = number;
        this.idExpansionBehaviour = idExpansionBehaviour;
    }

    public Number number() {
        return number;
    }

    public IdExpansionBehaviour idExpansionBehaviour() {
        return idExpansionBehaviour;
    }

    public ResourceQuantity add(ResourceQuantity quantity) {
        return new ResourceQuantity(
                number.add(quantity.number),
                idExpansionBehaviour
        );
    }

    public enum IdExpansionBehaviour {
        EXPAND,
        NO_EXPAND
    }

    @Override
    public String toString() {
        return (this == UNSET ? "UNSET" : number) + (idExpansionBehaviour == IdExpansionBehaviour.EXPAND ? " EACH" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceQuantity)) return false;
        ResourceQuantity that = (ResourceQuantity) o;
        return Objects.equals(number, that.number) &&
               idExpansionBehaviour == that.idExpansionBehaviour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, idExpansionBehaviour);
    }
}
