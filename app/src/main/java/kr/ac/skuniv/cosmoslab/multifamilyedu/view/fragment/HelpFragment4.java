package kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;

public class HelpFragment4 extends Fragment {

    private WebView webView1;
    private final String explain = "<html><body><p align=\"justify\">" + "&nbsp;&nbsp;DAY의 단어목록과 합격여부, 최고점수를 확인할 수 있습니다. 각 단어는 70점 이상 받을 경우 통과할수 있습니다." + "</p></body></html>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_help4, container, false);

        webView1 = (WebView) rootView.findViewById(R.id.webView1);
        webView1.getSettings().setDefaultFontSize(17);
        webView1.setHorizontalScrollBarEnabled(false);
        webView1.setVerticalScrollBarEnabled(false);
        webView1.loadData(explain, "text/html", "utf-8");

        return rootView;
    }
}
