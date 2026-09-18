/*
 * 09.10.2023 Original version
 */


package dk.via.jpe.grpcplanets.domain;


import java.util.TreeMap;


public class MoonListMap
        implements MoonListInterface
{
    private TreeMap<String,Moon> moons = new TreeMap<String,Moon>();


    public void insertMoon( Moon m )
    {
        moons.put( m.getName(), m );
    }

    public Moon findMoon( String name )
    {
        return moons.get( name );
    }



    public Moon[] getAllMoons()
    {
        Moon[] result = new Moon[moons.size()];
        int ix = 0;

        for( Moon m: moons.values() )
            result[ix++] = m;

        return result;
    }


    public Moon[] getAllMoonsForPlanet( String planet )
    {
        int count = 0;

        for( Moon m: moons.values() )
            if( m.getPlanet().equals( planet ) )
                ++count;

        Moon[] result = new Moon[count];
        int ix = 0;

        for( Moon m: moons.values() )
            if( m.getPlanet().equals( planet ) )
                result[ix++] = m;

        return result;
    }
}
