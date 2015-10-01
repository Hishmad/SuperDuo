package it.jaschke.alexandria.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.fragment.BookDetail;

/**
 * Created by hishmadabubakaralamudi on 9/28/15.
 */
public class BookDetailActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        if (savedInstanceState == null) {

            String ean = getIntent().getStringExtra(BookDetail.EAN_KEY);

            Bundle arguments = new Bundle();
            arguments.putString(BookDetail.EAN_KEY, ean);


            BookDetail fragment = new BookDetail();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.book_detail_container, fragment)
                    .commit();

        }
    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }



}
