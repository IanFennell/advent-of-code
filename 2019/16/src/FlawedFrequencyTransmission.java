import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.lang.Math.floor;

public class FlawedFrequencyTransmission {
    final static int[] PATTERN = {0, 1, 0, -1};
    static int LEN = 0;
    private final static int THREADS = 16;
    private final static int REPEAT = 10000;
    static int[][] signals;
    private static ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
    private final static List<Callable<Object>> queue = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        List<Integer> base = Arrays.stream(Files.readString(Paths.get("input.txt"))
                .split(""))
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(ArrayList::new));

        int offset = 0;


        int[] input = new int[base.size()*REPEAT];

        for (int i = 0; i < base.size()*REPEAT; i++) {
            input[i] = base.get(i%base.size());
        }

        LEN = input.length;
        int batchSize = (int) Math.floor(LEN / THREADS);

        batchSize = batchSize != 0 ? batchSize : 1;

        final int MAX = 100;
        signals = new int[MAX+1][LEN];

        signals[0] = input;

        long s = System.currentTimeMillis();

        for (int c = 0; c < MAX; c++) {
            System.out.println(c+1);
            // Batch sets of chars to calculate
            for (int i = 0; i < LEN; i+= batchSize) {
                int end = i+ batchSize;
                if ((LEN - i) < batchSize) {
                    end = LEN;
                }
                queue.add(Executors.callable(new FFTThread(i, end, c)));
            }
            executorService.invokeAll(queue);
        }

        System.out.println(String.format("%dms", System.currentTimeMillis() - s));

        List<Integer> last = new ArrayList<>();
        for (int i = 0; i < signals[MAX].length; i++) {
            last.add(signals[MAX][i]);
        }

        System.out.println(last.stream().map(String::valueOf).collect(Collectors.joining("")).substring(0, 8));
    }

}
