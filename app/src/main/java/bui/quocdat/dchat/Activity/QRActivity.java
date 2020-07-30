package bui.quocdat.dchat.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import bui.quocdat.dchat.Fragment.MyQRFragment;
import bui.quocdat.dchat.Fragment.QRScannerFragment;
import bui.quocdat.dchat.R;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class QRActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        initElement();

    }

    private void initElement() {
        tabLayout = findViewById(R.id.tabLayout_qr);
        viewPager = findViewById(R.id.view_pager_qr);

        //setup viewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        adapter.addFragment(new MyQRFragment(), "My QR");
        adapter.addFragment(new QRScannerFragment(), "QR Scanner");
        viewPager.setAdapter(adapter);

        //setup tabLayout
        tabLayout.setupWithViewPager(viewPager);

    }

    static class ViewPagerAdapter extends FragmentPagerAdapter{

        List<Fragment> fragmentList ;
        List<String> stringList;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
            fragmentList = new ArrayList<>();
            stringList = new ArrayList<>();
        }

        public void addFragment(Fragment fragment, String s){
            stringList.add(s);
            fragmentList.add(fragment);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return stringList.get(position);
        }
    }

}
