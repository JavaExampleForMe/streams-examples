package streams.examples;


@FunctionalInterface
interface TriPredicate<A,B,C> {

    boolean test(A a, B b, C c);
}