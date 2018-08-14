package tlab.sbu_foodie.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import tlab.sbu_foodie.Adapter.RecyclerViewAdapter;
import tlab.sbu_foodie.Adapter.RowElement;
import tlab.sbu_foodie.DataHandler.TSDProcessor;
import tlab.sbu_foodie.MenuDisplayHandler.PriorityComparator;
import tlab.sbu_foodie.R;


public class MenuActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    public static final String MENU = "";
    public static final String venue = "";
    private static final String TAG = "";

    private TSDProcessor tsdProcessor;
    private String venueName = "";

    private int revealX;
    private int revealY;
    private RecyclerViewAdapter recyclerViewAdapter;

    private ArrayList<RowElement> menuList;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.cv_add)
    CardView cvAdd;
    @BindView(R.id.root)
    View rootLayout;
    @BindView(R.id.revenue)
    TextView revenueText;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        menuList = new ArrayList<>();
        venueName = intent.getStringExtra(venue);
        revenueText.setText(venueName);
        revenueText.setTextColor(Color.BLACK);
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);
            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }

        tsdProcessor = new TSDProcessor();
        List<String> periods = tsdProcessor.getPeriods(venueName);
        Menu periodMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < periods.size(); i++) {
            periodMenu.add(Menu.NONE, periods.size(), Menu.NONE, periods.get(i))
                    .setIcon(R.drawable.ic_bottom_navi);
        }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String period = (String) item.getTitle();
                InitializeRecyclerView(period, venueName);
                return true;
            }
        });
        if (periods.size() != 0)
            InitializeRecyclerView(periods.get(0), venueName);
        else {
            InitializeRecyclerView("Closed", venueName);
        }

    }

    private void InitializeRecyclerView(String period, String venueName) {
        menuList.clear();
        menuList = null;
        menuList = tsdProcessor.processString(venueName, period);
        revenueText.setText(venueName);
        if (period.equals("Closed"))
            revenueText.setText(venueName + "\t" + "(Closed)");
        revenueText.setTextColor(Color.BLACK);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        Collections.sort(menuList, new PriorityComparator());
        recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), menuList, venueName);
        recyclerView.setAdapter(recyclerViewAdapter);

    }


    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        }
    }

    public void animateRevealClose() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animator mAnimator = ViewAnimationUtils.createCircularReveal(cvAdd, revealX, revealY, cvAdd.getHeight(), 40);
            mAnimator.setDuration(300);
            mAnimator.setInterpolator(new AccelerateInterpolator());
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    cvAdd.setVisibility(View.INVISIBLE);
                    super.onAnimationEnd(animation);
                    MenuActivity.super.onBackPressed();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            });
            mAnimator.start();
        }
    }

    @Override
    public void onBackPressed() {
        animateRevealClose();
        super.onBackPressed();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }


}
