package kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;

public class HelpFragment3 extends Fragment {

    private WebView webView1;
    private final String explain = "<html><body><p align=\"justify\">" + "학습 시작하기 : 해당 DAY에 맞는 단어가 랜덤으로 선택되어 학습을 시작합니다. <p> 진행현황 보기 : 선택한 DAY의 단어목록을 확인합니다." + "</p></body></html>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_help3, container, false);

        webView1 = (WebView) rootView.findViewById(R.id.webView1);
        webView1.getSettings().setDefaultFontSize(17);
        webView1.setHorizontalScrollBarEnabled(false);
        webView1.setVerticalScrollBarEnabled(false);
        webView1.loadData(explain, "text/html", "utf-8");

        return rootView;
    }
}
