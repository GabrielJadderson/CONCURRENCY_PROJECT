import java.util.ArrayList;

/**
 * Created by Gabriel Jadderson on 05-05-2017.
 */
public class CustomSplitter
{

    public ArrayList<Integer> ultraFastSplitter(String l)
    {
        ArrayList<Integer> integers = new ArrayList<>(); //an arraylist containing all the parsed numbers.
        StringBuilder stringBuilder = new StringBuilder(); //used to quickly stich strings together. way more efficient.
        for (int i = 0; i < l.length(); i++)
        {
            if (l.charAt(i) != ',')
                stringBuilder.append(l.charAt(i));
            else
            {
                integers.add(Integer.parseInt(stringBuilder.toString()));
                stringBuilder.setLength(0); //more efficient than created a new object.
            }
        }
        return integers;
    }

}
