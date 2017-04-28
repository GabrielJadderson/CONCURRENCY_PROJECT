package endpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Gabriel Jadderson github.com/TheProthean
 */
public class Exam
{

    public interface Revoid
    {
        void doget(Path path);
    }

    //public static ExecutorService executorService = Executors.newCachedThreadPool();
    public static ExecutorService executorService;

    public static CountDownLatch countDownLatch = new CountDownLatch(1);
    public static DirectoryStream<Path> stream;

    private static final BlockingDeque<ResultObject> THE_LIST = new LinkedBlockingDeque<ResultObject>();
    private static final BlockingDeque<ResultObject> resultList = new LinkedBlockingDeque<>();
    //public static List<Result> resultList = Collections.synchronizedList(new ArrayList<>());

    /**
     * This method recursively visits a directory to find all the text files contained in it and its subdirectories.
     * <p>
     * You should consider only files ending with a .txt suffix. You are guaranteed that they will be text files.
     * <p>
     * You can assume that each text file contains a (non-empty) comma-separated sequence of
     * (positive) numbers. For example: 100,200,34,25
     * There won't be any new lines, spaces, etc., and the sequence never ends with a comma.
     * <p>
     * The search is recursive: if the directory contains subdirectories,
     * these are also searched and so on so forth (until there are no more
     * subdirectories).
     * <p>
     * This method returns a list of results. The list contains a result for each text file that you find.
     * Each {@link Result} stores the path of its text file, and the highest number (maximum) found inside of the text file.
     *
     * @param dir the directory to search
     * @return a list of results ({@link Result}), each giving the highest number found in a file
     */
    public static List<Result> findAll(Path dir)
    {

        return null;
    }

    public static void produceIntoList(BlockingDeque<ResultObject> list, Path initialPath)
    {
        try
        {
            Files.walk(initialPath).forEach((x) ->
            {
                if (x.getFileName().toString().contains(".txt"))
                {
                    ResultObject resultObject = new ResultObject(x, -1);
                    list.add(resultObject);
                }
            });

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("added " + list.size() + " to list.");
    }

    public static void producer(ExecutorService executor)
    {
        IntStream.range(0, 500).forEach(i ->
        {
            Future<ResultObject> future = executor.submit(() -> consume(THE_LIST.takeFirst()));
            try
            {
                ResultObject object = future.get();

                synchronized (resultList)
                {
                    resultList.add(object);
                }
                System.out.println(object.path() + " : " + object.number() + " SIZE OF LIST: " + resultList.size() + " blocking list:" + THE_LIST.size());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }


    private static ResultObject consume(ResultObject resultObject)
    {
        if (resultObject.number() == -1)
        {
            return new ResultObject(resultObject.path(), max(resultObject.path()));
        }
        return null;
    }


    public static final int NUM_PRODUCERS = 3;

    public static void run()
    {
        executorService = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(NUM_PRODUCERS);
        CountDownLatch puttingIntoList = new CountDownLatch(1);


        // Proposal 1: Before the consumer waits, it checks if something is in the list.
        // Proposal 2: Before the producer sends the signal, it checks if a consumer is waiting.

        new Thread(() ->
        {
            System.out.println("produce Into List thread started");
            produceIntoList(THE_LIST, Paths.get("C:\\Users\\mulli\\OneDrive\\Dokumenter\\IdeaProjects\\Concurrency_Project_final\\CONCURRENCY_PROJECT\\data_example"));
            puttingIntoList.countDown();
        }).start();

        IntStream.range(0, NUM_PRODUCERS).forEach(
                i ->
                {
                    new Thread(() ->
                    {
                        System.out.println("new Thread created");
                        producer(executorService);
                        latch.countDown();
                    }).start();
                });

        try
        {
            puttingIntoList.await();
            latch.await();
            executorService.shutdown();
            executorService.awaitTermination(1L, TimeUnit.DAYS);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }


    private static int max(Path file)
    {
        AtomicInteger total = new AtomicInteger(0);
        try (BufferedReader reader = Files.newBufferedReader(file))
        {
            reader.lines().forEach((line) ->
            {
                String[] numbers = line.split(",");
                for (String number : numbers)
                {
                    if (total.get() < Integer.parseInt(number))
                        total.set(Integer.parseInt(number));
                }
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return total.get();
    }


    public static boolean didFind = false;
    public static ResultObject query = null;

    /**
     * Finds a file that contains at most (no more than) n numbers and such that all
     * numbers in the file are equal or greater than min.
     * <p>
    * This method searches only for one (any) file in the directory
     * (parameter dir) such that the condition above is respected.
     * As soon as one such occurrence is found, the search can be
     * stopped and the method can return immediately.
     * <p>
     * As for method {@code findAll}, the search is recursive.
     */
    public static Result findAny(Path dir, int n, int min)
    {
        try
        {
            Stream stream = Files.walk(dir, 0, FileVisitOption.FOLLOW_LINKS);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Computes overall statistics about the occurrences of numbers in a directory.
     * <p>
     * This method recursively searches the directory for all numbers in all files and returns
     * a {@link Stats} object containing the statistics of interest. See the
     * documentation of {@link Stats}.
     */
    public static Stats stats(Path dir)
    {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args)
    {
        doAndMeasure("Executors", () -> run());
    }

    public static void doAndMeasure(String caption, Runnable runnable)
    {
        long tStart = System.currentTimeMillis();
        runnable.run();
        System.out.println(caption + " took " + (System.currentTimeMillis() - tStart) + "ms");
        THE_LIST.forEach((x) ->
        {
            System.out.println(x.path() + ": " + x.number());
        });
    }


}