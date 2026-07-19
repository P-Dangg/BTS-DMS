/* Name  : Phuong Dang
Course  : CEN3024C-31032
Date    : 07/14/2026
BTS Surprise Song Tracker GUI using swing
 */


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

public class GUIApp extends JFrame {

    //Color theme
    static final Color BG_MAIN     = new Color(0x0E, 0x0B, 0x16);
    static final Color BG_PANEL    = new Color(0x18, 0x14, 0x20);
    static final Color BG_CARD     = new Color(0x1F, 0x1A, 0x2B);
    static final Color PURPLE_DEEP = new Color(0x6C, 0x3A, 0xC9);
    static final Color PURPLE_BRIGHT = new Color(0x9B, 0x5D, 0xE5);
    static final Color PURPLE_SOFT = new Color(0x4B, 0x2E, 0x77);
    static final Color TEXT_MAIN   = new Color(0xED, 0xE7, 0xF6);
    static final Color TEXT_MUTED  = new Color(0x8E, 0x85, 0xA8);
    static final Color ACCENT_GOLD = new Color(0xE8, 0xC4, 0x68);
    static final Color ROW_ALT     = new Color(0x22, 0x1B, 0x30);
    static final Color ROW_RARE    = new Color(0x3A, 0x21, 0x54);
    static final Color DANGER      = new Color(0xE5, 0x48, 0x4D);
    static final Color SUCCESS     = new Color(0x5F, 0xD0, 0x7A);

    //Constants
    static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String[] COLUMNS = {
            "Title", "Date", "City", "Plays", "Unit", "Year", "Borahae Score"
    };

    //Data
    private final SongRepository repository = new SongRepository();
    private final List<Song> visibleSongs = new ArrayList<>();
    private final Map<Integer, Boolean> sortAscending = new HashMap<>();

    //UI components
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JTable table;
    private SongTableModel tableModel;
    private JLabel statusStats;
    private JLabel statusConn;


