import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.min;

public class Main {
    public static void main(String[] args) {

        List<String> places = Arrays.asList("Buenos Aires", "Córdoba", "La Plata", "A", "B", "C", "D", "E", "F", "G");
        final Stream<List<String>> chunk = chunk(places, 4);
        final List<List<String>> collect = chunk.collect(Collectors.toList());

        List<String> list = Arrays.asList("A", "B", "C", "D", "E", "F", "G");
        final Stream<String> concat = concat(list, "A", "B", "C", "D", "E", "F", "G");
        final List<String> collect2 = concat.collect(Collectors.toList());

    }

    // Creates an array of elements split into groups the length of size.
    // If array can't be split evenly, th
    // e final chunk will be the remaining elements.
    private static <T> Stream<List<T>> chunk(List<T> list, int size) {
        final int numberOfGroups = (int) Math.ceil((list.size() * 1.0) / size);
        final IntStream range = IntStream.range(0, numberOfGroups);
        return range.mapToObj(x -> list.subList(x * size, min((x + 1) * size, list.size())));
    }

    // Creates a new array concatenating array with any additional arrays and/or values..
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

    // Return an array of values not included in the other given arrays. The order and references of result values are determined by the first array.
    private static <T> Stream<T> difference(List<T> original, List<T>... lists) {
        //if original is empty we waiste time on the following list build
        final List<T> allOtherElements = Arrays.stream(lists)
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());

        return Stream.of(allOtherElements)
                .flatMap(list -> original.stream().filter(element ->
                        list.stream().anyMatch(e -> element == e)));
    }

    // Creates an array of unique values that are included in all given arrays. The order and references of result values are determined by the first array..
    private static <T> Stream<T> intersection(List<T> source, List<T>... lists) {
        return source.stream()
                .filter(element -> Arrays.stream(lists).allMatch(list -> list.contains(element)));
    }

}
