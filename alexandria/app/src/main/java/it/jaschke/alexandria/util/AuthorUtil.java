package it.jaschke.alexandria.util;

import android.content.Context;
import android.widget.TextView;

import it.jaschke.alexandria.R;

/**
 * Created by teddydoll on 10/21/15.
 */
public class AuthorUtil {

    public static void populateAuthor(Context context, TextView view, String authString) {

        if(context == null || view == null) return;

        if(authString == null) {
            view.setLines(1);
            view.setText(context.getString(R.string.no_author));
        } else {
            String[] authorsArr = authString.split(",");
            view.setLines(authorsArr.length);
            view.setText(authString.replace(",", "\n"));
        }
    }
}
