package com.example.shubhamkanodia.roadrunner.Adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.example.shubhamkanodia.roadrunner.Fragments.MainFragment;
import com.example.shubhamkanodia.roadrunner.Fragments.RecordsFragment;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by shubhamkanodia on 04/09/15.
 */
public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Record", "My trips" };
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public static Fragment getCurrentFragment(ViewPager pager, FragmentPagerAdapter adapter) {
        try {
            Method m = adapter.getClass().getSuperclass().getDeclaredMethod("makeFragmentName", int.class, long.class);
            Field f = adapter.getClass().getSuperclass().getDeclaredField("mFragmentManager");
            f.setAccessible(true);
            FragmentManager fm = (FragmentManager) f.get(adapter);
            m.setAccessible(true);
            String tag = null;
            tag = (String) m.invoke(null, pager.getId(), (long) pager.getCurrentItem());
            return fm.findFragmentByTag(tag);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return MainFragment.newInstance();
            case 1: return RecordsFragment.newInstance();

        }
        return MainFragment.newInstance();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}

