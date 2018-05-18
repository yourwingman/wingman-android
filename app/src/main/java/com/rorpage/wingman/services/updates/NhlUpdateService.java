package com.rorpage.wingman.services.updates;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.rorpage.wingman.models.sports.nhl.Game;
import com.rorpage.wingman.models.sports.nhl.Schedule;

import java.util.ArrayList;
import java.util.Locale;

import timber.log.Timber;

import static com.rorpage.wingman.modules.updatables.sports.NhlModule.PREFERENCE_KEY_MODULEDATA_NHLMODULE;

public class NhlUpdateService extends BaseUpdateService {
    @Override
    public void onCreate() {
        super.onCreate();

        final String teamsUri = "https://statsapi.web.nhl.com/api/v1/teams";
        final String scheduleUri = "https://statsapi.web.nhl.com/api/v1/schedule";

        Ion.with(NhlUpdateService.this)
                .load(scheduleUri)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Timber.d("onCompleted()");
                        if (e != null) {
                            Timber.e(e);
                        } else {
                            final Schedule schedule = (new Gson()).fromJson(result, Schedule.class);
                            final ArrayList<Game> games = schedule.Dates.get(0).Games;

                            String gameOutput = "No games today";
                            if (games.size() > 0) {
                                final Game gameToWatch = games.get(0);
                                gameOutput = String.format(Locale.US, "In progress\n%s %s | %s %s",
                                        gameToWatch.ScheduleTeam.AwayTeam.Team.Id,
                                        gameToWatch.ScheduleTeam.AwayTeam.Score,
                                        gameToWatch.ScheduleTeam.HomeTeam.Team.Id,
                                        gameToWatch.ScheduleTeam.HomeTeam.Score);
                            }

                            mSharedPreferences.edit()
                                    .putString(PREFERENCE_KEY_MODULEDATA_NHLMODULE, gameOutput)
                                    .apply();

                            stopSelf();
                        }
                    }
                });
    }
}