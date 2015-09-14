package com.phong.flickicker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.phong.flickicker.fragments.LoginFragment;
import com.phong.flickicker.fragments.MainScreenFragment;
import com.phong.flickicker.images.CircleImageView;
import com.phong.flickicker.tasks.GetOAuthTokenTask;
import com.phong.flickicker.tasks.LoadUserTask;


import java.util.Locale;

public class FlickrjActivity extends FragmentActivity {
	public static final String CALLBACK_SCHEME = "flickrj-android-sample-oauth"; //$NON-NLS-1$
	public static final String PREFS_NAME = "flickrj-android-sample-pref"; //$NON-NLS-1$
	public static final String KEY_OAUTH_TOKEN = "flickrj-android-oauthToken"; //$NON-NLS-1$
	public static final String KEY_TOKEN_SECRET = "flickrj-android-tokenSecret"; //$NON-NLS-1$
	public static final String KEY_USER_NAME = "flickrj-android-userName"; //$NON-NLS-1$
	public static final String KEY_USER_ID = "flickrj-android-userId"; //$NON-NLS-1$
	

	
	
	private ListView listView;
	private TextView textUserTitle;
	private TextView textUserName;
	private TextView textUserId;
	private CircleImageView userIcon;
	private ImageButton refreshButton;


	private RelativeLayout mRelative;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mRelative = (RelativeLayout)findViewById(R.id.content_frame2);

        TextView logout =(TextView) findViewById(R.id.logout);
        logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });



		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.menu_icon,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
		) {

		};



		mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

		this.textUserTitle = (TextView) mRelative.findViewById(R.id.realname);
		this.textUserName = (TextView) mRelative.findViewById(R.id.name);
		this.textUserId = (TextView) mRelative.findViewById(R.id.id);
		this.userIcon = (CircleImageView) mRelative.findViewById(R.id.avatar);


		OAuth oauth = getOAuthToken();

		if (oauth == null || oauth.getUser() == null) {
			Fragment fragment = LoginFragment.newInstance();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
//			OAuthTask task = new OAuthTask(this);
//			task.execute();
		} else {
            MainScreenFragment fragment = MainScreenFragment.newInstance();
            fragment.setAuth(oauth);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
            new LoadUserTask(this, userIcon).execute(oauth);
		}
	}
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mRelative);

        return super.onPrepareOptionsMenu(menu);
    }

    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		//this is very important, otherwise you would get a null Scheme in the onResume later on.
		setIntent(intent);
	}
	
	public void setUser(User user) {
		textUserTitle.setText(user.getUsername());
		textUserName.setText(user.getRealName());
		textUserId.setText(user.getId());
	}
	
	public ImageView getUserIconImageView() {
		return this.userIcon;
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		OAuth savedToken = getOAuthToken();
		if (CALLBACK_SCHEME.equals(scheme) && (savedToken == null || savedToken.getUser() == null)) {
			Uri uri = intent.getData();
			String query = uri.getQuery();
			//logger.debug("Returned Query: {}", query); //$NON-NLS-1$
			String[] data = query.split("&"); //$NON-NLS-1$
			if (data != null && data.length == 2) {
				String oauthToken = data[0].substring(data[0].indexOf("=") + 1); //$NON-NLS-1$
				String oauthVerifier = data[1]
						.substring(data[1].indexOf("=") + 1); //$NON-NLS-1$
				//logger.debug("OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier); //$NON-NLS-1$

				OAuth oauth = getOAuthToken();
				if (oauth != null && oauth.getToken() != null && oauth.getToken().getOauthTokenSecret() != null) {
					GetOAuthTokenTask task = new GetOAuthTokenTask(this);
					task.execute(oauthToken, oauth.getToken().getOauthTokenSecret(), oauthVerifier);
				}
			}
		}

	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.



        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onOAuthDone(OAuth result) {
		if (result == null) {
			Toast.makeText(this,
					"Authorization failed", //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			User user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(this,
						"Authorization failed", //$NON-NLS-1$
						Toast.LENGTH_LONG).show();
				return;
			}
			String message = String.format(Locale.US, "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s", //$NON-NLS-1$
					user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			Toast.makeText(this,
					message,
					Toast.LENGTH_LONG).show();
			saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			//load(result);
			Intent intent = new Intent(this,FlickrjActivity.class);
			startActivity(intent);
			finish();
		}
	}
    
    
    public OAuth getOAuthToken() {
    	 //Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String oauthTokenString = settings.getString(KEY_OAUTH_TOKEN, null);
        String tokenSecret = settings.getString(KEY_TOKEN_SECRET, null);
        if (oauthTokenString == null && tokenSecret == null) {
        	return null;
        }
        if(tokenSecret.equalsIgnoreCase("")){
            return null;
        }
        OAuth oauth = new OAuth();
        String userName = settings.getString(KEY_USER_NAME, null);
        String userId = settings.getString(KEY_USER_ID, null);
        if (userId != null) {
        	User user = new User();
        	user.setUsername(userName);
        	user.setId(userId);
        	oauth.setUser(user);
        }
        OAuthToken oauthToken = new OAuthToken();
        oauth.setToken(oauthToken);
        oauthToken.setOauthToken(oauthTokenString);
        oauthToken.setOauthTokenSecret(tokenSecret);
       // logger.debug("Retrieved token from preference store: oauth token={}, and token secret={}", oauthTokenString, tokenSecret); //$NON-NLS-1$
        return oauth;
    }

    public void saveOAuthToken(String userName, String userId, String token, String tokenSecret) {
    //	logger.debug("Saving userName=%s, userId=%s, oauth token={}, and token secret={}", new String[]{userName, userId, token, tokenSecret}); //$NON-NLS-1$
    	SharedPreferences sp = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(KEY_OAUTH_TOKEN, token);
		editor.putString(KEY_TOKEN_SECRET, tokenSecret);
		editor.putString(KEY_USER_NAME, userName);
		editor.putString(KEY_USER_ID, userId);
		editor.commit();
    }

	public void logout(){
		SharedPreferences sp = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(KEY_OAUTH_TOKEN, "");
		editor.putString(KEY_TOKEN_SECRET, "");
		editor.putString(KEY_USER_NAME, "");
		editor.putString(KEY_USER_ID, "");
		editor.commit();

        Intent intent = new Intent(this,FlickrjActivity.class);
        startActivity(intent);
        finish();
	}

    PopupWindow mPopup;

    public void setPopup(PopupWindow input){
        mPopup = input;
    }

    @Override
    public void onBackPressed() {
        if(mPopup!= null && mPopup.isShowing())
        {
            mPopup.dismiss();
        } else {
            finish();
        }

    }
}