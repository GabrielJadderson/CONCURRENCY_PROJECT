package endpackage.FinalPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Gabriel Jadderson on 01-05-2017.
 */
public class StatsProcessor
{

    private final Deque<Path> THE_LIST = new LinkedList<>();
    //public static ConcurrentHashMap<>

    public StatsObject statsObject = new StatsObject();

    private final int NUM_CONSUMERS = 2;

    private void produce(Path startDir, Deque<Path> list)
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


    private boolean consume(Deque<Path> list, CountDownLatch latch)
    {
        boolean keepRun = true;
        while (keepRun)
        {
            synchronized (list)
            {
                if (list.isEmpty())
                    if (latch.getCount() == 0)
                        keepRun = false;
                    else
                        try
                        {
                            list.wait();
                        } catch (InterruptedException e)
                        {
                        }
                else if (getStats(list.removeFirst())) //do something with them
                    return true;
            }
        }
        return false;
    }


    private boolean getStats(Path path)
    {
        try (FileReader fileReader = new FileReader(path.toFile()); BufferedReader bufferedReader = new BufferedReader(fileReader))
        {
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                String[] numbers = line.split(",");
                synchronized (statsObject) {
                    statsObject.setOccurrences(statsObject.occurrences + numbers.length);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public Result run(Path path, int n, int min)
    {
        CountDownLatch productionLatch = new CountDownLatch(1);
        CountDownLatch consumerLatch = new CountDownLatch(NUM_CONSUMERS);

        new Thread(() ->
        {
            produce(path, THE_LIST); //stores them in an array to be processed.
            productionLatch.countDown();
        }).start();

        IntStream.range(0, NUM_CONSUMERS).parallel().forEach(
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
        } catch (InterruptedException e)
        {
        }
        return resultObject;
    }

}
