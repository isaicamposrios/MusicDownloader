package net.ddns.paolo7297.musicdownloader.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ddns.paolo7297.musicdownloader.R;
import net.ddns.paolo7297.musicdownloader.adapter.TabsAdapter;

/**
 * Created by paolo on 21/04/17.
 */

public class TabManagerFragment extends Fragment {
    TabLayout tabs;

    public void setTabs(TabLayout tabs) {
        this.tabs = tabs;
        //tabs.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tabs, null);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        TabsAdapter adapter = new TabsAdapter(getChildFragmentManager());
        TopSongsFragment fs[] = new TopSongsFragment[5];
        for (int i = 0; i < 5; i++) {
            fs[i] = new TopSongsFragment();
            fs[i].setTarget(i + 1);
        }
        adapter.add(fs[0], "Top Settimana");
        adapter.add(fs[1], "Top 3 Mesi");
        adapter.add(fs[2], "Top 6 Mesi");
        adapter.add(fs[3], "Top Annuali");
        adapter.add(fs[4], "Top di Sempre");
        viewPager.setAdapter(adapter);
        // Set Tabs inside Toolbar
        //TabLayout tabs = (TabLayout) view.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(viewPager);

        return view;
    }


}
