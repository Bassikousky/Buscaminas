package minesweeper;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

import static java.lang.Math.ceil;


public class Score
{
    ArrayList<Time> bestTimes;
    
    int gamesPlayed;
    int gamesWon;
       
    int longestWinningStreak;
    int longestLosingStreak;
    
    int currentStreak;

    int currentWinningStreak;
    int currentLosingStreak;
    
    public Score()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
        bestTimes = new ArrayList<>();
    }
    
    
    public int getGamesPlayed()
    {
        return gamesPlayed;        
    }
    
    public int getGamesWon()
    {        
        return gamesWon;
    }
    
    public int getWinPercentage()
    {
        double gP = gamesPlayed;
        double gW = gamesWon;
        
        double percentage = ceil((gW/gP) * 100);
        
        return (int)percentage;
    }
    
    public int getLongestWinningStreak()
    {
        return longestWinningStreak;
    }
    
    public int getLongestLosingStreak()
    {
        return longestLosingStreak;
    }
    
    public int getCurrentStreak()
    {
        return currentStreak;
    }
    
    public int getCurrentLosingStreak()
    {
        return currentLosingStreak;
    }

    public int getCurrentWinningStreak(){
        return currentWinningStreak;
    }
    
    public void incGamesWon()
    {
        gamesWon++;
    }
    
    public void incGamesPlayed()
    {
        gamesPlayed++;
    }
    
    public void incCurrentStreak()
    {
        currentStreak++;
    }
    
    public void incCurrentLosingStreak()
    {
        currentLosingStreak++;
        
        if (longestLosingStreak < currentLosingStreak)
        {
            longestLosingStreak = currentLosingStreak;
        }                
    }

    public void incCurrentWinningStreak()
    {
        currentWinningStreak++;
        
        if (longestWinningStreak < currentWinningStreak)
        {
            longestWinningStreak = currentWinningStreak;
        }                
    }
    
    
    public void decCurrentStreak()
    {        
        currentStreak--;
    }    
    
    
    public void resetScore()
    {
        gamesPlayed = gamesWon = currentStreak = longestLosingStreak = longestWinningStreak = currentWinningStreak = currentLosingStreak = 0;
    }
    
    
    
    public ArrayList<Time> getBestTimes()
    {
        return bestTimes;
    }
        
    
    public void addTime(int time, Date date)
    {
        bestTimes.add(new Time(time, date));
        bestTimes.sort(new TimeComparator());
        
        if(bestTimes.size() > 5)
            bestTimes.remove(bestTimes.size()-1);
    }
     
    //--------------------------------------------------------//

    
    //------------DATABASE--------------------------//
    
    //------------POPULATE FROM DATABASE------------//
    public void populate()
    {
        Connection connection;
        Statement statement;
        ResultSet resultSet;

        try {
            String dbURL = Game.dbPath; 

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
                
                bestTimes.add(new Time(time, date));
            }
            
            
            // cleanup resources, once after processing
            resultSet.close();
            statement.close();
            
            
            // and then finally close connection
            connection.close();

        }
        catch(SQLException sqlex)
        {
            sqlex.printStackTrace();
        }
    }

    
    public void save()
    {
        Connection connection;
        PreparedStatement statement;
        

        try {
            String dbURL = Game.dbPath; 
            
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


            for (Time bestTime : bestTimes) {
                statement.setInt(1, bestTime.getTimeValue());
                statement.setDate(2, bestTime.getDateValue());

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
    public static class TimeComparator implements Comparator<Time>
    {
        @Override
        public int compare(Time a, Time b) {
            return Integer.compare(a.getTimeValue(), b.getTimeValue());
        }                        
    }

    //----------------------------------------------------------//
    public static class Time{
        Date date;
        int time;
        
        public Time(int t, Date d)
        {
            time = t;
            date = d;
        }
        
        public Date getDateValue()
        {
            return date;
        }
        
        public int getTimeValue()
        {
            return time;
        }        
    }    
}
