package org.molgenis.vibe.formats;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public abstract class BiologicalEntityCombination<T1 extends BiologicalEntity, T2 extends BiologicalEntity> {
    private T1 t1;

    private T2 t2;

    public T1 getT1() {
        return t1;
    }

    public T2 getT2() {
        return t2;
    }

    public BiologicalEntityCombination(T1 t1, T2 t2) {
        this.t1 = requireNonNull(t1);
        this.t2 = requireNonNull(t2);
    }

    @Override
    public String toString() {
        return "BiologicalEntityCombination{" +
                "t1=" + t1 +
                ", t2=" + t2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiologicalEntityCombination<?, ?> that = (BiologicalEntityCombination<?, ?>) o;
        return Objects.equals(t1, that.t1) &&
                Objects.equals(t2, that.t2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }
}