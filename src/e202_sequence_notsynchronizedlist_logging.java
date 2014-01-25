import java.util.ArrayList;
import java.util.List;

/**
 * A very contrived yet simple example of a Sequence class producing increasing
 * natural numbers with each nextNumber() call. Two Worker threads get the
 * numbers, sharing the same instance, and add them to a shared List. After
 * they've added a certain number of numbers, the main object checks if all the
 * numbers in the list are increasing uniformly.
 *
 * This basic version doesn't use any synchronization on the Sequence objects
 * intentionally to demonstrate that it is very easy to get the wrong order in
 * the results list.
 *
 * Created by u on 2014-01-25.
 */
public class e202_sequence_notsynchronizedlist_logging {
    static class Sequence {
        private Long theNumber = -1L;

        public Long nextNumber() {
            return ++theNumber;
        }
    }

    static class Worker implements Runnable {
        private final Sequence sequence;

        public Worker(Sequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public void run() {
            for (int i = 0; i < 50; ++i) {
                final Long number = sequence.nextNumber();
                System.out.println(number);
                addNumber(number);
            }
        }
    }

    private static final long CORRECT_INDEXES = -1L;

    private static List<Long> numbers = new ArrayList<Long>();

    static void addNumber(Long number) {
        numbers.add(number);
    }

    private static Long firstIncorrectNumberIndex() {
        long index = 0;
        for (Long number : numbers) {
            if (index != number) {
                return index;
            }

            ++index;
        }

        return CORRECT_INDEXES;
    }

    private static void processResults() {
        System.out.println("Result: " + numbers);
        final Long incorrect = firstIncorrectNumberIndex();
        if (CORRECT_INDEXES != incorrect) {
            System.out.println(
                    "First incorrect number @index " + incorrect + ": "
                            + numbers.get(incorrect.intValue()));
        } else {
            System.out.println("OK");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        final Sequence sequence = new Sequence();
        final Thread worker0 = new Thread(new Worker(sequence), "Worker0");
        final Thread worker1 = new Thread(new Worker(sequence), "Worker1");

        worker0.start();
        worker1.start();

        worker0.join();
        worker1.join();

        processResults();

        long finish = System.currentTimeMillis();
        System.out.println("\nDone in " + (finish - start) + " ms.");
    }
}
