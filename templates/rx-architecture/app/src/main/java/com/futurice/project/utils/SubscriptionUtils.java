package com.futurice.project.utils;

import android.widget.TextView;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class SubscriptionUtils {
    private SubscriptionUtils() { }

    static public Subscription subscribeTextViewText(final Observable<String> observable,
        final TextView textView)
    {
        return observable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(textView::setText);
    }
}
