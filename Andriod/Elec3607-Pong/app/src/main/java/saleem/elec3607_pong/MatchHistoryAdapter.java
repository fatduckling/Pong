package saleem.elec3607_pong;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.LinkedList;

/**
 * Created by saleem on 19/05/17.
 */

public class MatchHistoryAdapter extends RecyclerView.Adapter<MatchHistoryAdapter.ViewHolder>{
    // this is the adapter that actually displays all the match histories
    private LinkedList<MatchHistory> data; // store all match histories in a linked list

    public MatchHistoryAdapter(){
        this.data = new LinkedList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            // the recycler view holds a card view
            cardView = v;
        }
    }

    @Override
    public MatchHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int
                                                                viewType){
        CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_match_history, parent, false);
        // create a new cardview
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        // this will get called for every entry in the linked list "data"
        MatchHistory matchHistory = data.get(position); // get the match history class

        // extract the information
        int p1Score = matchHistory.getAllyScore();
        int p2Score = matchHistory.getEnemyScore();
        boolean win = p1Score > p2Score;
        CardView cardView = holder.cardView;

        // display the win or loss text
        TextView outcomeText = (TextView)cardView.findViewById(R.id.outcome_text);
        outcomeText.setText(win ? "WIN" : "LOSS");
        // append the scores
        outcomeText.setText(outcomeText.getText() + " " + p1Score + "-" + p2Score);
        // display the date
        TextView dateText = (TextView)cardView.findViewById(R.id.date_text);
        dateText.setText(matchHistory.getDate());

        // set the background colour to green or red depending on the outcome of the game (green for win)
        cardView.setBackgroundColor(win ? Color.GREEN: Color.RED);

    }
    // basic methods to delete all items in the linked list, or add an item
    public void clear(){
        this.data.clear();
    }
    // add it to the beginning of the linked list
    public void addFirst(MatchHistory m){
        this.data.addFirst(m);
    }
    // add it to the end of the linked list
    public void add(MatchHistory m){
        this.data.add(m);
    }

    @Override
    public int getItemCount(){
        return this.data.size();
    }
}
