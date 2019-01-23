package kr.ac.skuniv.cosmoslab.multifamilyedu.view.fragment;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class HelpFragment1 extends Fragment {

    private WebView webView1;
    private WebView webView2;
    private final String introduce = "<html><body><p align=\"justify\">" + "&nbsp;&nbsp;본 어플리케이션은 한국어 말하기 교육을 위한 어플리케이션입니다. 아나운서의 음성을 듣고 사용자가 말한 음성과 비교하여 점수를 나타내어 한국어 말하기를 배울수 있도록 합니다." + "</p></body></html>";
    private final String precautions = "<html><body><p align=\"justify\">" + "&nbsp;&nbsp;본 어플리케이션은 인터넷 통신을 기반으로 동작합니다. 따라서 인터넷이 연결된 상태에서 이용해주세요." + "</p></body></html>";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_help1, container, false);

        webView1 = (WebView) rootView.findViewById(R.id.webView1);
        webView2 = (WebView) rootView.findViewById(R.id.webView2);
        webView1.getSettings().setDefaultFontSize(17);
        webView2.getSettings().setDefaultFontSize(17);
        webView1.loadData(introduce, "text/html", "utf-8");
        webView2.loadData(precautions, "text/html", "utf-8");

        return rootView;
    }
}
