package robustgametools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import robustgametools.model.Game;
import robustgametools.playstation_guide.R;

/**
 * An adapter to display list of games in user profile
 */
public class GameListAdapter extends BaseAdapter {

    private ArrayList<Game> mGames;
    private LayoutInflater mInflater;
    private Context mContext;

    public GameListAdapter(Context context, ArrayList<Game> games) {
        mInflater = LayoutInflater.from(context);
        mGames = games;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mGames.size();
    }

    @Override
    public Game getItem(int position) {
        return mGames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.card_game_stat, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Game game = mGames.get(position);

        String gameIconUrl = game.getTrophyIconUrl();
        Picasso.with(mContext).load(gameIconUrl).
                placeholder(R.drawable.placeholder_image).into(holder.gameImage);

        holder.title.setText(game.getTitle());
        holder.platform.setText(game.getPlatform().get(0).toString());
        holder.progress.setProgress(game.getProgress());
        holder.bronze.setText(Integer.toString(game.getBronze()));
        holder.silver.setText(Integer.toString(game.getSilver()));
        holder.gold.setText(Integer.toString(game.getGold()));

        int platinum = game.getPlatinum();
        int visibility = (platinum == 0) ? View.GONE : View.VISIBLE;
        holder.platinum.setVisibility(visibility);
        return view;
    }

    public static class ViewHolder {
        @InjectView(R.id.title) TextView title;
        @InjectView(R.id.platform) TextView platform;
        @InjectView(R.id.progress) NumberProgressBar progress;
        @InjectView(R.id.game_image) ImageView gameImage;
        @InjectView(R.id.bronze) TextView bronze;
        @InjectView(R.id.silver) TextView silver;
        @InjectView(R.id.gold) TextView gold;
        @InjectView(R.id.platinum) ImageView platinum;
        @InjectView(R.id.platforms) LinearLayout platformLayout;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
