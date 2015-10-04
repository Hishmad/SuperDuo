package barqsoft.footballscores.support;

import android.content.Context;
import android.text.format.Time;

import java.text.SimpleDateFormat;

import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {

    // Constant
    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int NINE = 9;
    private static final int TEN = 10;
    private static final int ELEVEN = 11;
    private static final int TWELVE = 12;


    private static final String ARSENAL_LONDON_FC = "Arsenal London FC";
    private static final String MANCHESTER_UNITED_FC = "Manchester United FC";
    private static final String SWANSEA_CITY = "Swansea City";
    private static final String LEICESTER_CITY = "Leicester City";
    private static final String EVERTON_FC = "Everton FC";
    private static final String WEST_HAM_UNITED_FC = "West Ham United FC";
    private static final String TOTTENHAM_HOTSPUR_FC = "Tottenham Hotspur FC";
    private static final String WEST_BROMWICH_ALBION = "West Bromwich Albion";
    private static final String SUNDERLAND_AFC = "Sunderland AFC";
    private static final String STOKE_CITY_FC = "Stoke City FC";
    private static final String SIMPLE_DATE_FORMAT = "EEEE";
    private static final String DASH = " - ";

    public static String getLeague(Context c, int league_num) {
        switch (league_num) {
            case SERIE_A:
                return c.getString(R.string.get_league_serie_a);
            case PREMIER_LEGAUE:
                return c.getString(R.string.get_league_premier_league);
            case CHAMPIONS_LEAGUE:
                return c.getString(R.string.get_league_UEFA_champions_league);
            case PRIMERA_DIVISION:
                return c.getString(R.string.get_league_primera_division);
            case BUNDESLIGA:
                return c.getString(R.string.get_league_bundesliga);
            default:
                return c.getString(R.string.get_league_unknown_report);
        }
    }

    public static String getMatchDay(Context c, int match_day, int league_num) {
        if (league_num == CHAMPIONS_LEAGUE) {
            if (match_day <= SIX) {
                return c.getString(R.string.get_match_day_group_stages);
            } else if (match_day == SEVEN || match_day == EIGHT) {
                return c.getString(R.string.get_match_day_knockout);
            } else if (match_day == NINE || match_day == TEN) {
                return c.getString(R.string.get_match_day_q_final);
            } else if (match_day == ELEVEN || match_day == TWELVE) {
                return c.getString(R.string.get_match_day_semi_final);
            } else {
                return c.getString(R.string.get_match_day_final);
            }
        } else {
            return c.getString(R.string.get_match_day_matchday) + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        if (home_goals < ZERO || awaygoals < ZERO) {
            return DASH;
        } else {
            return String.valueOf(home_goals) + DASH + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        if (teamname == null) {
            return R.drawable.no_icon;
        }
        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case ARSENAL_LONDON_FC:
                return R.drawable.arsenal;
            case MANCHESTER_UNITED_FC:
                return R.drawable.manchester_united;
            case SWANSEA_CITY:
                return R.drawable.swansea_city_afc;
            case LEICESTER_CITY:
                return R.drawable.leicester_city_fc_hd_logo;
            case EVERTON_FC:
                return R.drawable.everton_fc_logo1;
            case WEST_HAM_UNITED_FC:
                return R.drawable.west_ham;
            case TOTTENHAM_HOTSPUR_FC:
                return R.drawable.tottenham_hotspur;
            case WEST_BROMWICH_ALBION:
                return R.drawable.west_bromwich_albion_hd_logo;
            case SUNDERLAND_AFC:
                return R.drawable.sunderland;
            case STOKE_CITY_FC:
                return R.drawable.stoke_city;
            default:
                return R.drawable.no_icon;
        }
    }

    /**
     * Locale or otherwise just the name of the day.
     * @param c
     * @param dateInMillis
     * @return
     */
    public static String getNameOfTheDay(Context c, long dateInMillis) {

        Time time = new Time();
        time.setToNow();

        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), time.gmtoff);

        if (julianDay == currentJulianDay) {
            return c.getString(R.string.today);
        } else if (julianDay == currentJulianDay + ONE ) {
            return c.getString(R.string.tomorrow);
        } else if (julianDay == currentJulianDay - ONE) {
            return c.getString(R.string.yesterday);
        } else {
            Time elseTime = new Time();
            elseTime.setToNow();
            SimpleDateFormat dayFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
            return dayFormat.format(dateInMillis);
        }
    }


}
