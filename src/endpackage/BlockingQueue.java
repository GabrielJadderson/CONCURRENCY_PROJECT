package endpackage;

import endpackage.FinalPackage.ResultObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;

/**
 * @author Fabrizio Montesi <fmontesi@imada.sdu.dk>
 */
public class BlockingQueue
{

    private static final BlockingDeque<ResultObject> THE_LIST = new LinkedBlockingDeque<>();
    private static final BlockingDeque<ResultObject> RESULT_LIST = new LinkedBlockingDeque<>();

    private static void produce(Path startDir, BlockingDeque<ResultObject> list)
    {
        try
        {
            Files.walk(startDir)
                    .parallel()
                    .filter(x -> x.toString().endsWith(".txt"))
                    .forEach(x -> list.add(new ResultObject(x, -1)));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void consume(BlockingDeque<ResultObject> list, BlockingDeque<ResultObject> resultsList, CountDownLatch latch)
    {
        boolean keepRun = true;
        while (keepRun)
        {
            try
            {
                ResultObject prod = list.takeFirst();
                resultsList.add(new ResultObject(prod.path(), getMax(prod.path())));
            } catch (InterruptedException e)
            {
            }
            if (latch.getCount() == 0)
            {
                keepRun = false;
            }
        }
    }

    private static int getMax(Path path)
    {
        return 0;
    }

    private static final int NUM_CONSUMERS = 3;

    public static void run()
    {
        // Proposal 1: Before the consumer waits, it checks if something is in the list.
        // Proposal 2: Before the producer sends the signal, it checks if a consumer is waiting.

        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(() ->
        {
            produce(Paths.get("data_example"), THE_LIST);
            latch.countDown();
        }).start();

        IntStream.range(0, NUM_CONSUMERS).forEach(
                i ->
                {
                    new Thread(() ->
                    {
                        consume(THE_LIST, RESULT_LIST, latch);
                    }).start();
                });

        try
        {
            latch.await();
            RESULT_LIST.notifyAll();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // How do we stop the Consumer threads here as in GuardedBlocks?
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
        RESULT_LIST.forEach(x-> System.out.println(x.path() + " : " + x.number()));
        System.out.println(RESULT_LIST.size());
    }
}