package endpackage.FinalPackage;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by Gabriel Jadderson on 01-05-2017.
 */
public class StatsObject implements Stats
{


    int occurrences = -1;
    int mostFrequent = -1;
    int leastFrequent = -1;
    List<Path> atMost = null;
    List<Path> byTotals = null;


    @Override
    public int occurrences(int number)
    {
        return occurrences;
    }

    /**
     * Returns the list of files that do not contain numbers that are greater than max.
     */
    @Override
    public List<Path> atMost(int max)
    {
        return atMost;
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
        return byTotals;
    }


    public void setOccurrences(int occurrences)
    {
        this.occurrences = occurrences;
    }

    public void setMostFrequent(int mostFrequent)
    {
        this.mostFrequent = mostFrequent;
    }

    public void setLeastFrequent(int leastFrequent)
    {
        this.leastFrequent = leastFrequent;
    }

    public void setAtMost(List<Path> atMost)
    {
        this.atMost = atMost;
    }

    public void setByTotals(List<Path> byTotals)
    {
        this.byTotals = byTotals;
    }
}
