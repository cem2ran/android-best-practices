package com.futurice.project.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futurice.project.R;
import com.futurice.project.models.BiographiesModel;
import com.futurice.project.models.pojo.Book;
import com.futurice.project.utils.SubscriptionUtils;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class BookFragment extends Fragment {
    final private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private BiographiesModel biographiesModel;
    private Observable<Book> bookStream = Observable.empty(); // instead of null as default

    private TextView bookNameTextView;
    private TextView bookAuthorTextView;
    private TextView bookPriceTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        biographiesModel = BiographiesModel.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookNameTextView = (TextView) getView().findViewById(R.id.title);
        bookAuthorTextView = (TextView) getView().findViewById(R.id.author);
        bookPriceTextView = (TextView) getView().findViewById(R.id.price);
        bookStream = getBiographyBookOf("Steve");
    }

    @Override
    public void onResume() {
        super.onResume();
        subscribeTextView(getBookName(), bookNameTextView);
        subscribeTextView(getAuthorName(), bookAuthorTextView);
        subscribeTextView(getBookPrice(), bookPriceTextView);
    }

    private Observable<Book> getBiographyBookOf(final String personNameQuery) {
        return biographiesModel.getBiographyBooks(personNameQuery)
            // Get first book in the list
            .map(books -> {
                if (books == null || books.size() == 0) {
                    return null;
                }
                else {
                    return books.get(0);
                }
            })
            // Ignore null books
            .filter(book -> (book != null))
            // Make it a hot observable because it will be used by other observables
            .publish().refCount();
    }

    private Observable<String> getBookName() {
        return bookStream
            // Get book title
            .map(book -> book.title)
            .startWith("Loading title...");
    }

    private Observable<String> getAuthorName() {
        return bookStream
            .flatMap(book -> biographiesModel.getAuthor(book.id))
            .map(author -> author.name)
            .startWith("Loading author name...");
    }

    private Observable<String> getBookPrice() {
        return bookStream
            .flatMap(book -> biographiesModel.getBookPrice(book.id))
            .map(integer -> "Price: " + integer + " EUR")
            .startWith("Loading price...");
    }

    private void subscribeTextView(Observable<String> observable, final TextView textView) {
        compositeSubscription.add(SubscriptionUtils.subscribeTextViewText(observable, textView));
    }

    @Override
    public void onPause() {
        super.onPause();
        compositeSubscription.clear();
    }
}
