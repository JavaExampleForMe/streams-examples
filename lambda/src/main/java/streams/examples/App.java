package main.java.streams.examples;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;

public class App {

    static class User {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
    public static void main(String[] args) {

        List<String> places = Arrays.asList("Buenos Aires", "Córdoba", "La Plata", "A", "B", "C", "D", "E", "F", "G");
        final Stream<List<String>> chunk = chunk(places, 4);
        final List<List<String>> collect = chunk.collect(Collectors.toList());

        List<String> list = Arrays.asList("A", "B", "C", "D", "E", "F", "G");
        final Stream<String> concat = concat(list, "A", "B", "C", "D", "E", "F", "G");
        final List<String> collect2 = concat.collect(Collectors.toList());

        ///////////////  findLastIndex   /////////////////////
        Predicate<User> containsA = (u) ->
        {
            return u.getName().toLowerCase().contains("a");
        };
        List<User> users = Arrays.asList(new User(1,"abc"), new User(2,"BbAcd"), new User(3,"bYr"));

        final boolean abc = containsA.test(new User(1, "abc"));
        final OptionalInt lastIndex = findLastIndex(users, containsA, 1);

        ///////////////  dropRightWhile   /////////////////////
        List<User> users2 = Arrays.asList(new User(3,"bYr"), new User(1,"abc"), new User(2,"BbAcd"));
        final Stream<User> userStream = dropRightWhile(users2, containsA);

        TriPredicate<User, Integer, List<User>> myTest = (u, i, l) ->
        {
            return u.getName().toLowerCase().equals(l.get(i).name);
        };
    }

    /**
     * Creates an array of elements split into groups the length of size.
     * If array can't be split evenly,
     * the final chunk will be the remaining elements.
     * @param list : The list to process.
     * @param size :The length of each chunk.
     * @param <T>
     * @return : Returns the new array of chunks.
     * chunk(['a', 'b', 'c', 'd'], 2);
     * // => [['a', 'b'], ['c', 'd']]
     *
     * chunk(['a', 'b', 'c', 'd'], 3);
     * // => [['a', 'b', 'c'], ['d']]
     */
    private static <T> Stream<List<T>> chunk(List<T> list, int size) {
        final int numberOfGroups = (int) Math.ceil((list.size() * 1.0) / size);
        final IntStream range = IntStream.range(0, numberOfGroups);
        return range.mapToObj(x -> list.subList(x * size, min((x + 1) * size, list.size())));
    }

    /**
     * Creates a new array concatenating array with any additional arrays and/or values.
     * @param list : The list to concatenate.
     * @param args : The values to concatenate.
     * @return : Returns the new concatenated stream.
    var array = [1];
    var other = concat(array, 2, [3], [[4]]);

    console.log(other);
    // => [1, 2, [3], [[4]]]

    console.log(array);
    // => [1]
     */
    private static <T> Stream<T> concat(List<T> list, T... args) {
        return Stream.concat(list.stream(), Arrays.stream(args));
    }

    // Return an array of values not included in the other given arrays. The order and references of result values are determined by the first array.
    private static <T> Stream<T> difference2(List<T> original, List<T>... lists) {
        final Stream<T> tStream = Arrays.stream(lists).flatMap(list -> list.stream());
        return original.stream().filter(element -> {
            return Arrays.stream(lists).flatMap(list -> list.stream()).anyMatch(e -> element == e);
        });
    }

    // Return an array of values not included in the other given arrays. The order and references of result values are determined by the first array.
    private static <T> Stream<T> difference3(List<T> original, List<T>... lists) {
        final Stream<T> allOtherElements = Arrays.stream(lists)
                .flatMap(list -> list.stream());

        return Stream.of(allOtherElements)
                .flatMap(streamOfAllElements -> original.stream().filter(element ->
                        // map creates a new stream so we call anyMath on new stream (streamOfAllElements.map(Function.identity())) in each filter iteration.
                        // if we do not map, since anyMatch is a terminal action, the stream (streamOfAllElements) is unused any more
                        // we recreate the stream allOtherElements again and again for any elemnt in the filter
                        streamOfAllElements.map(Function.identity()).anyMatch(e -> element == e)));
    }

    /**
     * Return an array of values not included in the other given arrays. The order and references of result values are determined by the first array.
     * @param original : The array to inspect.
     * @param lists :  The values to exclude.
     * @return : Returns the new array of filtered values.
     * difference([2, 1], [2, 3]);
     * // => [1]
     */
    private static <T> Stream<T> difference(List<T> original, List<T>... lists) {
        //if original is empty we waist time on the following list build
        final List<T> allOtherElements = Arrays.stream(lists)
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());

        return Stream.of(allOtherElements)
                .flatMap(list -> original.stream().filter(element ->
                        list.stream().anyMatch(e -> element == e)));
    }

    /**
    * Creates an array of unique values that are included in all given arrays. The order and references of result values are determined by the first array.
    * @param source
    * @param lists -  The lists to inspect.
    * @return  Returns the new list of intersecting values.
     * intersection([2, 1], [2, 3]);
     * // => [2]
    */
    private static <T> Stream<T> intersection(List<T> source, List<T>... lists) {
        return source.stream()
                .filter(element -> Arrays.stream(lists).allMatch(list -> list.contains(element)));
    }

    /**
     * Return the first index i from right to left such that both conditions fulfilled:
     * 1.predicate(list.at(i)) == true
     * 2.0 <= i <= fromIndex.
      * @param list :array (Array): The array to inspect.
     * @param predicate : (Function): The function invoked per iteration.
     * @param fromIndex : [fromIndex=array.length-1] (number): The index to search from.
      * @return Returns the index of the found element.
     */
    private static <T> OptionalInt findLastIndex(List<T> list, Predicate<T> predicate, int fromIndex) {
        ;
        final IntStream range = IntStream.range((list.size()-1)* -1, (fromIndex* -1)+1);
//        range.boxed().collect(Collectors.toList());
        return range
                .map(u -> u * -1)
 //               .peek(b -> System.out.println("before filter: - " + b))
                .filter(index -> predicate.test(list.get(index)))
 //               .peek(n -> System.out.println("after filter: - " + n))
                .findFirst();
    }

    /**
     * Creates a slice of array excluding elements dropped from the end. Elements are dropped until predicate returns false.
     * The predicate is invoked with three arguments: (value, index, array).
     * @param list :The array to inspect.
     * @param predicate : (Function): The function invoked per iteration.
     * @return  Returns the slice of list.
     */
    private static <T> Stream<T> dropRightWhile(List<T> list, Predicate<T> predicate) {
        final IntStream range = IntStream.range((list.size()-1)* -1, 1);
//        range.boxed().collect(Collectors.toList());
        return range
                .map(u -> u * -1)
                .peek(b -> System.out.println("before filter: - " + b))
                .filter(index -> !predicate.test(list.get(index)))
                .peek(n -> System.out.println("after filter: - " + n))
                .mapToObj(i->list.get(i));
    }
}
