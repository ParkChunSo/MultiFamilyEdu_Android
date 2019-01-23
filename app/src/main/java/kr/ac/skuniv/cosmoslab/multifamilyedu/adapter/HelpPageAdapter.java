package kr.ac.skuniv.cosmoslab.multifamilyedu.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment.HelpFragment1;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment.HelpFragment2;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment.HelpFragment3;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment.HelpFragment4;
import kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment.HelpFragment5;

public class HelpPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public HelpPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                HelpFragment1 tab1 = new HelpFragment1();
                return tab1;
            case 1:
                HelpFragment2 tab2 = new HelpFragment2();
                return tab2;
            case 2:
                HelpFragment3 tab3 = new HelpFragment3();
                return tab3;
            case 3:
                HelpFragment4 tab4 = new HelpFragment4();
                return tab4;
            case 4:
                HelpFragment5 tab5 = new HelpFragment5();
                return tab5;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
