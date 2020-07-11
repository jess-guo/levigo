package com.levigo.levigoapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;




public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 1;


    private FirebaseFirestore levigoDb = FirebaseFirestore.getInstance();

    private CollectionReference inventoryRef = levigoDb.collection("Inventory");


    private RecyclerView inventoryScroll ;
    private RecyclerView.Adapter iAdapter ;
    private RecyclerView.LayoutManager iLayoutManager ;

//    private List<String> names = new LinkedList<>();
    private List<Map<String, Object>> entries = new LinkedList<>();

    private FloatingActionButton mAdd;
    private float x1, x2, y1, y2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inventoryScroll = findViewById(R.id.inventoryScroll);
        mAdd = findViewById(R.id.main_add);

        inventoryScroll.setHasFixedSize(true);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanner();
            }
        });


        Toolbar mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);

        getPermissions();
        initInventory();
    }

    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(x1 < x2){
                Intent i = new Intent(MainActivity.this, SwipeLeft.class);
                startActivity(i);
            }else if(x1 > x2){
                Intent i = new Intent(MainActivity.this, SwipeRight.class);
                startActivity(i);
            }
            break;
        }
        return false;
    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    private void getPermissions() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM);
        }
    }

    private void initInventory() {
        iLayoutManager = new LinearLayoutManager(this);
        inventoryScroll.setLayoutManager(iLayoutManager);
//        iAdapter = new InventoryViewAdapter(names, inventoryRef);
        iAdapter = new InventoryViewAdapter(entries, inventoryRef);
        inventoryScroll.setAdapter(iAdapter);

        Log.d(TAG, "WHERE IS THIS SHIT: ");


        Query alphabetical = inventoryRef.orderBy("name");
        Query expiration = inventoryRef.orderBy("expiration");
        Query dateAdded = inventoryRef.orderBy("current_date_time");
        Query quantity = inventoryRef.orderBy("quantity");
        Query reverse = inventoryRef.orderBy("name", Query.Direction.DESCENDING);


        inventoryRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) throws NullPointerException {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                assert queryDocumentSnapshots != null;
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
//                    String udi = dc.getDocument().getString("udi");
                    Map<String, Object> entry = dc.getDocument().getData();
                    Log.d(TAG, "Data entries: " + entry.toString());
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "added");
//                            Log.d(TAG, "added: " + udi);
//                            names.add(udi);
                            entries.add(entry);
                            break;
                        case REMOVED:
                            Log.d(TAG, "remove");
                            //TODO implement
//                            for (int i = 0; i < names.size(); ++i) {
//                                if (names.get(i).equals(udi)) {
//                                    names.remove(i);
//                                    break;
//                                }
//                            }
                            break;
                        case MODIFIED:
                            Log.d(TAG, "modify");
                            //TODO implement
//                            for (int i = 0; i < names.size(); ++i) {
//                                if (names.get(i).equals(udi)) {
//                                    names.set(i, udi);
//                                }
//                            }
                            break;
                    }
                }
                iAdapter.notifyDataSetChanged();
            }
        });
    }
//    private void addItem(String json) {
//        HashMap<String, String> data = new Gson().fromJson(json, HashMap.class);
//        String udi = data.get("udi");
//        if(udi == null) udi = "UNKNOWN UDI";
//        inventoryRef.document(udi).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "Added Successfully");
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            String contents = result.getContents();
            if (contents != null) {
                startItemView(contents);

            }
            if (result.getBarcodeImagePath() != null) {
                Log.d(TAG, "" + result.getBarcodeImagePath());
//                mImageView.setImageBitmap(BitmapFactory.decodeFile(result.getBarcodeImagePath()));
                //maybe add image to firebase storage
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startItemView(String barcode) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("barcode",barcode);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fui_slide_in_right, R.anim.fui_slide_out_left);
        fragmentTransaction.add(R.id.activity_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
//                mAdd.setVisibility(View.GONE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else if(!(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.manual_entry:
                startItemView("");
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.network:
                Intent intent_network = new Intent(getApplicationContext(), NetworkActivity.class);
                startActivity(intent_network);
                finish();
                return true;
            case R.id.settings:
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
            return super.onOptionsItemSelected(item);
        }
    }



}
