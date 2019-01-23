package kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;

public class HelpFragment2 extends Fragment {

    private WebView webView1;
    private final String explain = "<html><body><p align=\"justify\">" + "&nbsp;&nbsp;로그인하면 사용자 레벨에 맞게 나타납니다. 위 화면을 예로들면 사용자는 DAY4까지 통과하였으며 DAY5로 이동하기 위해서는 DAY4 단어목록 중 70%이상을 통과해야합니다." + "</p></body></html>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_help2, container, false);

        webView1 = (WebView) rootView.findViewById(R.id.webView1);
        webView1.getSettings().setDefaultFontSize(17);
        webView1.setHorizontalScrollBarEnabled(false);
        webView1.setVerticalScrollBarEnabled(false);
        webView1.loadData(explain, "text/html", "utf-8");

        return rootView;
    }
}
