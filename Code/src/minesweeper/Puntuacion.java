package minesweeper;

import static java.lang.Math.ceil;
import java.sql.Connection;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;

/**
 * Clase Puntuacion
 * @author
 */
public class Puntuacion
{
    ArrayList<Time> bestTimes;
    
    int gamesPlayed;
    int gamesWon;
       
    int longestWinningStreak;
    int longestLosingStreak;
    
    int currentStreak;

    int currentWinningStreak;
    int currentLosingStreak;
    
    /**
     * Constructor
     */
    public Puntuacion()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
        bestTimes = new ArrayList();
    }
    
    /**
     * Metodo que retorna las partidas jugadas.
     * @return gamesPlayed
     */
    public int getGamesPlayed()
    {
        return gamesPlayed;        
    }
    
    /**
     * Metodo que retorna las partidas ganadas.
     * @return gamesWon
     */
    public int getGamesWon()
    {        
        return gamesWon;
    }
    
    /**
     * Metodo que retorna el porcentaje de victorias.
     * @return percentage
     */
    public int getWinPercentage()
    {
        double gP = gamesPlayed;
        double gW = gamesWon;
        
        double percentage = ceil((gW/gP) * 100);
        
        return (int)percentage;
    }
    
    /**
     * Metodo que retorna la mayor racha de victorias
     * @return longestWinningStreak
     */
    public int getLongestWinningStreak()
    {
        return longestWinningStreak;
    }
    
    /**
     * 
     * @return longestLosingStreak
     */
    public int getLongestLosingStreak()
    {
        return longestLosingStreak;
    }
    
    /**
     *
     * @return currentStreak
     */
    public int getCurrentStreak()
    {
        return currentStreak;
    }
    
    /**
     *
     * @return currentLosingStreak
     */
    public int getCurrentLosingStreak()
    {
        return currentLosingStreak;
    }
    
    /**
     *
     * @return currentWinningStreak
     */
    public int getCurrentWinningStreak(){
        return currentWinningStreak;
    }
    
    /**
     * Aumenta partidas ganadas en 1.
     */
    public void incGamesWon()
    {
        gamesWon++;
    }
    
    /**
     * Aumenta partidas jugadas en 1.
     */
    public void incGamesPlayed()
    {
        gamesPlayed++;
    }
    
    /**
     * Aumenta el valor de racha actual
     */
    public void incCurrentStreak()
    {
        currentStreak++;
    }
    
    /**
     * Aumenta el valor de racha de derrotas
     */
    public void incCurrentLosingStreak()
    {
        currentLosingStreak++;
        
        if (longestLosingStreak < currentLosingStreak)
        {
            longestLosingStreak = currentLosingStreak;
        }                
    }

    /**
     * Aumenta la racha de victorias
     */
    public void incCurrentWinningStreak()
    {
        currentWinningStreak++;
        
        if (longestWinningStreak < currentWinningStreak)
        {
            longestWinningStreak = currentWinningStreak;
        }                
    }
    
    /**
     * Disminuye la racha actual
     */ 
    public void decCurrentStreak()
    {        
        currentStreak--;
    }    
    
    /**
     * Reinicia puntuación
     */
    public void resetScore()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
    }
    
    /**
     *
     * @return bestTimes
     */
    public ArrayList<Time> getBestTimes()
    {
        return bestTimes;
    }
        
    /**
     * Aumenta tiempo
     * @param time
     * @param date
     */
    public void addTime(int time, Date date)
    {
        bestTimes.add(new Time(time,date));
        Collections.sort(bestTimes,new TimeComparator()); 
        
        if(bestTimes.size() > 5)
            bestTimes.remove(bestTimes.size()-1);
    }
     
    //--------------------------------------------------------//

    
    //------------DATABASE--------------------------//
    
    //------------POPULATE FROM DATABASE------------//

    /**
     *
     * @return boolean
     */
    public boolean populate()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String dbURL = Juego.dbPath; 

            connection = DriverManager.getConnection(dbURL); 
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM SCORE");

            while(resultSet.next()) 
            {
                gamesPlayed = resultSet.getInt("GAMES_PLAYED");
                gamesWon = resultSet.getInt("GAMES_WON");

                longestWinningStreak = resultSet.getInt("LWSTREAK");
                longestLosingStreak = resultSet.getInt("LLSTREAK");

                currentStreak = resultSet.getInt("CSTREAK");

                currentWinningStreak = resultSet.getInt("CWSTREAK");
                currentLosingStreak = resultSet.getInt("CLSTREAK");                                
            }
            
            // cleanup resources, once after processing
            resultSet.close();
            statement.close();

            
            //------------------------LOAD TIMES------------------//
            
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM TIME");
            
            
            while(resultSet.next())
            {
                int time = resultSet.getInt("TIME_VALUE");
                Date date = resultSet.getDate("DATE_VALUE");
                
                bestTimes.add(new Time(time,date));
            }
            
            
            // cleanup resources, once after processing
            resultSet.close();
            statement.close();
            
            
            // and then finally close connection
            connection.close();            
            
            return true;
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
            return false;
        }
    }

    /**
     * Guardar
     */
    public void save()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        

        try {
            String dbURL = Juego.dbPath; 
            
            connection = DriverManager.getConnection(dbURL); 

            
            //----------EMPTY SCORE TABLE------//
            String template = "DELETE FROM SCORE"; 
            statement = connection.prepareStatement(template);
            statement.executeUpdate();
            
            //----------EMPTY TIME TABLE------//
            template = "DELETE FROM TIME"; 
            statement = connection.prepareStatement(template);
            statement.executeUpdate();
            
            //--------------INSERT DATA INTO SCORE TABLE-----------//            
            template = "INSERT INTO SCORE (GAMES_PLAYED,GAMES_WON, LWSTREAK, LLSTREAK, CSTREAK, CWSTREAK, CLSTREAK) values (?,?,?,?,?,?,?)";
            statement = connection.prepareStatement(template);
            
            statement.setInt(1, gamesPlayed);
            statement.setInt(2, gamesWon);
            statement.setInt(3, longestWinningStreak);
            statement.setInt(4, longestLosingStreak);
            statement.setInt(5, currentStreak);
            statement.setInt(6, currentWinningStreak);
            statement.setInt(7, currentLosingStreak);
            
            statement.executeUpdate();
            
            //-------------------INSERT DATA INTO TIME TABLE-----------//
            template = "INSERT INTO TIME (TIME_VALUE, DATE_VALUE) values (?,?)";
            statement = connection.prepareStatement(template);
            

            for (int i = 0; i < bestTimes.size(); i++)
            {
                statement.setInt(1, bestTimes.get(i).getTimeValue());
                statement.setDate(2, bestTimes.get(i).getDateValue());
                
                statement.executeUpdate();            
            }

            //---------------------------------------------------------//
            
            statement.close();
            
            // and then finally close connection
            connection.close();            
        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
        }
        
    }

    //--------------------------------------------------//
    
    
    //---------------------------------------------------//

    /**
     * Clase comparador de tiempo
     */
    public class TimeComparator implements Comparator<Time>
    {
        @Override
        public int compare(Time a, Time b) {
            if (a.getTimeValue() > b.getTimeValue())
                return 1;
            else if (a.getTimeValue() < b.getTimeValue())
                return -1;
            else
                return 0;
        }                        
    }

    //----------------------------------------------------------//

    /**
     * Clase Tiempo
     */
    public class Time{
        Date date;
        int time;
        
        /**
         * Tiempo
         * @param t
         * @param d
         */
        public Time(int t, Date d)
        {
            time = t;
            date = d;
        }
        
        /**
         *
         * @return date
         */
        public Date getDateValue()
        {
            return date;
        }
        
        /**
         *
         * @return time
         */
        public int getTimeValue()
        {
            return time;
        }        
    }    
}
