package saleem.elec3607_pong;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by saleem on 19/05/17.
 * Simple class to store the match histories
 */

public class MatchHistory {
    // ally (human) score
    private int allyScore;
    // computer score
    private int enemyScore;
    // date of the game
    private String date;


    public MatchHistory(int allyScore, int enemyScore, long milliseconds){
        this.allyScore = allyScore;
        this.enemyScore = enemyScore;
        // create and format the date
        Date date = new Date(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat("d/M/y h:ma");
        this.date = format.format(date);
    }

    // setters and getters for our private methods

    public int getAllyScore() {
        return allyScore;
    }

    public void setAllyScore(int allyScore) {
        this.allyScore = allyScore;
    }

    public int getEnemyScore() {
        return enemyScore;
    }

    public void setEnemyScore(int enemyScore) {
        this.enemyScore = enemyScore;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
