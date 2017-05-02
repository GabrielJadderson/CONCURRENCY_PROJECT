import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Gabriel Jadderson on 01-05-2017.
 */
public class StatsObject implements Stats
{
    int mostFrequent = -1;
    int leastFrequent = -1;
    List<Path> atMostList = new ArrayList<>();
    List<Path> byTotalsList = new ArrayList<>();
    HashMap<String, Integer> atMostHashMap = new HashMap<>();
    HashMap<Integer, Integer> occouranceMap = new HashMap<>();


    @Override
    public int occurrences(int number)
    {
        int occourances = 0;
        try //becuase hashmap.get returns null if no key exists, in our case if no key exists that mean the occurrence is 0.
        {
            occourances = occouranceMap.get(number);
        } catch (NullPointerException e)
        {
        }
        return occourances;
    }

    @Override
    public List<Path> atMost(int max)
    {
        atMostHashMap.forEach((k, v) ->
        {
            if (v <= max)
                atMostList.add(Paths.get(k));
        });
        return atMostList;
    }

    @Override
    public int mostFrequent()
    {
        return mostFrequent;
    }

    @Override
    public int leastFrequent()
    {
        return leastFrequent;
    }

    @Override
    public List<Path> byTotals()
    {
        return byTotalsList;
    }


}
