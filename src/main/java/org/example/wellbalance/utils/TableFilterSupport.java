package org.example.wellbalance.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Predicate;

public class TableFilterSupport<T> {

    private final ObservableList<T> source = FXCollections.observableArrayList();
    private final FilteredList<T> filtered = new FilteredList<>(source, item -> true);
    private final SortedList<T> sorted = new SortedList<>(filtered);

    public TableFilterSupport(TableView<T> tableView) {
        sorted.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sorted);
    }

    public void setItems(Collection<T> items) {
        source.setAll(items);
    }

    public void apply(Predicate<T> predicate) {
        filtered.setPredicate(predicate);
    }

    public void sortWith(Comparator<T> comparator) {
        if (comparator == null) {
            return;
        }
        FXCollections.sort(source, comparator);
    }

    public ObservableList<T> getSource() {
        return source;
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }

    public static boolean containsNormalized(String haystack, String needle) {
        return normalize(haystack).contains(normalize(needle));
    }
}
