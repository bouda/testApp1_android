package h.d.t.dkaraoke;

import h.d.t.data.KaraokeDB;
import h.d.t.model.Song;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class DKaraoke extends ActionBarActivity implements OnClickListener {

	private ListView mListView;
	private ArrayList<ArrayList<Song>> mSongs;
	private ListAdapter mAdapter;
	private TextView optv1;
	private TextView optv2;
	private TextView op_tv3;
	private int manufature;
	private AdView adView;
	private KaraokeDB kdb;
	private View navigation;
	private int language;
	private ImageView langImg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("aa", "1");
		setContentView(R.layout.fragment_main);
		showLoadingDialog();
		manufature = 1;
		language = 0;
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		navigation = LayoutInflater.from(this).inflate(R.layout.option_menu,
				null);
		actionBar.setCustomView(navigation, lp);

		optv1 = (TextView) navigation.findViewById(R.id.op_tv1);
		optv2 = (TextView) navigation.findViewById(R.id.op_tv2);
		op_tv3 = (TextView) navigation.findViewById(R.id.op_tv3);
		langImg = (ImageView) navigation.findViewById(R.id.language);
		langImg.setOnClickListener(this);
		optv1.setOnClickListener(this);
		optv2.setOnClickListener(this);
		op_tv3.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.listview);
		getData.start();
		// Create an ad.
		// adView = new AdView(this);
		// adView.setAdSize(AdSize.BANNER);
		// adView.setLayoutParams(new LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.MATCH_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT));
		// adView.setAdUnitId("ca-app-pub-3455242531560994/5246531266");
		//
		// // Add the AdView to the view hierarchy. The view will have no size
		// // until the ad is loaded.
		// LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
		// layout.addView(adView);

		mSongs = new ArrayList<ArrayList<Song>>();
	}

	private Thread getData = new Thread(new Runnable() {

		@Override
		public void run() {
			kdb = KaraokeDB.getInstance(DKaraoke.this);
			ArrayList<Song> songs = kdb.getSongs();
			ArrayList<Song> songsA = new ArrayList<Song>();
			ArrayList<Song> songsB = new ArrayList<Song>();
			ArrayList<Song> songsC = new ArrayList<Song>();
			ArrayList<Song> songsD = new ArrayList<Song>();
			ArrayList<Song> songsE = new ArrayList<Song>();
			for (Song song : songs) {
				if (song.manufacture == 1) {
					if (song.language == 0) {
						songsA.add(song);
					} else {
						songsD.add(song);
					}
				} else {
					if (song.language == 0) {
						songsB.add(song);
					} else {
						songsE.add(song);
					}
				}
				if (song.isLike) {
					songsC.add(song);
				}
			}
			mSongs.add(songsA);
			mSongs.add(songsB);
			mSongs.add(songsC);
			mSongs.add(songsD);
			mSongs.add(songsE);
			adView = (AdView) findViewById(R.id.adView);
			final AdRequest adRequest = new AdRequest.Builder().build();
			runOnUiThread(new Runnable() {
				public void run() {
					adView.loadAd(adRequest);
					mAdapter = new ListAdapter(mSongs.get(0), DKaraoke.this,
							onCickListener);
					mListView.setAdapter(mAdapter);
				}
			});
			dismissLoadingDialog();
		}
	});

	@Override
	public void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
	}

	@Override
	public void onPause() {
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}

	/** Called before the activity is destroyed. */
	@Override
	public void onDestroy() {
		// Destroy the AdView.
		if (adView != null) {
			adView.destroy();
		}
		if (kdb != null) {
			kdb.close();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actions, menu);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu
				.findItem(R.id.action_search));
		searchView.setOnQueryTextListener(mOnQueryTextListener);
		searchView.addOnLayoutChangeListener(new OnLayoutChangeListener() {

			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight,
					int oldBottom) {
				SearchView searchView = (SearchView) v;
				if (searchView.isIconfiedByDefault()
						&& !searchView.isIconified()) {
					// search got expanded from icon to search box, hide tabs to
					// make space
					getActionBar().getCustomView().setVisibility(View.GONE);
				} else {
					getActionBar().getCustomView().setVisibility(View.VISIBLE);
				}
			}

		});
		return true;
	}

	private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextChange(String newText) {
			newText = newText.toLowerCase();
			int index;
			if (manufature == 3) {
				index = 2;
			} else {
				index = manufature - 1 + 3 * language;
			}
			ArrayList<Song> songss = mSongs.get(index);
			if (newText.length() == 0) {
				mAdapter = new ListAdapter(songss, DKaraoke.this,
						onCickListener);
				mListView.setAdapter(mAdapter);
				return true;
			}
			ArrayList<Song> songs = new ArrayList<Song>();
			for (Song song : songss) {
				if (song.name.toLowerCase().contains(newText)
						|| song.abbr.toLowerCase().contains(newText)
						|| song.author.toLowerCase().contains(newText)
						|| song.lyric.toLowerCase().contains(newText)) {
					songs.add(song);
				}
			}
			mAdapter = new ListAdapter(songs, DKaraoke.this, onCickListener);
			mListView.setAdapter(mAdapter);
			return true;
		}

		@Override
		public boolean onQueryTextSubmit(String query) {
			return true;
		}
	};

	private OnClickListener onCickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int index = (Integer) v.getTag();
			Song song = mSongs.get(manufature - 1).get(index);
			ArrayList<Song> songs = mSongs.get(2);
			if (song.isLike) {
				((ImageView) v).setImageResource(R.drawable.ic_bookmark);
				song.isLike = false;
				kdb.updateSong(song);
				songs.remove(song);
			} else {
				((ImageView) v).setImageResource(R.drawable.ic_bookmark_active);
				song.isLike = true;
				kdb.updateSong(song);
				songs.add(song);
			}
			if (manufature == 3) {
				mAdapter = new ListAdapter(songs, DKaraoke.this, onCickListener);
				mListView.setAdapter(mAdapter);
			}
		}
	};

	private static class ListAdapter extends BaseAdapter {

		private ArrayList<Song> songs;
		private LayoutInflater mInfalter;
		private OnClickListener onCickListener;

		public ListAdapter(ArrayList<Song> songs, Activity activity,
				OnClickListener onCickListener) {
			this.songs = songs;
			mInfalter = LayoutInflater.from(activity);
			this.onCickListener = onCickListener;
		}

		@Override
		public int getCount() {
			return songs.size();
		}

		@Override
		public Object getItem(int position) {
			return songs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh;
			if (convertView == null) {
				vh = new ViewHolder();
				convertView = mInfalter.inflate(R.layout.list_item, null);
				vh.tv1 = (TextView) convertView.findViewById(R.id.tv1);
				vh.tv2 = (TextView) convertView.findViewById(R.id.tv2);
				vh.tv3 = (TextView) convertView.findViewById(R.id.tv3);
				vh.img = (ImageView) convertView.findViewById(R.id.favorite);
				vh.img.setOnClickListener(onCickListener);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}
			Song song = songs.get(position);
			vh.tv1.setText(song.id);
			vh.tv2.setText(song.name);
			vh.tv3.setText(song.lyric);
			vh.img.setTag(position);
			if (song.isLike) {
				vh.img.setImageResource(R.drawable.ic_bookmark_active);
			} else {
				vh.img.setImageResource(R.drawable.ic_bookmark);
			}
			return convertView;
		}

		private static class ViewHolder {
			TextView tv1;
			TextView tv2;
			TextView tv3;
			ImageView img;
		}
	}

	private ProgressDialog mLoadingDialog;

	public void showLoadingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mLoadingDialog == null) {
					mLoadingDialog = ProgressDialog.show(DKaraoke.this, "", "");
					mLoadingDialog.setContentView(R.layout.progress_layout);
				}
				mLoadingDialog.show();
			}
		});
	}

	public void dismissLoadingDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.v("aa", "2");
				if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
					mLoadingDialog.dismiss();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.op_tv1:
			if (manufature == 1) {
				return;
			}
			manufature = 1;
			mAdapter = new ListAdapter(mSongs.get(3 * language), DKaraoke.this,
					onCickListener);
			mListView.setAdapter(mAdapter);
			optv1.setBackgroundColor(Color.parseColor("#066CCF"));
			optv2.setBackgroundColor(0);
			op_tv3.setBackgroundColor(0);
			break;
		case R.id.op_tv2:
			if (manufature == 2) {
				return;
			}
			manufature = 2;
			mAdapter = new ListAdapter(mSongs.get(1 + 3 * language),
					DKaraoke.this, onCickListener);
			mListView.setAdapter(mAdapter);
			optv2.setBackgroundColor(Color.parseColor("#066CCF"));
			optv1.setBackgroundColor(0);
			op_tv3.setBackgroundColor(0);
			break;
		case R.id.op_tv3:
			if (manufature == 3) {
				return;
			}
			manufature = 3;
			mAdapter = new ListAdapter(mSongs.get(2), DKaraoke.this,
					onCickListener);
			mListView.setAdapter(mAdapter);
			op_tv3.setBackgroundColor(Color.parseColor("#066CCF"));
			optv1.setBackgroundColor(0);
			optv2.setBackgroundColor(0);
			break;
		case R.id.language:
			if (language == 0) {
				language = 1;
				((ImageView) v).setImageResource(R.drawable.e);
			} else {
				language = 0;
				((ImageView) v).setImageResource(R.drawable.v);
			}
			int index;
			if (manufature == 3) {
				index = 2;
			} else {
				index = manufature - 1 + 3 * language;
			}
			ArrayList<Song> songss = mSongs.get(index);
			mAdapter = new ListAdapter(songss, DKaraoke.this, onCickListener);
			mListView.setAdapter(mAdapter);
			break;

		}

	}

}