    //Constructor
    public GUIApp() {
        super("BTS Surprise Song Tracker \u2014 Concert Stage Layout \uD83D\uDC9C");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 680);
        setMinimumSize(new Dimension(920, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        repository.loadFromFile();

        add(buildControlPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        filterAndRefresh();
    }

    //Control panel
    private JPanel buildControlPanel() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(BG_PANEL);
        outer.setBorder(new EmptyBorder(12, 16, 12, 16));

        //Row 1: title + search + filter
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(BG_PANEL);

        JLabel title = new JLabel("\uD83D\uDC9C BTS Surprise Song Tracker  \uD83D\uDC9C");
        title.setForeground(PURPLE_BRIGHT);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        row1.add(title, BorderLayout.WEST);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchWrap.setBackground(BG_PANEL);

        searchField = new JTextField(18);
        styleTextField(searchField);
        searchField.getDocument().addDocumentListener(new SimpleDocListener(this::filterAndRefresh));

        filterCombo = new JComboBox<>(new String[]{"All Songs", "Unit Songs Only", "Group Songs Only"});
        styleComboBox(filterCombo);
        filterCombo.addActionListener(e -> filterAndRefresh());

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(TEXT_MUTED);
        searchWrap.add(searchLabel);
        searchWrap.add(searchField);
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setForeground(TEXT_MUTED);
        searchWrap.add(filterLabel);
        searchWrap.add(filterCombo);

        row1.add(searchWrap, BorderLayout.EAST);

        //Row 2: action buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        row2.setBackground(BG_PANEL);

        row2.add(makeButton("\u2795 Add Song", PURPLE_DEEP, Color.WHITE, this::onAdd));
        row2.add(makeButton("\uD83D\uDCC1 Import File", BG_CARD, TEXT_MAIN, this::onImport));
        row2.add(makeButton("\u270F\uFE0F Edit", BG_CARD, TEXT_MAIN, this::onEdit));
        row2.add(makeButton("\uD83D\uDDD1\uFE0F Delete", BG_CARD, DANGER, this::onDelete));
        row2.add(makeButton("\uD83D\uDC9C Borahae Score", PURPLE_BRIGHT, Color.WHITE, this::onBorahae));
        row2.add(makeButton("\uD83D\uDEAA Exit", DANGER, Color.WHITE, this::onExit));

        outer.add(row1);
        outer.add(row2);
        return outer;
    }

    //Table panel
    private JPanel buildTablePanel() {
        tableModel = new SongTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BG_PANEL);
        table.setSelectionBackground(PURPLE_DEEP);
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PURPLE_SOFT);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setReorderingAllowed(false);
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col >= 0) sortByColumn(col);
            }
        });

        table.setDefaultRenderer(Object.class, new RareRowRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEdit();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG_CARD);
        scroll.setBorder(new EmptyBorder(0, 16, 8, 16));
        scroll.setBackground(BG_MAIN);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG_MAIN);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    //Status bar
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(new EmptyBorder(6, 16, 6, 16));

        statusStats = new JLabel(" ");
        statusStats.setForeground(TEXT_MUTED);
        statusStats.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bar.add(statusStats, BorderLayout.WEST);

        statusConn = new JLabel("\u25CF Connected to " + repository.getDatabasePath());
        statusConn.setForeground(SUCCESS);
        statusConn.setFont(new Font("SansSerif", Font.BOLD, 12));
        bar.add(statusConn, BorderLayout.EAST);

        return bar;
    }

    //Filter, sort, refresh
    private void filterAndRefresh() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String mode = (String) filterCombo.getSelectedItem();

        visibleSongs.clear();
        for (Song s : repository.getAll()) {
            //Search filter
            if (!query.isEmpty()
                    && !s.getSongTitle().toLowerCase().contains(query)
                    && !s.getCity().toLowerCase().contains(query)) {
                continue;
            }
            //Unit/group filter
            if ("Unit Songs Only".equals(mode) && !s.isUnitSong()) continue;
            if ("Group Songs Only".equals(mode) && s.isUnitSong()) continue;
            visibleSongs.add(s);
        }
        tableModel.fireTableDataChanged();
        updateStatusBar();
    }

    //Sort by column
    private void sortByColumn(int col) {
        boolean asc = sortAscending.getOrDefault(col, true);
        Comparator<Song> cmp;
        switch (col) {
            case 0: cmp = Comparator.comparing(s -> s.getSongTitle().toLowerCase()); break;
            case 1: cmp = Comparator.comparing(Song::getPerformedDate); break;
            case 2: cmp = Comparator.comparing(s -> s.getCity().toLowerCase()); break;
            case 3: cmp = Comparator.comparingInt(Song::getTimesPlayed); break;
            case 4: cmp = Comparator.comparing(Song::isUnitSong); break;
            case 5: cmp = Comparator.comparingInt(Song::getReleaseYear); break;
            case 6: cmp = Comparator.comparingDouble(BorahaeCalculator::calculate); break;
            default: cmp = null;
        }
        if (cmp != null) {
            if (!asc) cmp = cmp.reversed();
            visibleSongs.sort(cmp);
            sortAscending.put(col, !asc);
            tableModel.fireTableDataChanged();
        }
    }
    //Update status bar with stats
    private void updateStatusBar() {
        List<Song> all = repository.getAll();
        int total = all.size();

        java.util.Set<String> cities = new java.util.HashSet<>();
        for (Song s : all) cities.add(s.getCity().toLowerCase());

        statusStats.setText(String.format(
                "Total Songs: %d   |   Unique Cities: %d",
                total, cities.size()));
    }

    //Button actions
    //Add
    private void onAdd() {
        new SongDialog(this, repository, null, this::afterSave).setVisible(true);
    }
    //Import text file
    private void onImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Songs From File");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Text files (*.txt)", "txt"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String path = chooser.getSelectedFile().getAbsolutePath();
        SongRepository.ImportResult importResult = repository.importFromFile(path);

        if (importResult.errorMessage != null) {
            JOptionPane.showMessageDialog(this, importResult.errorMessage,
                    "Import Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        filterAndRefresh();
        JOptionPane.showMessageDialog(this,
                "Added: " + importResult.added + "   |   Skipped: " + importResult.skipped,
                "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        statusConn.setText("\u25CF Imported " + importResult.added + " songs from file");
        statusConn.setForeground(SUCCESS);
    }

    //Edit
    private void onEdit() {
        Song selected = getSelectedSong();
        if (selected == null) {
            info("Select a song first.");
            return;
        }
        new SongDialog(this, repository, selected, this::afterSave).setVisible(true);
    }

    //Delete
    private void onDelete() {
        Song selected = getSelectedSong();
        if (selected == null) {
            info("Select a song first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + selected.getSongTitle() + "' (" + selected.getCity() + ")?",
                "Delete Song", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String title = selected.getSongTitle();
        List<Song> sameTitle = new ArrayList<>();
        for (Song s : repository.getAll()) {
            if (s.getSongTitle().equalsIgnoreCase(title)) sameTitle.add(s);
        }

        int index = repository.getAll().indexOf(selected);
        if (index >= 0) repository.delete(index);

        if (sameTitle.size() > 1) {
            int remaining = sameTitle.size() - 1;
            for (Song s : repository.getAll()) {
                if (s.getSongTitle().equalsIgnoreCase(title)) s.setTimesPlayed(remaining);
            }
            repository.persistAfterUpdate();
        }

        filterAndRefresh();
        statusConn.setText("\u25CF Deleted '" + title + "'");
        statusConn.setForeground(SUCCESS);
    }

    //Score
    private void onBorahae() {
        Song selected = getSelectedSong();
        if (selected == null) {
            info("Select a song first.");
            return;
        }
        double score = BorahaeCalculator.calculate(selected);
        int heartCount = Math.max(0, Math.min((int) score, 25));
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < heartCount; i++) {
            hearts.append("\uD83D\uDC9C");
            if ((i + 1) % 5 == 0 && i < heartCount - 1) hearts.append("\n");
        }
        //Borahae dialog
        JDialog dialog = new JDialog(this, "Borahae Meter", true);
        dialog.getContentPane().setBackground(BG_PANEL);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_PANEL);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel header = new JLabel("\uD83D\uDC9C BORAHAE METER \uD83D\uDC9C");
        header.setForeground(PURPLE_BRIGHT);
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(header);
        content.add(Box.createVerticalStrut(12));

        content.add(infoLine("Song", selected.getSongTitle()));
        content.add(infoLine("City", selected.getCity()));
        content.add(infoLine("Release Year", String.valueOf(selected.getReleaseYear())));
        content.add(infoLine("Times Played", String.valueOf(selected.getTimesPlayed())));
        content.add(infoLine("Borahae Score", String.format("%.1f", score)));
        content.add(Box.createVerticalStrut(14));

        JLabel heartsLabel = new JLabel("<html><div style='text-align:center; color:#9B5DE5;'>"
                + (hearts.length() == 0 ? "No hearts yet" : hearts.toString().replace("\n", "<br>"))
                + "</div></html>");
        heartsLabel.setForeground(PURPLE_BRIGHT);
        heartsLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
        heartsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        heartsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(heartsLabel);

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    //Exit button
    private void onExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit the application?",
                "Exit Application",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    //Helper for info line
    private JLabel infoLine(String label, String value) {
        JLabel l = new JLabel(label + ": " + value);
        l.setForeground(TEXT_MAIN);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void afterSave() {
        filterAndRefresh();
    }
    //Get the selected songs
    private Song getSelectedSong() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleSongs.size()) return null;
        return visibleSongs.get(row);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }

    //Styling helpers
    private void styleTextField(JTextField field) {
        field.setBackground(BG_CARD);
        field.setForeground(TEXT_MAIN);
        field.setCaretColor(TEXT_MAIN);
        field.setBorder(new CompoundBorder(new LineBorder(PURPLE_SOFT, 1), new EmptyBorder(4, 8, 4, 8)));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BG_CARD);
        combo.setForeground(TEXT_MAIN);
    }

    private JButton makeButton(String text, Color bg, Color fg, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.addActionListener(e -> action.run());
        return button;
    }

    //Table model
    private class SongTableModel extends AbstractTableModel {
        @Override public int getRowCount() { return visibleSongs.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Song s = visibleSongs.get(row);
            switch (col) {
                case 0: return s.getSongTitle();
                case 1: return s.getPerformedDate();
                case 2: return s.getCity();
                case 3: return s.getTimesPlayed();
                case 4: return s.isUnitSong() ? "Yes" : "No";
                case 5: return s.getReleaseYear();
                case 6: return String.format("%.1f", BorahaeCalculator.calculate(s));
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) { return false; }
    }

    //Row renderer
    private class RareRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            if (isSelected) {
                c.setBackground(PURPLE_DEEP);
                c.setForeground(Color.WHITE);
                return c;
            }
            Song s = (row >= 0 && row < visibleSongs.size()) ? visibleSongs.get(row) : null;
            boolean rare = s != null && BorahaeCalculator.calculate(s) >= 15.0;
            if (rare) {
                c.setBackground(ROW_RARE);
                c.setForeground(ACCENT_GOLD);
            } else if (row % 2 == 1) {
                c.setBackground(ROW_ALT);
                c.setForeground(TEXT_MAIN);
            } else {
                c.setBackground(BG_CARD);
                c.setForeground(TEXT_MAIN);
            }
            setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
            return c;
        }
    }

    //Document listener
    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;
        SimpleDocListener(Runnable callback) { this.callback = callback; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
    }

    //Add/edit dialog
    private static class SongDialog extends JDialog {
        private final SongRepository repository;
        private final Song existingSong; // null when adding
        private final Runnable onSaved;

        private JTextField titleField, dateField, cityField, playsField, yearField;
        private JComboBox<String> unitCombo;
        private JLabel errorLabel;

        SongDialog(JFrame owner, SongRepository repository, Song existingSong, Runnable onSaved) {
            super(owner, existingSong == null ? "Add Surprise Song" : "Edit Surprise Song", true);
            this.repository = repository;
            this.existingSong = existingSong;
            this.onSaved = onSaved;
            buildUI();
        }

        private void buildUI() {
            getContentPane().setBackground(BG_PANEL);
            setLayout(new BorderLayout());
            setResizable(false);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(BG_PANEL);
            form.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            //Header
            JLabel header = new JLabel(existingSong == null ? "\uD83D\uDC9C New Surprise Song" : "\uD83D\uDC9C Edit Song");
            header.setForeground(PURPLE_BRIGHT);
            header.setFont(new Font("SansSerif", Font.BOLD, 15));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            form.add(header, gbc);
            gbc.gridwidth = 1;
            //Form fields
            titleField = addRow(form, gbc, 1, "Song Title", existingSong == null ? "" : existingSong.getSongTitle());
            dateField = addRow(form, gbc, 2, "Performed Date (MM/dd/yyyy, 2026)", existingSong == null ? "" : existingSong.getPerformedDate());
            cityField = addRow(form, gbc, 3, "City", existingSong == null ? "" : existingSong.getCity());
            playsField = addRow(form, gbc, 4, "Times Played", existingSong == null ? "0" : String.valueOf(existingSong.getTimesPlayed()));
            //Unit song checkbox
            gbc.gridx = 0; gbc.gridy = 5;
            JLabel unitLabel = new JLabel("Unit Song?");
            unitLabel.setForeground(TEXT_MUTED);
            form.add(unitLabel, gbc);
            unitCombo = new JComboBox<>(new String[]{"No", "Yes"});
            unitCombo.setBackground(BG_CARD);
            unitCombo.setForeground(TEXT_MAIN);
            if (existingSong != null && existingSong.isUnitSong()) unitCombo.setSelectedItem("Yes");
            gbc.gridx = 1;
            form.add(unitCombo, gbc);

            yearField = addRow(form, gbc, 6, "Release Year (2013-2026)", existingSong == null ? "2026" : String.valueOf(existingSong.getReleaseYear()));
            //Error label
            errorLabel = new JLabel(" ");
            errorLabel.setForeground(DANGER);
            errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
            form.add(errorLabel, gbc);
            //Buttons
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            buttons.setBackground(BG_PANEL);

            JButton cancel = new JButton("Cancel");
            styleDialogButton(cancel, BG_CARD, TEXT_MUTED);
            cancel.addActionListener(e -> dispose());

            JButton save = new JButton("Save Song");
            styleDialogButton(save, PURPLE_DEEP, Color.WHITE);
            save.addActionListener(e -> onSave());

            buttons.add(cancel);
            buttons.add(save);

            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(getOwner());
        }
        //Helper for add labeled text field
        private JTextField addRow(JPanel form, GridBagConstraints gbc, int row, String label, String value) {
            gbc.gridx = 0; gbc.gridy = row;
            JLabel l = new JLabel(label);
            l.setForeground(TEXT_MUTED);
            form.add(l, gbc);

            JTextField field = new JTextField(value, 20);
            field.setBackground(BG_CARD);
            field.setForeground(TEXT_MAIN);
            field.setCaretColor(TEXT_MAIN);
            field.setBorder(new CompoundBorder(new LineBorder(PURPLE_SOFT, 1), new EmptyBorder(4, 8, 4, 8)));
            gbc.gridx = 1;
            form.add(field, gbc);
            return field;
        }
        //Style dialog buttons
        private void styleDialogButton(JButton button, Color bg, Color fg) {
            button.setBackground(bg);
            button.setForeground(fg);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setBorder(new EmptyBorder(8, 16, 8, 16));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        //Save actions
        private void onSave() {
            String title = titleField.getText().trim();
            String date = dateField.getText().trim();
            String city = cityField.getText().trim();
            String playsRaw = playsField.getText().trim();
            String yearRaw = yearField.getText().trim();
            boolean isUnit = "Yes".equals(unitCombo.getSelectedItem());
            //Validations
            if (title.isEmpty() || city.isEmpty()) {
                showError("Title and City can't be empty.");
                return;
            }
            if (!InputValidator.isValidDate(date)) {
                showError("Date must be MM/dd/yyyy.");
                return;
            }
            try {
                LocalDate parsed = LocalDate.parse(date, DATE_FMT);
                if (parsed.getYear() != 2026) {
                    showError("Performance date must be in 2026 (Arirang Tour).");
                    return;
                }
            } catch (DateTimeParseException ex) {
                showError("Date must be MM/dd/yyyy.");
                return;
            }

            int plays;
            try {
                plays = Integer.parseInt(playsRaw);
                if (plays < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError("Times Played must be a non-negative number.");
                return;
            }

            int year;
            try {
                year = Integer.parseInt(yearRaw);
                if (year < 2013 || year > 2026) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError("Release Year must be 2013-2026.");
                return;
            }
            //Check for duplicates
            if (existingSong == null) {
                if (repository.isDuplicate(title, date, city, -1)) {
                    showError("Duplicate: same title + date + city already exists.");
                    return;
                }
                Song newSong = new Song(title, date, city, plays, isUnit, year);
                repository.addSong(newSong);
            } else {
                int excludeIndex = repository.getAll().indexOf(existingSong);
                if (repository.isDuplicate(title, date, city, excludeIndex)) {
                    showError("Duplicate: same title + date + city already exists.");
                    return;
                }
                existingSong.setSongTitle(title);
                existingSong.setPerformedDate(date);
                existingSong.setCity(city);
                existingSong.setTimesPlayed(plays);
                existingSong.setUnitSong(isUnit);
                existingSong.setReleaseYear(year);
                repository.persistAfterUpdate();
            }

            onSaved.run();
            dispose();
        }

        private void showError(String msg) {
            errorLabel.setText("\u274C " + msg);
        }
    }

    //Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIApp().setVisible(true));
    }
}