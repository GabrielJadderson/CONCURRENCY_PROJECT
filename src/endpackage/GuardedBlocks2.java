package endpackage;

import endpackage.FinalPackage.ResultObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class GuardedBlocks2
{

    private static final Deque<Path> THE_LIST = new LinkedList<>();
    private static final Deque<Path> RESULT_LIST = new LinkedList<>();

    private static void produce(Path startDir, Deque<Path> list)
    {
        try
        {
            Files.walk(startDir)
                    .filter(x -> x.toString().endsWith(".txt"))
                    .forEach(x ->
                    {
                        synchronized (list)
                        {
                            list.add(x);
                            list.notify();
                        }
                    });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static ResultObject resultObject = new ResultObject(null, -1);
    private final static AtomicInteger counter = new AtomicInteger(0);

    private static boolean consume(Deque<Path> list, CountDownLatch latch, int n, int min)
    {
        boolean keepRun = true;
        while (keepRun)
        {
            synchronized (list)
            {
                if (list.isEmpty())
                {
                    if (latch.getCount() == 0)
                    {
                        keepRun = false;
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
                    Path prod = list.removeFirst();
                    if (isQualified(prod, n, min))
                    {
                        synchronized (resultObject)
                        {
                            resultObject = new ResultObject(prod.toAbsolutePath(), 0);
                            latch.countDown();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public static boolean isQualified(Path path, int n, int min)
    {
        try (FileReader fileReader = new FileReader(path.toFile()); BufferedReader bufferedReader = new BufferedReader(fileReader))
        {
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] numbers = line.split(",");
                if (numbers.length >= n)
                    return false;
                else
                    for (String number : numbers)
                        if (Integer.parseInt(number) <= min)
                            return true;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private static final int NUM_CONSUMERS = 2;

    public static void run(int n, int min)
    {
        // Proposal 1: Before the consumer waits, it checks if something is in the list.
        // Proposal 2: Before the producer sends the signal, it checks if a consumer is waiting.

        CountDownLatch productionLatch = new CountDownLatch(1);
        CountDownLatch consumerLatch = new CountDownLatch(NUM_CONSUMERS);

        new Thread(() ->
        {
            produce(Paths.get("data_example"), THE_LIST);
            productionLatch.countDown();
        }).start();

        IntStream.range(0, NUM_CONSUMERS).forEach(
                i ->
                {
                    new Thread(() ->
                    {
                        consume(THE_LIST, productionLatch, n, min);
                        consumerLatch.countDown();
                    }).start();
                });
        try
        {
            productionLatch.await();
            synchronized (THE_LIST)
            {
                THE_LIST.notifyAll();
            }
            consumerLatch.await();
            if (resultObject != null)
                System.out.println(resultObject.path());
        } catch (InterruptedException e)
        {
        }
        //System.out.println("WASTED: " + counter.get());
    }


    public static void main(String[] args)
    {
        doAndMeasure("Executors", () -> run(1000, 10000));
    }

    public static void doAndMeasure(String caption, Runnable runnable)
    {
        long tStart = System.currentTimeMillis();
        runnable.run();
        System.out.println(caption + " took " + (System.currentTimeMillis() - tStart) + "ms");
        //RESULT_LIST.forEach(x-> System.out.println(x.path() + ": " + x.number()));
        System.out.println(RESULT_LIST.size());
    }
}