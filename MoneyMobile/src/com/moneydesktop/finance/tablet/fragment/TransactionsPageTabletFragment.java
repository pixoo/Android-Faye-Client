package com.moneydesktop.finance.tablet.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.moneydesktop.finance.BaseFragment;
import com.moneydesktop.finance.R;
import com.moneydesktop.finance.database.Transactions;
import com.moneydesktop.finance.model.EventMessage.ParentAnimationEvent;
import com.moneydesktop.finance.tablet.adapter.TransactionsTabletAdapter;
import com.moneydesktop.finance.views.AmazingListView;

import de.greenrobot.event.EventBus;

import java.util.List;

public class TransactionsPageTabletFragment extends BaseFragment implements OnItemClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    private AmazingListView mTransactionsList;

    private TransactionsTabletAdapter mAdapter;
    
    private boolean mLoaded = false;
    private boolean mWaiting = false;
    
    public static TransactionsPageTabletFragment newInstance() {
            
        TransactionsPageTabletFragment fragment = new TransactionsPageTabletFragment();
    
        Bundle args = new Bundle();
        fragment.setArguments(args);
        
        return fragment;
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        EventBus.getDefault().register(this);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        
        mRoot = inflater.inflate(R.layout.tablet_transaction_page_view, null);
        
        mTransactionsList = (AmazingListView) mRoot.findViewById(R.id.transactions);
        mTransactionsList.setOnItemClickListener(this);
        
        if (!mLoaded)
            getInitialTransactions();
        else
            setupList();
        
        return mRoot;
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        EventBus.getDefault().unregister(this);
    }
    
    private void setupList() {

        mTransactionsList.setAdapter(mAdapter);
        mTransactionsList.setLoadingView(mActivity.getLayoutInflater().inflate(R.layout.loading_view, null));
        
        mAdapter.notifyMayHaveMorePages();
        
        if (!mWaiting) {
            configureView();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        
        Transactions transaction = (Transactions) parent.getItemAtPosition(position);
        
        if (transaction != null) {
            
            int[] location = new int[2];
            parent.getLocationOnScreen(location);
            
            Log.i(TAG, "Y: " + location[1]);
            
            TransactionsTabletFragment frag = ((TransactionsTabletFragment) getParentFragment());
            frag.showTransactionDetails(view, location[1]);
        }
    }

    private void configureView() {
        
        if (mLoaded && !mWaiting) {
            
            mTransactionsList.setVisibility(View.VISIBLE);
            animate(mTransactionsList).alpha(1.0f).setDuration(400);
        }
    }
    
    private void getInitialTransactions() {

        new AsyncTask<Integer, Void, Void>() {
            
            @Override
            protected Void doInBackground(Integer... params) {
                
                int page = params[0];

                List<Transactions> row1 = Transactions.getRows(page).second;

                mAdapter = new TransactionsTabletAdapter(mActivity, mTransactionsList, row1);
                
                return null;
            }
            
            @Override
            protected void onPostExecute(Void result) {

                mLoaded = true;
                setupList();
            };
            
        }.execute(1);
    }
    
    public void onEvent(ParentAnimationEvent event) {
        
        if (!event.isOutAnimation() && !event.isFinished()) {
            mWaiting = true;
        }
        
        if (event.isOutAnimation() && event.isFinished()) {
            
            mWaiting = false;
            
            configureView();
        }
    }
    
    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
