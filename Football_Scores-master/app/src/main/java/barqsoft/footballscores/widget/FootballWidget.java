package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.service.myFetchService;

/**
 * Implementation of App Widget functionality.
 */
public class FootballWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, myFetchService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        intent.setAction(myFetchService.WIDGET_FETCH_ACTION);
        context.startService(intent);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int[] appWidgetIds, Cursor cursor) {


        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_view);

            int dateCol = cursor.getColumnIndex(DatabaseContract.scores_table.DATE_COL);
            int timeCol = cursor.getColumnIndex(DatabaseContract.scores_table.TIME_COL);
            int homeCol = cursor.getColumnIndex(DatabaseContract.scores_table.HOME_COL);
            int awayCol = cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_COL);
            int homeScoreCol = cursor.getColumnIndex(DatabaseContract.scores_table.HOME_GOALS_COL);
            int awayScoreCol = cursor.getColumnIndex(DatabaseContract.scores_table.AWAY_GOALS_COL);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date current = new Date();
            Date closesDate = null;
            Match match = null;
            while (cursor.moveToNext()) {
                String date = cursor.getString(dateCol);
                String time = cursor.getString(timeCol);
                String homeTeam = cursor.getString(homeCol);
                String awayTeam = cursor.getString(awayCol);
                String homeTeamScore = cursor.getString(homeScoreCol);
                String awayTeamScore = cursor.getString(awayScoreCol);
                Log.d("FootballWidget", "infos: " + date + " : " + time + "| " + awayTeam + " " + awayTeamScore + " - " + homeTeamScore + " " + homeTeam);
                Date matchDate;
                try {
                    matchDate = format.parse(date + "T" + time);
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }
                if (closesDate == null) {
                    closesDate = matchDate;
                    match = new Match(awayTeam, homeTeam, awayTeamScore, homeTeamScore);
                    Log.d("FootballWidget", "match updated: " + match.away + " " + match.awayScore + " - " + match.homeScore + " " + match.home);

                } else {
                    if (matchDate.before(current) && matchDate.after(closesDate) && !homeTeamScore.equals("-1")) {
                        closesDate = matchDate;
                        match = new Match(awayTeam, homeTeam, awayTeamScore, homeTeamScore);
                        Log.d("FootballWidget", "match updated: " + match.away + " " + match.awayScore + " - " + match.homeScore + " " + match.home);
                    }
                }
            }

            if(match != null && !match.homeScore.equals("-1")) {
                Log.d("FootballWidget", "final match found: " + match.away + " " + match.awayScore + " - " + match.homeScore + " " + match.home);
                views.setTextViewText(R.id.away_name, match.away);
                views.setTextViewText(R.id.score_textview, match.awayScore + " - " + match.homeScore);
                views.setTextViewText(R.id.home_name, match.home);
                views.setViewVisibility(R.id.widget_empty, View.GONE);
                views.setViewVisibility(R.id.widget_content, View.VISIBLE);

            } else {
                views.setViewVisibility(R.id.widget_empty, View.VISIBLE);
                views.setViewVisibility(R.id.widget_content, View.GONE);
            }

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_container, pi);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private static class Match {
        public final String away;
        public final String home;
        public final String awayScore;
        public final String homeScore;

        private Match(String away, String home, String awayScore, String homeScore) {
            this.away = away;
            this.home = home;
            this.awayScore = awayScore;
            this.homeScore = homeScore;
        }
    }
}

