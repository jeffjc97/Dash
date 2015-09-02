package com.first.dash;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class MyRequests extends BaseActivity {
    
 // Adapters for the different ParseQuerys
  private ParseQueryAdapter<RequestPost> pendingPostsQueryAdapter;	
  private ParseQueryAdapter<RequestPost> activePostsQueryAdapter;
  private ParseQueryAdapter<RequestPost> completedPostsQueryAdapter;
  
  ListView listview0, listview1, listview2;

  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);   
    setContentView(R.layout.my_request_main);
    super.onCreateDrawer();

    // gets the current username
    final String username = ParseUser.getCurrentUser().getUsername().trim();
    
    ListView listview;
    
    // gets the location passed from MapHome
    Intent intent = getIntent(); 
    final Location location = intent.getParcelableExtra(Dash.INTENT_EXTRA_LOCATION);
        
    
  
    // Set up the handler for the button click
//    Button postButton = (Button) findViewById(R.id.asker_post);
//
//    postButton.setOnClickListener(new OnClickListener() {
//
//      public void onClick(View v) {
//
//          // Creates a new intent
//          Intent intent = new Intent(MyRequests.this, Asker.class);
//          // passes the location to asker
//          intent.putExtra(Dash.INTENT_EXTRA_LOCATION, location);
//
//          startActivity(intent);
//
//      }
//
//    });    
    
    // creates the query adapter for pending requests
    ParseQueryAdapter.QueryFactory<RequestPost> factory0 =

            new ParseQueryAdapter.QueryFactory<RequestPost>() {

              public ParseQuery<RequestPost> create() {

                ParseQuery<RequestPost> query = RequestPost.getQuery();

                query.include("user");
                
                query.orderByDescending("createdAt");
                
                query.whereEqualTo("username", username);
                query.whereEqualTo("status", "Pending");
                
                return query;

              }

            };



        // Set up the query adapter

        pendingPostsQueryAdapter = new ParseQueryAdapter<RequestPost>(this, factory0) {

          @Override

          public View getItemView(RequestPost post, View view, ViewGroup parent) {

            if (view == null) {

              view = View.inflate(getContext(), R.layout.request_list_item, null);

            }
            
            // finds the fields to be updated
            TextView usernameView = (TextView) view.findViewById(R.id.username_view);             
            TextView paymentView = (TextView) view.findViewById(R.id.payment_view);        
            TextView itemView = (TextView) view.findViewById(R.id.item_view);        
            TextView itemCostView = (TextView) view.findViewById(R.id.cost_view);   
            TextView timeView = (TextView) view.findViewById(R.id.time_view);
            
            // updates the fields
            usernameView.setText(post.getDasher().toString());
            paymentView.setText("$" + post.get("payment").toString());
            itemView.setText(post.get("item").toString());
            itemCostView.setText("$" + post.get("itemCost").toString());
            timeView.setText(post.get("time").toString());

            return view;

          }

        };

        // Disable automatic loading when the adapter is attached to a view.

        pendingPostsQueryAdapter.setAutoload(false);

        // Attach the query adapter to the view

        ListView pendingPostsListView = (ListView) findViewById(R.id.requests_listview_pending);

        pendingPostsListView.setAdapter(pendingPostsQueryAdapter);
        
        pendingPostsQueryAdapter.loadObjects();
          
        // Set up the handler for an item's selection
        ListView postsListView = (ListView) findViewById(R.id.requests_listview_pending);

        postsListView.setAdapter(pendingPostsQueryAdapter);

        postsListView.setOnItemClickListener(new OnItemClickListener() {

          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final RequestPost item = pendingPostsQueryAdapter.getItem(position);

            Intent intent = new Intent(MyRequests.this, AskerHome.class);

            intent.putExtra("id",item.getObjectId());

            startActivity(intent);
          }

        });      
    
    
      // sets up the query factory for the active posts
      ParseQueryAdapter.QueryFactory<RequestPost> factory =

          new ParseQueryAdapter.QueryFactory<RequestPost>() {

            public ParseQuery<RequestPost> create() {

              // runs the query under these parameters
              ParseQuery<RequestPost> query = RequestPost.getQuery();
              query.include("user");
              query.orderByDescending("createdAt");
              query.whereEqualTo("username", username);
              query.whereEqualTo("status", "Unanswered");

              return query;

            }

          };


      // Set up the query adapter
      activePostsQueryAdapter = new ParseQueryAdapter<RequestPost>(this, factory) {

        @Override

        public View getItemView(RequestPost post, View view, ViewGroup parent) {

          if (view == null) {

            view = View.inflate(getContext(), R.layout.request_list_item, null);

          }
          
          // finds the fields to be updated
          TextView usernameView = (TextView) view.findViewById(R.id.username_view);             
          TextView paymentView = (TextView) view.findViewById(R.id.payment_view);        
          TextView itemView = (TextView) view.findViewById(R.id.item_view);        
          TextView itemCostView = (TextView) view.findViewById(R.id.cost_view);   
          TextView timeView = (TextView) view.findViewById(R.id.time_view);
          
          // updates the fields
          paymentView.setText(post.get("payment").toString());
          itemView.setText(post.get("item").toString());
          itemCostView.setText(post.get("itemCost").toString());
          timeView.setText(post.get("time").toString());

          return view;

        }

      };



      // Disable automatic loading when the adapter is attached to a view.

      activePostsQueryAdapter.setAutoload(false);


      // Attach the query adapter to the view
      ListView postsListView1 = (ListView) findViewById(R.id.requests_listview_active);
      postsListView1.setAdapter(activePostsQueryAdapter);
      activePostsQueryAdapter.loadObjects();
        
    
    
    // listview for the completed requests
    ParseQueryAdapter.QueryFactory<RequestPost> factory2 =

            new ParseQueryAdapter.QueryFactory<RequestPost>() {

              public ParseQuery<RequestPost> create() {

                ParseQuery<RequestPost> query = RequestPost.getQuery();

                query.include("user");
                
                query.orderByDescending("createdAt");
                
                query.whereEqualTo("username", username);
                
                query.whereEqualTo("status", "Complete");

                return query;

              }

            };



      // Set up the query adapter

      completedPostsQueryAdapter = new ParseQueryAdapter<RequestPost>(this, factory2) {

        @Override

        public View getItemView(RequestPost post, View view, ViewGroup parent) {

          if (view == null) {

            view = View.inflate(getContext(), R.layout.request_list_item, null);

          }
          
          // finds the fields to be updated
          TextView usernameView = (TextView) view.findViewById(R.id.username_view);             
          TextView paymentView = (TextView) view.findViewById(R.id.payment_view);        
          TextView itemView = (TextView) view.findViewById(R.id.item_view);        
          TextView itemCostView = (TextView) view.findViewById(R.id.cost_view);   
          TextView timeView = (TextView) view.findViewById(R.id.time_view);
          
          // updates the fields
          usernameView.setText(post.getDasher().toString());
          paymentView.setText(post.get("payment").toString());
          itemView.setText(post.get("item").toString());
          itemCostView.setText(post.get("itemCost").toString());
          timeView.setText(post.get("time").toString());

          return view;

        }

      };



        // Disable automatic loading when the adapter is attached to a view.

        completedPostsQueryAdapter.setAutoload(false);


        // Attach the query adapter to the view
        ListView completedPostsListView = (ListView) findViewById(R.id.requests_listview_completed);
        completedPostsListView.setAdapter(completedPostsQueryAdapter);
        completedPostsQueryAdapter.loadObjects();
                
        
        

  }
 }