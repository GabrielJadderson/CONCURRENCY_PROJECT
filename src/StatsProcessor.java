import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Gabriel Jadderson on 01-05-2017.
 */
public class StatsProcessor
{

    private final Deque<Path> THE_LIST = new LinkedList<>();
    public static ConcurrentHashMap<String, Integer> atMostMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> byTotalsMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Integer> numericFrequency = new ConcurrentHashMap<>();

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
                else if (getStats(list.removeFirst()))
                {
                    //do something with them
                }
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

                //occurrences
                int max = Stream.of(numbers).mapToInt(Integer::parseInt).max().getAsInt();
                atMostMap.put(path.toString(), max);
                Arrays.stream(numbers).parallel().forEach(x ->
                {
                    int e = Integer.parseInt(x);
                    if (numericFrequency.containsKey(e))
                        numericFrequency.put(e, numericFrequency.get(e) + 1);
                    else
                        numericFrequency.put(e, 1);
                });

                //Frequency of numbers
                final int[] mostFrequent = {0}; //*cries*
                final int[] leastFrequent = {Integer.MAX_VALUE};
                numericFrequency.forEach((k, v) ->
                {
                    synchronized (mostFrequent)
                    {
                        if (v > mostFrequent[0])
                            mostFrequent[0] = k;
                        mostFrequent.notify();
                    }
                    synchronized (leastFrequent)
                    {
                        if (v < leastFrequent[0])
                            leastFrequent[0] = k;
                        leastFrequent.notify();
                    }
                });

                //total sum processing
                int sum = Stream.of(numbers).mapToInt(Integer::parseInt).sum();
                byTotalsMap.put(path.toString(), sum);
                synchronized (statsObject)
                {
                    statsObject.mostFrequent = mostFrequent[0];
                    statsObject.leastFrequent = leastFrequent[0];
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public Stats run(Path path)
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
                        consume(THE_LIST, productionLatch);
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
        statsObject.occouranceMap.putAll(numericFrequency);
        statsObject.atMostHashMap.putAll(atMostMap);

        //sort and add
        ArrayList<Integer> sortingList = new ArrayList<>();
        HashMap<Integer, String> dumMap = new HashMap<>();
        byTotalsMap.entrySet().forEach(x ->
        {
            sortingList.add(x.getValue());
            dumMap.put(x.getValue(), x.getKey());
        });
        Collections.sort(sortingList);
        sortingList.forEach(x -> statsObject.byTotalsList.add(Paths.get(dumMap.get(x))));
        return statsObject;
    }

}
