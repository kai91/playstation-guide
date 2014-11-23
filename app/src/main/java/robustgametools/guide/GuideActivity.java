package robustgametools.guide;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import robustgametools.adapter.NavigationDrawerAdapter;
import robustgametools.model.BaseActivity;
import robustgametools.model.TrophyGuide;
import robustgametools.playstation_guide.R;

public class GuideActivity extends BaseActivity {

    @InjectView(R.id.drawer) DrawerLayout mDrawer;
    @InjectView(R.id.drawer_menu) ListView mDrawerMenu;

    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            Fragment frag = new GuideFragment();
            Bundle bundle = new Bundle();
            bundle.putString("guideInfo", getTrophyGuideInfo());
            frag.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .add(R.id.container, frag)
                    .commit();
        }

        initDrawer();
    }

    private String getTrophyGuideInfo() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("guideInfo");
        return json;
    }

    private void initDrawer() {
        setActionBarIcon(R.drawable.ic_drawer);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawer,
                mToolbar,
                R.string.app_name,
                R.string.app_name
        );
        mDrawer.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_guide;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.guide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == R.id.action_refresh_guide) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
