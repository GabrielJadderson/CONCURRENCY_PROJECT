package endpackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by Gabriel Jadderson on 24/04/2017.
 */
public class Exam3
{

    public static BlockingDeque<ResultObject> THE_LIST = new LinkedBlockingDeque<>();
    public static BlockingDeque<ResultObject> RESULT_LIST = new LinkedBlockingDeque<>();

    public static final int NUMBER_OF_CONSUME_THREADS = 4;
    public static CountDownLatch countDownLatch_1 = new CountDownLatch(NUMBER_OF_CONSUME_THREADS);
    static AtomicBoolean isDone = new AtomicBoolean(false);
    public static ExecutorService executorService = Executors.newFixedThreadPool(4);

    /**
     * is run on another thread
     *
     * @param startDir
     */
    public static void getAndAdd(Path startDir, BlockingDeque<ResultObject> list)
    {
        try
        {
            Files.walk(startDir)
                    .filter(x -> x.toString().endsWith(".txt"))
                    .forEach(x -> {
                        executorService.submit(()->list.add(new ResultObject(x, -1)));
                    });
            isDone.set(true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void consume(BlockingDeque<ResultObject> list, BlockingDeque<ResultObject> resultObjects)
    {
        try
        {
            ResultObject first = null;
            while (isDone.get() == false || list.iterator().hasNext())
            {
                //System.out.println("PRE: " + THE_LIST.size());
                first = list.takeFirst();
                resultObjects.add(new ResultObject(first.path(), getMaxAsynch(first.path())));
                //System.out.println("POST: " + THE_LIST.size());
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static int getMaxAsynch(Path path) throws IOException
    {
        //long d = Files.lines(path).parallel().map(x -> x.split(",")).flatMap(x -> Arrays.stream(x)).count(); //USE THIS FOR FIND ANY!!!!!!!!!
        return Files.lines(path)
                .map(x -> x.split(","))
                .flatMap(x -> Arrays.stream(x))
                .mapToInt(Integer::parseInt)
                .max()
                .getAsInt();
    }


    private static int getHighest(Path file)
    {
        AtomicInteger high = new AtomicInteger(0);
        try (BufferedReader reader = Files.newBufferedReader(file))
        {

            reader.lines().forEach((line) ->
            {
                String[] numbers = line.split(",");
                for (String number : numbers)
                {
                    if (high.get() < Integer.parseInt(number))
                        high.set(Integer.parseInt(number));
                }
            });
        } catch (Exception e)
        {
        }
        return high.get();
    }

    public static void run()
    {

        new Thread(() -> getAndAdd(Paths.get("data_example"), THE_LIST)).start();

        IntStream.range(0, NUMBER_OF_CONSUME_THREADS).forEach(x ->
        {
            executorService.submit(() ->
            {
                new Thread(()-> {
                    consume(THE_LIST, RESULT_LIST);
                    countDownLatch_1.countDown();
                }).start();
            });
        });



        try
        {
            countDownLatch_1.await();
            executorService.shutdown();

            executorService.awaitTermination(2L, TimeUnit.DAYS);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception
    {

        doAndMeasure("Executors", () -> run());

        //RESULT_LIST.forEach(x -> System.out.println(x.path() + " : " + x.number()));
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
