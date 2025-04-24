// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package my.datafixers.kinds;

import my.datafixers.kinds.App;
import my.datafixers.kinds.Functor;
import my.datafixers.kinds.K1;

import java.util.function.Function;

public interface Traversable<T extends K1, Mu extends Traversable.Mu> extends Functor<T, Mu> {
    static <F extends K1, Mu extends Traversable.Mu & K1> Traversable<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Traversable<F, Mu>) proofBox;
    }

    interface Mu extends Functor.Mu {}

    <F extends K1, A, B> App<F, App<T, B>> traverse(final Applicative<F, ?> applicative, final Function<A, App<F, B>> function, final App<T, A> input);

    default <F extends K1, A> App<F, App<T, A>> flip(final Applicative<F, ?> applicative, final App<T, App<F, A>> input) {
        return traverse(applicative, Function.identity(), input);
    }
}
