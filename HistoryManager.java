package Tann;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

/**
 * HistoryManager — Data Handling
 * Manages all calculation history entries with timestamps.
 */
public class HistoryManager {

    // =========================================================
    // INNER CLASS — HISTORY ENTRY
    // =========================================================

    public static class HistoryEntry {

        private final String timestamp;
        private final String operation;
        private final String result;

        public HistoryEntry(String operation, String result) {
            this.timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            this.operation = operation;
            this.result    = result;
        }

        public String getTimestamp()  { return timestamp;  }
        public String getOperation()  { return operation;  }
        public String getResult()     { return result;     }

        @Override
        public String toString() {
            // Compact single line for JList display
            String shortResult = result.replace("\n", " | ");
            return "[" + timestamp + "]  " + operation + "  =  " + shortResult;
        }
    }

    // =========================================================
    // FIELDS
    // =========================================================

    private final List<HistoryEntry>          entries;
    private final DefaultListModel<String>    listModel;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public HistoryManager() {
        entries   = new ArrayList<>();
        listModel = new DefaultListModel<>();
    }

    // =========================================================
    // ADD HISTORY
    // =========================================================

    /**
     * Adds a new history entry with auto-generated timestamp.
     *
     * @param operation  human-readable operation description
     * @param result     the result string
     */
    public void addHistory(String operation, String result) {

        HistoryEntry entry = new HistoryEntry(operation, result);
        entries.add(entry);
        listModel.addElement(entry.toString());
    }

    // =========================================================
    // DELETE SELECTED
    // =========================================================

    /**
     * Deletes the entry at the given index.
     *
     * @param index list index to remove
     */
    public void deleteEntry(int index) {

        if (index >= 0 && index < entries.size()) {
            entries.remove(index);
            listModel.remove(index);
        }
    }

    // =========================================================
    // CLEAR ALL
    // =========================================================

    public void clearAll() {
        entries.clear();
        listModel.clear();
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public DefaultListModel<String> getListModel() {
        return listModel;
    }

    public List<HistoryEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public int size() {
        return entries.size();
    }
}