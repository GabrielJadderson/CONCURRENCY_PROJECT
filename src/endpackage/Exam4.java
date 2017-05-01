package endpackage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by Gabriel Jadderson on 25/04/2017.
 */
public class Exam4
{

    private static final Deque<ResultObject> THE_LIST = new LinkedList<>();
    public static ResultObject resultObject = new ResultObject(null, -1);


    static AtomicBoolean isDone = new AtomicBoolean(false);
    public static boolean processAny(Path path, int n, int min)
    {
        try
        {
            int[] initialStream = Files.lines(path).parallel().map(x -> x.split(",")).flatMap(x -> Arrays.stream(x).parallel()).mapToInt(Integer::parseInt).toArray();
            int listSize = initialStream.length;
            IntStream ints = Arrays.stream(initialStream);
            int fileMin = ints.min().getAsInt();
            boolean found = listSize >= n && fileMin >= min;
            if (found)
                System.out.println("found n: " + n + " min: " + min + " file n: " + listSize + " file min: " + fileMin);
            else
                System.out.println("NOT found n: " + n + " min: " + min + " file n: " + listSize + " file min: " + fileMin);
            return found;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }



    public static void produce(Path path, Deque<ResultObject> list, ExecutorService executorService)
    {
        try
        {
            Files.walk(path)
                    .filter(x -> x.toString().endsWith(".txt"))
                    .forEach(x ->
                    {
                        executorService.submit(() -> list.add(new ResultObject(x, -1)));
                    });
            isDone.set(true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    static AtomicBoolean keepRun = new AtomicBoolean(true);
    public static void consume(Deque<ResultObject> list, CountDownLatch latch, AtomicInteger counter, int n, int min)
    {
        while (keepRun.get())
        {
            synchronized (list)
            {
                if (list.isEmpty())
                {
                    if (latch.getCount() == 0)
                    {
                        keepRun.set(false);
                    } else
                    {
                        try
                        {
                            list.wait();
                        } catch (InterruptedException e)
                        {
                        }
                        counter.incrementAndGet();
                    }
                } else
                {
                    if (latch.getCount() == 0)
                    {
                        keepRun.set(false);
                    } else
                    {
                        try
                        {
                            list.wait();
                        } catch (InterruptedException e)
                        {
                        }
                        counter.incrementAndGet();
                    }
                    ResultObject prod = list.removeFirst();
                    synchronized (resultObject)
                    {
                        if (processAny(prod.path(), n, min))
                        {
                            keepRun.set(false);
                            latch.countDown();
                            break;
                        } else
                        {
                            //consume(list, latch, counter, n, min);
                        }
                    }
                    //System.out.println("consumed a product " + prod.path());
                }
            }
        }
    }

    public static final int NUM_PRODUCERS = 4;

    public static void run(int n, int min)
    {
        AtomicInteger counter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Proposal 1: Before the consumer waits, it checks if something is in the list.
        // Proposal 2: Before the producer sends the signal, it checks if a consumer is waiting.

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(NUM_PRODUCERS);

        new Thread(() ->
        {
            produce(Paths.get("data_example"), THE_LIST, executor);
            latch.countDown();
        }).start();

        IntStream.range(0, NUM_PRODUCERS).forEach(
                i ->
                {
                    new Thread(() ->
                    {
                        consume(THE_LIST, latch, counter, n, min);
                        latch2.countDown();
                    }).start();
                });
        try
        {
            latch.await();
            synchronized (THE_LIST)
            {
                THE_LIST.notifyAll();
            }
            latch2.await();
        } catch (InterruptedException e)
        {
        }
        System.out.println("WASTED: " + counter.get());

    }

    public static void main(String[] args)
    {
        doAndMeasure("Executors", () -> run(65, 6));
    }

    public static void doAndMeasure(String caption, Runnable runnable)
    {
        long tStart = System.currentTimeMillis();
        runnable.run();
        System.out.println(caption + " took " + (System.currentTimeMillis() - tStart) + "ms");
    }
}
