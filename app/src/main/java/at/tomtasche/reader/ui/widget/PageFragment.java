package at.tomtasche.reader.ui.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import at.tomtasche.reader.background.Document.Page;

public class PageFragment extends Fragment {

	public static final String FRAGMENT_TAG = "page_fragment";

	private static final String EXTRA_SCROLL_POSITION = "scroll_position";

	private PageView pageView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			pageView = new PageView(getActivity(),
					savedInstanceState.getInt(EXTRA_SCROLL_POSITION));
		} else {
			pageView = new PageView(getActivity());
			pageView.loadData("", "text/plain", PageView.ENCODING);
		}

		pageView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		return pageView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(EXTRA_SCROLL_POSITION, pageView.getScrollY());
	}

	private void loadData(String url) {
		pageView.loadUrl(url);
	}

	public void loadPage(Page page) {
		loadData(page.getUrl());
	}

	@SuppressWarnings("deprecation")
	public void searchDocument(String query) {
		pageView.findAll(query);
	}

	public PageView getPageView() {
		return pageView;
	}
}
