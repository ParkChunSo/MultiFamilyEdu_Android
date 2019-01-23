package kr.ac.skuniv.cosmoslab.multifamilyedu.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

import kr.ac.skuniv.cosmoslab.multifamilyedu.view.PlayFragment;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.PlayListener;

/**
 * Created by chunso on 2019-01-12.
 */

public class PlayPageAdapter extends FragmentStatePagerAdapter {
    private List<String> mStudyWords;
    private List<Integer> mWordsScore;
    private List<Integer> mPosition;
    private String mWord;

    public PlayPageAdapter(FragmentManager fm, List<String> studyWords, List<Integer> wordsScore) {
        super(fm);
        mStudyWords = studyWords;
        mWordsScore = wordsScore;
        mPosition = new ArrayList<>();
    }

    public void update(String word, int position){
        mPosition.add(position);
        mWord = word;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new PlayFragment();
        Bundle bundle = new Bundle();

        if (mPosition.contains(position)){
            bundle.putString("tag", "both");
        }else{
            bundle.putString("tag", "original");
        }

        bundle.putString("word", mStudyWords.get(position));
        bundle.putString("score", String.valueOf(mWordsScore.get(position)));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return mStudyWords.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if(object instanceof PlayListener){
            ((PlayListener) object).onRecord(mWord);
        }

        return super.getItemPosition(object);
    }
}
