package bui.quocdat.dchat.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import bui.quocdat.dchat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements View.OnClickListener {

    private ViewPager viewPager;

    private NavController navController;

    private View rootView;

    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        initView();

        return rootView;
    }

    private void initView() {
        //Toolbar
        Toolbar toolbar = rootView.findViewById(R.id.toolbar_main);
        AppCompatActivity activity = (AppCompatActivity)getActivity();

        assert activity != null;
        assert activity.getSupportActionBar() != null;
        activity.setSupportActionBar(toolbar);

        viewPager = rootView.findViewById(R.id.view_pager_main);
        setupViewPager();

        tabLayout = rootView.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        view.findViewById(R.id.img_search_user).setOnClickListener(this);

    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), 0);
        adapter.addFragment(new FriendsFragment(), "Friends");
        adapter.addFragment(new GroupChatFragment(), "Groups");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.img_search_user){
            navController.navigate(R.id.action_menu_chat_to_searchFragment);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragmentList;
        List<String> listTitle;

        ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
            fragmentList = new ArrayList<>();
            listTitle = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return listTitle.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return listTitle.get(position);
        }

        void addFragment(Fragment fragment, String title){
            fragmentList.add(fragment);
            listTitle.add(title);
        }
    }
}
