package kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;

public class HelpFragment5 extends Fragment {

    private WebView webView1;
    private final String explain = "<html><body><p align=\"justify\">" + "&nbsp;&nbsp;파랑색 그래프는 해당 단어에 맞는 아나운서 음성의 그래프입니다. 다음은 버튼에 대한 설명입니다. <p>  단어듣기 : 아나운서의 음성을 듣습니다.<br> 녹음하기 : 녹음을 진행합니다. <br> 녹음확인 : 사용자의 녹음을 확인합니다. <br> 학습현황 : 학습현황 화면으로 이동합니다." + "</p></body></html>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_help5, container, false);

        webView1 = (WebView) rootView.findViewById(R.id.webView1);
        webView1.getSettings().setDefaultFontSize(17);
        webView1.setHorizontalScrollBarEnabled(false);
        webView1.setVerticalScrollBarEnabled(false);
        webView1.loadData(explain, "text/html", "utf-8");

        return rootView;
    }
}
