package kr.ac.skuniv.cosmoslab.multifamilyedu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity.WordModel;

/**
 * Created by chunso on 2019-01-02.
 */

public class WordPageAdapter extends BaseAdapter {
    private List<WordModel> wordModels;
    private Context context;

    public WordPageAdapter(Context context, List<WordModel> wordModels){
        this.wordModels = wordModels;
        this.context = context;
    }

    @Override
    public int getCount() {
        return wordModels.size();
    }

    @Override
    public Object getItem(int position) {
        return wordModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_status, parent, false);
        }

        TextView wordTextView = convertView.findViewById(R.id.word_textView);
        TextView passTextView = convertView.findViewById(R.id.pass_textView);
        TextView highestScoreTextView = convertView.findViewById(R.id.highest_score_textView);

        wordTextView.setText(wordModels.get(position).getWord());
        passTextView.setText(wordModels.get(position).getPass());
        highestScoreTextView.setText(String.valueOf(wordModels.get(position).getHighestScore()));

        return convertView;
    }
}
