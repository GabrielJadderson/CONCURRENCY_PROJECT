package endpackage;

import endpackage.FinalPackage.ResultObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class GuardedBlocks
{

    //
    // private static final Deque<ResultObject> THE_LIST = new LinkedList<>();
    private static final BlockingDeque<ResultObject> THE_LIST = new LinkedBlockingDeque<>();
    //private static final Deque<ResultObject> RESULT_LIST = new LinkedList<>();
    private static final BlockingDeque<ResultObject> RESULT_LIST = new LinkedBlockingDeque<>();

    private static void FindFiles(Path startDir, BlockingDeque<ResultObject> list)
    {
        try
        {
            Files.walk(startDir)
                    .parallel()
                    .filter(x -> x.toString().endsWith(".txt"))
                    .forEach(x ->
                    {
                        ResultObject prod = new ResultObject(x, -1);
                        synchronized (list)
                        {
                            list.add(prod);
                            list.notify();
                        }
                    });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private final static AtomicInteger counter = new AtomicInteger(0);

    private static void processFile(Deque<ResultObject> list, BlockingDeque<ResultObject> resultObjects, CountDownLatch latch) throws IOException
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
                    ResultObject prod = list.removeFirst();
                    synchronized (resultObjects)
                    {
                        resultObjects.add(new ResultObject(prod.path(), getMax(prod.path())));
                        resultObjects.notify();
                    }
                    //System.out.println("consumed a product " + prod.path());
                }
            }
        }
    }

    private static int getMax(Path path) throws IOException
    {
        AtomicInteger total = new AtomicInteger(0);
        Files.lines(path).map(x -> x.split(",")).flatMap(x -> Arrays.stream(x)).forEach(x ->
        {
            if (total.get() < Integer.parseInt(x))
                total.set(Integer.parseInt(x));
        });
        return total.get();
    }


    private static final int NUM_PRODUCERS = Runtime.getRuntime().availableProcessors() - 1;

    public static void run()
    {
        // Proposal 1: Before the consumer waits, it checks if something is in the list.
        // Proposal 2: Before the producer sends the signal, it checks if a consumer is waiting.

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(NUM_PRODUCERS);

        new Thread(() ->
        {
            FindFiles(Paths.get("data_example"), THE_LIST);
            latch.countDown();
        }).start();

        IntStream.range(0, NUM_PRODUCERS).forEach(
                i ->
                {
                    new Thread(() ->
                    {
                        try
                        {
                            processFile(THE_LIST, RESULT_LIST, latch);
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
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
        doAndMeasure("Executors", () -> run());
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