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
 * Created by paolo on 26/07/17.
 */

public class SongsTabManagerFragment extends Fragment {
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
        Fragment fs[] = new Fragment[4];
        for (int i = 0; i < 2; i++) {
            fs[i] = new LocalSongsFragment();
            ((LocalSongsFragment) fs[i]).setTarget(i);
        }
        fs[2] = new ArtistFragment();
        fs[3] = new AlbumFragment();
        adapter.add(fs[0], getString(R.string.downloaded_song));
        adapter.add(fs[1], getString(R.string.by_song));
        adapter.add(fs[2], getString(R.string.by_artist));
        adapter.add(fs[3], getString(R.string.by_album));
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);

        return view;
    }

}