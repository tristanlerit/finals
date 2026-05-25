package Tann;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.*;

/**
 * BaseCalculatorDashboard — "Precision Instrument" Edition
 *
 * Aesthetic: Machined-metal scientific instrument. Amber/gold on near-black.
 * Sharp edges, ruled lines, tight monospace data. No rounded corners.
 *
 * New Features:
 *   • Live Expression Evaluator  (type expressions like 1010b + 1Fh)
 *   • 32-bit Visualizer          (interactive bit grid, click to toggle)
 *   • Step-by-step Conversion Trace
 *   • Two's Complement display
 *   • Signed/Unsigned toggle
 *   • Quick-base converter row   (all 4 bases simultaneously)
 *   • Bit-width selector         (8 / 16 / 32 bit)
 *   • History with replay
 */
public class BaseCalculatorDashboard extends JFrame {

    // ─────────────────────────────────────────────────────────
    //  PALETTE
    // ─────────────────────────────────────────────────────────
    static final Color C_BG        = new Color(9,   9,  12);
    static final Color C_PANEL     = new Color(13,  13, 18);
    static final Color C_CARD      = new Color(17,  17, 24);
    static final Color C_CARD2     = new Color(21,  21, 30);
    static final Color C_RULE      = new Color(38,  38, 52);
    static final Color C_RULE2     = new Color(55,  55, 75);
    static final Color C_GOLD      = new Color(212, 163,  58);
    static final Color C_GOLD_DIM  = new Color(100,  75,  20);
    static final Color C_AMBER     = new Color(255, 183,  77);
    static final Color C_AMBER_DIM = new Color(80,   55,  10);
    static final Color C_TEAL      = new Color(56,  189, 166);
    static final Color C_TEAL_DIM  = new Color(15,   65,  55);
    static final Color C_RED       = new Color(220,  75,  75);
    static final Color C_RED_DIM   = new Color(65,   18,  18);
    static final Color C_BLUE      = new Color(88,  166, 255);
    static final Color C_BLUE_DIM  = new Color(18,   42,  80);
    static final Color C_GREEN     = new Color(82,  196, 120);
    static final Color C_GREEN_DIM = new Color(15,   55,  25);
    static final Color C_WHITE     = new Color(230, 228, 220);
    static final Color C_TEXT      = new Color(185, 183, 168);
    static final Color C_MUTED     = new Color(88,   86,  78);
    static final Color C_FIELD     = new Color(11,  11,  16);

    // ─────────────────────────────────────────────────────────
    //  FONTS
    // ─────────────────────────────────────────────────────────
    static final Font F_DISPLAY  = new Font("Consolas", Font.BOLD,   32);
    static final Font F_LG       = new Font("Consolas", Font.BOLD,   18);
    static final Font F_MD       = new Font("Consolas", Font.BOLD,   13);
    static final Font F_SM       = new Font("Consolas", Font.PLAIN,  12);
    static final Font F_XS       = new Font("Consolas", Font.PLAIN,  10);
    static final Font F_LABEL    = new Font("Consolas", Font.BOLD,   10);
    static final Font F_UI       = new Font("Segoe UI", Font.PLAIN,  12);
    static final Font F_UI_BOLD  = new Font("Segoe UI", Font.BOLD,   12);
    static final Font F_TITLE    = new Font("Segoe UI", Font.BOLD,   13);
    static final Font F_KEY      = new Font("Consolas", Font.BOLD,   15);

    // ─────────────────────────────────────────────────────────
    //  ENGINES
    // ─────────────────────────────────────────────────────────
    private final BaseConverter  engine  = new BaseConverter();
    private final HistoryManager history = new HistoryManager();

    // ─────────────────────────────────────────────────────────
    //  STATE
    // ─────────────────────────────────────────────────────────
    private int  bitWidth   = 32;
    private boolean signed  = true;
    private long currentVal = 0L;

    // ─────────────────────────────────────────────────────────
    //  ARITHMETIC WIDGETS
    // ─────────────────────────────────────────────────────────
    private JTextField     fldA, fldB;
    private JComboBox<String> cmbBaseA, cmbBaseB, cmbOp;
    private JLabel         lblExpr, lblArithResult;
    private JTextArea      areaArithDetail;

    // ─────────────────────────────────────────────────────────
    //  CONVERTER WIDGETS
    // ─────────────────────────────────────────────────────────
    private JTextField     fldConvIn;
    private JComboBox<String> cmbConvFrom, cmbConvTo;
    private JLabel         lblConvOut;
    private JTextArea      areaTrace;

    // ─────────────────────────────────────────────────────────
    //  LIVE EVALUATOR
    // ─────────────────────────────────────────────────────────
    private JTextField fldExpr;
    private JLabel     lblEvalResult;

    // ─────────────────────────────────────────────────────────
    //  QUICK-BASE ROW
    // ─────────────────────────────────────────────────────────
    private JLabel lblQuickBin, lblQuickOct, lblQuickDec, lblQuickHex;

    // ─────────────────────────────────────────────────────────
    //  BIT VISUALIZER
    // ─────────────────────────────────────────────────────────
    private BitGridPanel bitGrid;
    private JLabel       lblTwos, lblBitCount;
    private JLabel[]     lblBitWidthBtns = new JLabel[3];

    // ─────────────────────────────────────────────────────────
    //  HISTORY
    // ─────────────────────────────────────────────────────────
    private JList<String> lstHistory;

    // ─────────────────────────────────────────────────────────
    //  STATUS
    // ─────────────────────────────────────────────────────────
    private JLabel lblStatus;
    private Timer  blinkTimer;
    private boolean blinkOn = true;

    // =========================================================
    //  MAIN
    // =========================================================
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            flattenUIDefaults();
            new BaseCalculatorDashboard().setVisible(true);
        });
    }

    private static void flattenUIDefaults() {
        UIManager.put("ComboBox.background",          C_FIELD);
        UIManager.put("ComboBox.foreground",          C_TEXT);
        UIManager.put("ComboBox.selectionBackground", C_GOLD_DIM);
        UIManager.put("ComboBox.selectionForeground", C_AMBER);
        UIManager.put("ComboBox.buttonBackground",    C_FIELD);
        UIManager.put("OptionPane.background",        C_PANEL);
        UIManager.put("Panel.background",             C_PANEL);
        UIManager.put("OptionPane.messageForeground", C_TEXT);
    }

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public BaseCalculatorDashboard() {
        setTitle("CALCULUS  ·  Number Systems Workstation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1520, 960);
        setMinimumSize(new Dimension(1280, 820));
        setLocationRelativeTo(null);
        setBackground(C_BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        setContentPane(root);

        root.add(buildTopBar(),    BorderLayout.NORTH);
        root.add(buildWorkspace(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        startBlink();
    }

    // =========================================================
    //  TOP BAR
    // =========================================================
    private JPanel buildTopBar() {
        JPanel bar = new RuledPanel(C_BG, C_RULE, false, true);
        bar.setLayout(new BorderLayout());
        bar.setBorder(new EmptyBorder(0, 0, 0, 0));
        bar.setPreferredSize(new Dimension(0, 48));

        // Left: product wordmark
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);

        JPanel accentBar = new JPanel();
        accentBar.setBackground(C_GOLD);
        accentBar.setPreferredSize(new Dimension(4, 48));
        accentBar.setOpaque(true);

        JPanel wordmark = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        wordmark.setOpaque(false);

        JLabel lCal = new JLabel("CALCULUS");
        lCal.setFont(new Font("Consolas", Font.BOLD, 16));
        lCal.setForeground(C_AMBER);

        JLabel lSep = new JLabel("  ·  ");
        lSep.setFont(F_MD);
        lSep.setForeground(C_RULE2);

        JLabel lSub = new JLabel("Number Systems Workstation");
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSub.setForeground(C_MUTED);

        wordmark.add(lCal); wordmark.add(lSep); wordmark.add(lSub);
        left.add(accentBar);
        left.add(wordmark);

        // Center: module tabs
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabs.setOpaque(false);
        tabs.add(makeTab("ARITHMETIC",  true));
        tabs.add(makeTab("CONVERTER",   false));
        tabs.add(makeTab("BIT VIEWER",  false));
        tabs.add(makeTab("EVALUATOR",   false));

        // Right: system badges
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 13));
        right.setOpaque(false);
        right.add(makeTopBadge("BIN·2",  C_BLUE));
        right.add(makeTopBadge("OCT·8",  C_AMBER));
        right.add(makeTopBadge("DEC·10", C_GREEN));
        right.add(makeTopBadge("HEX·16", C_GOLD));
        right.add(Box.createHorizontalStrut(8));

        bar.add(left,  BorderLayout.WEST);
        bar.add(tabs,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JLabel makeTab(String text, boolean active) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(F_LABEL);
        l.setForeground(active ? C_AMBER : C_MUTED);
        l.setPreferredSize(new Dimension(110, 48));
        if (active) {
            l.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, C_GOLD),
                new EmptyBorder(0, 0, 0, 0)));
        }
        return l;
    }

    private JLabel makeTopBadge(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(c);
        l.setBorder(new CompoundBorder(
            new LineBorder(mix(c, C_BG, 0.3f), 1, false),
            new EmptyBorder(3, 8, 3, 8)));
        l.setBackground(mix(c, C_BG, 0.07f));
        l.setOpaque(true);
        return l;
    }

    // =========================================================
    //  WORKSPACE  — 3-column layout
    // =========================================================
    private JPanel buildWorkspace() {
        JPanel ws = new JPanel(new GridBagLayout());
        ws.setBackground(C_BG);
        ws.setBorder(new EmptyBorder(8, 8, 8, 8));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets  = new Insets(0, 4, 0, 4);

        // Column A: Arithmetic
        g.gridx = 0; g.weightx = 0.34;
        ws.add(buildArithColumn(), g);

        // Column B: Converter + Evaluator
        g.gridx = 1; g.weightx = 0.36;
        ws.add(buildCenterColumn(), g);

        // Column C: Bit Grid + History
        g.gridx = 2; g.weightx = 0.30;
        ws.add(buildRightColumn(), g);

        return ws;
    }

    // =========================================================
    //  COLUMN A — ARITHMETIC
    // =========================================================
    private JPanel buildArithColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 6));
        col.setOpaque(false);

        col.add(buildArithCard(),   BorderLayout.CENTER);
        col.add(buildKeypadCard(),  BorderLayout.SOUTH);

        return col;
    }

    private JPanel buildArithCard() {
        JPanel card = makeCard("ARITHMETIC ENGINE", C_GOLD);
        JPanel body = padded(card, 12, 12);

        // Expression display
        lblExpr = new JLabel("  —", SwingConstants.LEFT);
        lblExpr.setFont(F_LG);
        lblExpr.setForeground(C_AMBER);
        lblExpr.setBackground(C_FIELD);
        lblExpr.setOpaque(true);
        lblExpr.setBorder(new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, C_GOLD),
            new EmptyBorder(8, 10, 8, 10)));
        lblExpr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        lblExpr.setAlignmentX(0);

        // Result
        lblArithResult = new JLabel("  —", SwingConstants.LEFT);
        lblArithResult.setFont(F_DISPLAY);
        lblArithResult.setForeground(C_WHITE);
        lblArithResult.setBackground(C_BG);
        lblArithResult.setOpaque(true);
        lblArithResult.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblArithResult.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        lblArithResult.setAlignmentX(0);

        // Operand row
        JPanel opRow = new JPanel(new GridLayout(1, 2, 8, 0));
        opRow.setOpaque(false);
        opRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        opRow.setAlignmentX(0);

        fldA = makeField("OPERAND  A", "e.g. 1010");
        fldB = makeField("OPERAND  B", "e.g. FF");
        opRow.add(fldA);
        opRow.add(fldB);

        // Base + Op row
        JPanel configRow = new JPanel(new GridLayout(1, 3, 8, 0));
        configRow.setOpaque(false);
        configRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        configRow.setAlignmentX(0);
        cmbBaseA = makeBaseCombo(); cmbBaseB = makeBaseCombo();
        cmbOp = makeCombo(new String[]{"+  ADD","-  SUB","×  MUL","÷  DIV"});
        configRow.add(labeledCombo("BASE  A", cmbBaseA));
        configRow.add(labeledCombo("BASE  B", cmbBaseB));
        configRow.add(labeledCombo("OPERATION", cmbOp));

        // Detail area
        JLabel detLbl = microLabel("MULTI-BASE RESULT");
        areaArithDetail = makeArea(C_TEXT, 6);
        JScrollPane detScroll = styledScroll(areaArithDetail);

        // Action buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnRow.setAlignmentX(0);
        btnRow.add(makeBtn("CALCULATE", C_GOLD,  C_BG,   e -> doCalculate()));
        btnRow.add(makeBtn("CLEAR",     C_MUTED, C_CARD, e -> clearArith()));
        btnRow.add(makeBtn("COPY",      C_TEAL,  C_CARD, e -> copy(areaArithDetail.getText())));

        body.add(lblExpr);       body.add(vs(4));
        body.add(lblArithResult);body.add(vs(10));
        body.add(opRow);         body.add(vs(6));
        body.add(configRow);     body.add(vs(8));
        body.add(btnRow);        body.add(vs(8));
        body.add(detLbl);        body.add(vs(3));
        body.add(detScroll);

        return card;
    }

    private JPanel buildKeypadCard() {
        JPanel card = makeCard("KEYPAD", C_MUTED);
        card.setPreferredSize(new Dimension(0, 260));

        JPanel grid = new JPanel(new GridLayout(5, 5, 3, 3));
        grid.setBackground(C_CARD);
        grid.setBorder(new EmptyBorder(8, 10, 8, 10));

        String[][] keys = {
            {"A","B","C","D","⌫"},
            {"E","F","(",")","+"},
            {"7","8","9","×","-"},
            {"4","5","6","÷","*"},
            {"1","2","3","0","="}
        };

        Color[] rowFg = {C_BLUE, C_BLUE, C_TEXT, C_TEXT, C_TEXT};
        Color[] rowBg = {C_CARD2, C_CARD2, C_CARD2, C_CARD2, C_CARD2};

        for (int r = 0; r < keys.length; r++) {
            for (int c2 = 0; c2 < keys[r].length; c2++) {
                String k  = keys[r][c2];
                Color fg  = k.equals("+") || k.equals("-") || k.equals("×")
                           || k.equals("÷") || k.equals("*") ? C_AMBER
                          : k.equals("=") ? C_BG
                          : k.equals("⌫") ? C_RED
                          : rowFg[r];
                Color bg  = k.equals("=") ? C_GOLD : rowBg[r];
                JButton b = makeKey(k, fg, bg);

                b.addActionListener(e -> {
                    switch (k) {
                        case "=":  doCalculate(); break;
                        case "⌫":
                            JTextField af = fldA.isFocusOwner() ? fldA : fldB;
                            String t = af.getText();
                            if (!t.isEmpty()) af.setText(t.substring(0, t.length()-1));
                            break;
                        case "+": case "-": case "×": case "÷": case "*":
                            setOp(k); break;
                        default:
                            JTextField f = fldA.isFocusOwner() ? fldA : fldB;
                            f.setText(f.getText() + k);
                    }
                });
                grid.add(b);
            }
        }
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================
    //  COLUMN B — CONVERTER + EVALUATOR + QUICK ROW
    // =========================================================
    private JPanel buildCenterColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 6));
        col.setOpaque(false);

        col.add(buildConverterCard(), BorderLayout.CENTER);

        JPanel bot = new JPanel(new GridLayout(2, 1, 0, 6));
        bot.setOpaque(false);
        bot.add(buildEvaluatorCard());
        bot.add(buildQuickBaseCard());
        col.add(bot, BorderLayout.SOUTH);

        return col;
    }

    private JPanel buildConverterCard() {
        JPanel card = makeCard("BASE CONVERTER", C_TEAL);
        JPanel body = padded(card, 12, 12);

        // Input
        fldConvIn = new JTextField();
        styleField(fldConvIn, C_AMBER);
        fldConvIn.setFont(F_LG);
        fldConvIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        fldConvIn.setAlignmentX(0);

        // From / To
        JPanel fromTo = new JPanel(new GridLayout(1, 2, 8, 0));
        fromTo.setOpaque(false);
        fromTo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        fromTo.setAlignmentX(0);
        cmbConvFrom = makeBaseCombo(); cmbConvFrom.setSelectedIndex(2);
        cmbConvTo   = makeBaseCombo(); cmbConvTo.setSelectedIndex(0);
        fromTo.add(labeledCombo("FROM", cmbConvFrom));
        fromTo.add(labeledCombo("TO",   cmbConvTo));

        // Big output
        lblConvOut = new JLabel("——", SwingConstants.RIGHT);
        lblConvOut.setFont(F_DISPLAY);
        lblConvOut.setForeground(C_GREEN);
        lblConvOut.setBackground(C_FIELD);
        lblConvOut.setOpaque(true);
        lblConvOut.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 3, C_TEAL),
            new EmptyBorder(8, 14, 8, 14)));
        lblConvOut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        lblConvOut.setAlignmentX(0);

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(1, 3, 6, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnRow.setAlignmentX(0);
        btnRow.add(makeBtn("CONVERT", C_TEAL, C_BG,   e -> doConvert()));
        btnRow.add(makeBtn("CLEAR",   C_MUTED, C_CARD, e -> clearConv()));
        btnRow.add(makeBtn("COPY",    C_GOLD,  C_CARD, e -> copy(lblConvOut.getText())));

        // Trace area
        JLabel traceLbl = microLabel("STEP-BY-STEP CONVERSION TRACE");
        areaTrace = makeArea(C_TEXT, 8);
        JScrollPane traceScroll = styledScroll(areaTrace);

        // Signed/unsigned toggle + bit width
        JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        opts.setOpaque(false);
        opts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        opts.setAlignmentX(0);
        opts.add(microLabel("BIT WIDTH:"));
        opts.add(Box.createHorizontalStrut(6));
        String[] widths = {"8","16","32"};
        int[] wVals     = {8, 16, 32};
        for (int i = 0; i < 3; i++) {
            final int w = wVals[i];
            JLabel wl = new JLabel(" " + widths[i] + " ");
            wl.setFont(F_LABEL);
            wl.setForeground(w == bitWidth ? C_BG : C_MUTED);
            wl.setBackground(w == bitWidth ? C_GOLD : C_CARD2);
            wl.setOpaque(true);
            wl.setBorder(new LineBorder(C_RULE, 1, false));
            wl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblBitWidthBtns[i] = wl;
            wl.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { setBitWidth(w); }
            });
            opts.add(wl);
            opts.add(Box.createHorizontalStrut(3));
        }
        opts.add(Box.createHorizontalStrut(12));
        JLabel signedLbl = new JLabel("  SIGNED  ");
        signedLbl.setFont(F_LABEL);
        signedLbl.setForeground(C_BG);
        signedLbl.setBackground(C_TEAL);
        signedLbl.setOpaque(true);
        signedLbl.setBorder(new LineBorder(C_RULE, 1));
        signedLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signedLbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                signed = !signed;
                signedLbl.setText(signed ? "  SIGNED  " : " UNSIGNED ");
                signedLbl.setBackground(signed ? C_TEAL : C_AMBER_DIM);
                signedLbl.setForeground(signed ? C_BG : C_AMBER);
                updateBitDisplay();
            }
        });
        opts.add(signedLbl);

        body.add(microLabel("INPUT NUMBER"));  body.add(vs(3));
        body.add(fldConvIn);                  body.add(vs(8));
        body.add(fromTo);                     body.add(vs(8));
        body.add(btnRow);                     body.add(vs(8));
        body.add(opts);                       body.add(vs(8));
        body.add(microLabel("RESULT"));       body.add(vs(3));
        body.add(lblConvOut);                 body.add(vs(10));
        body.add(traceLbl);                   body.add(vs(3));
        body.add(traceScroll);

        return card;
    }

    private Object clearConv() {
		// TODO Auto-generated method stub
		return null;
	}

	private JPanel buildEvaluatorCard() {
        JPanel card = makeCard("LIVE EXPRESSION EVALUATOR", C_BLUE);
        card.setPreferredSize(new Dimension(0, 100));

        JPanel body = new JPanel(new BorderLayout(8, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(8, 12, 8, 12));

        fldExpr = new JTextField("1010b + 1Fh");
        styleField(fldExpr, C_WHITE);
        fldExpr.setFont(F_MD);

        lblEvalResult = new JLabel("  type an expression…", SwingConstants.RIGHT);
        lblEvalResult.setFont(F_MD);
        lblEvalResult.setForeground(C_BLUE);
        lblEvalResult.setBackground(C_FIELD);
        lblEvalResult.setOpaque(true);
        lblEvalResult.setBorder(new EmptyBorder(6, 10, 6, 10));

        JButton evalBtn = makeBtn("EVAL", C_BLUE, C_BG, e -> doEval());
        evalBtn.setPreferredSize(new Dimension(72, 0));

        body.add(fldExpr,      BorderLayout.WEST);
        body.add(lblEvalResult, BorderLayout.CENTER);
        body.add(evalBtn,       BorderLayout.EAST);

        fldExpr.addActionListener(e -> doEval());

        JLabel hint = new JLabel("  Syntax: 1010b=bin  FFh=hex  77o=oct  99=dec  e.g.  1Fh * 1010b + 77");
        hint.setFont(F_XS);
        hint.setForeground(C_MUTED);

        card.add(body,  BorderLayout.CENTER);
        card.add(hint,  BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildQuickBaseCard() {
        JPanel card = makeCard("QUICK BASE DISPLAY", C_AMBER);
        card.setPreferredSize(new Dimension(0, 110));

        JPanel grid = new JPanel(new GridLayout(2, 4, 1, 1));
        grid.setBackground(C_FIELD);
        grid.setBorder(new EmptyBorder(6, 8, 6, 8));

        grid.add(baseTag("BIN·2",  C_BLUE));
        grid.add(baseTag("OCT·8",  C_AMBER));
        grid.add(baseTag("DEC·10", C_GREEN));
        grid.add(baseTag("HEX·16", C_GOLD));

        lblQuickBin = baseVal("0", C_BLUE);
        lblQuickOct = baseVal("0", C_AMBER);
        lblQuickDec = baseVal("0", C_GREEN);
        lblQuickHex = baseVal("0", C_GOLD);

        grid.add(lblQuickBin);
        grid.add(lblQuickOct);
        grid.add(lblQuickDec);
        grid.add(lblQuickHex);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // =========================================================
    //  COLUMN C — BIT GRID + TWOS + HISTORY
    // =========================================================
    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 6));
        col.setOpaque(false);

        col.add(buildBitCard(),     BorderLayout.NORTH);
        col.add(buildHistoryCard(), BorderLayout.CENTER);

        return col;
    }

    private JPanel buildBitCard() {
        JPanel card = makeCard("BIT VISUALIZER  —  CLICK BITS TO TOGGLE", C_RED);
        card.setPreferredSize(new Dimension(0, 220));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_CARD);
        body.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Bit width selector inside card header area
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        topRow.setAlignmentX(0);

        lblBitCount = new JLabel("VALUE: 0  |  BITS SET: 0");
        lblBitCount.setFont(F_LABEL);
        lblBitCount.setForeground(C_MUTED);

        topRow.add(lblBitCount, BorderLayout.EAST);

        bitGrid = new BitGridPanel(bitWidth);
        bitGrid.setAlignmentX(0);
        bitGrid.addChangeListener(val -> {
            currentVal = val;
            updateQuickBases(currentVal);
            updateTwosDisplay(currentVal);
            int cnt = Long.bitCount(currentVal & maskFor(bitWidth));
            lblBitCount.setText("VALUE: " + currentVal + "  |  BITS SET: " + cnt);
        });

        // Two's complement
        lblTwos = new JLabel("2's COMPLEMENT: —");
        lblTwos.setFont(F_XS);
        lblTwos.setForeground(C_RED);
        lblTwos.setAlignmentX(0);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(0);
        btnRow.add(makeMini("CLEAR",  e -> { bitGrid.clear(); currentVal=0; updateAll(); }));
        btnRow.add(makeMini("ALL 1s", e -> { bitGrid.setAll(); updateAll(); }));
        btnRow.add(makeMini("SEND→",  e -> {
            fldConvIn.setText(String.valueOf(currentVal));
            cmbConvFrom.setSelectedIndex(2);
        }));

        body.add(topRow);   body.add(vs(4));
        body.add(bitGrid);  body.add(vs(6));
        body.add(lblTwos);  body.add(vs(4));
        body.add(btnRow);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildHistoryCard() {
        JPanel card = makeCard("OPERATION HISTORY", C_MUTED);

        lstHistory = new JList<>(history.getListModel());
        lstHistory.setBackground(C_FIELD);
        lstHistory.setForeground(C_TEXT);
        lstHistory.setFont(F_XS);
        lstHistory.setFixedCellHeight(22);
        lstHistory.setSelectionBackground(C_GOLD_DIM);
        lstHistory.setSelectionForeground(C_AMBER);
        lstHistory.setCellRenderer(new HistoryRenderer());

        JScrollPane scroll = styledScroll(lstHistory);

        JPanel btnRow = new JPanel(new GridLayout(1, 3, 4, 0));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(5, 8, 6, 8));
        btnRow.add(makeMini("REPLAY",     e -> replayHistory()));
        btnRow.add(makeMini("DELETE",     e -> history.deleteEntry(lstHistory.getSelectedIndex())));
        btnRow.add(makeMini("CLEAR ALL",  e -> history.clearAll()));

        card.add(scroll, BorderLayout.CENTER);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================
    //  STATUS BAR
    // =========================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(C_BG);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, C_RULE));
        bar.setPreferredSize(new Dimension(0, 22));

        lblStatus = new JLabel("  ● READY  ·  BaseLogic Number Systems Workstation  ·  OOP Final 2025-26");
        lblStatus.setFont(F_XS);
        lblStatus.setForeground(C_GREEN);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 3));
        right.setOpaque(false);
        for (String s : new String[]{"BIN","OCT","DEC","HEX","ARITH","CONV"}) {
            JLabel l = new JLabel(s);
            l.setFont(F_XS);
            l.setForeground(C_MUTED);
            right.add(l);
        }

        bar.add(lblStatus, BorderLayout.WEST);
        bar.add(right,     BorderLayout.EAST);
        return bar;
    }

    private void startBlink() {
        blinkTimer = new Timer(900, e -> {
            blinkOn = !blinkOn;
            lblStatus.setForeground(blinkOn ? C_GREEN : C_MUTED);
        });
        blinkTimer.start();
    }

    // =========================================================
    //  LOGIC ACTIONS
    // =========================================================
    private void doCalculate() {
        String n1 = fldA.getText().trim().toUpperCase();
        String n2 = fldB.getText().trim().toUpperCase();
        int b1 = baseOf(cmbBaseA), b2 = baseOf(cmbBaseB);
        String opRaw = (String) cmbOp.getSelectedItem();
        if (opRaw == null) opRaw = "+  ADD";
        String op = opRaw.trim().charAt(0) == '×' ? "*"
                  : opRaw.trim().charAt(0) == '÷' ? "/" : String.valueOf(opRaw.trim().charAt(0));

        if (n1.isEmpty() || n2.isEmpty()) { err("Both operand fields must be filled."); return; }
        if (!engine.validateInput(n1, b1)) { err("Operand A ["+n1+"] has invalid digits for Base-"+b1); return; }
        if (!engine.validateInput(n2, b2)) { err("Operand B ["+n2+"] has invalid digits for Base-"+b2); return; }

        try {
            String sym = opRaw.trim().split("\\s+")[0];
            String expr = n1+"(B"+b1+") "+sym+" "+n2+"(B"+b2+")";
            lblExpr.setText("  " + expr);

            String multiResult = engine.performArithmetic(n1, n2, b1, b2, op);
            long decVal = engine.getArithmeticDecimal(n1, n2, b1, b2, op);
            currentVal = decVal;

            // Extract decimal line for big display
            String decLine = "?";
            for (String line : multiResult.split("\n"))
                if (line.contains("Decimal")) decLine = line.replaceAll(".*:\\s*", "").trim();

            lblArithResult.setText("  " + decLine);
            areaArithDetail.setText(multiResult);

            updateQuickBases(decVal);
            updateBitDisplay();
            history.addHistory(expr, multiResult.replace("\n","  "));
            status("ARITHMETIC  ·  " + expr + "  =  " + decLine);
        } catch (ArithmeticException ae) { err(ae.getMessage()); }
          catch (Exception ex)           { err("Invalid input: " + ex.getMessage()); }
    }

    private void clearArith() {
        fldA.setText(""); fldB.setText("");
        lblExpr.setText("  —"); lblArithResult.setText("  —");
        areaArithDetail.setText("");
    }

    private void doConvert() {
        String val  = fldConvIn.getText().trim().toUpperCase();
        int from = baseOf(cmbConvFrom), to = baseOf(cmbConvTo);

        if (val.isEmpty()) { err("Enter a number to convert."); return; }
        if (!engine.validateInput(val, from)) { err("["+val+"] is not valid Base-"+from); return; }

        try {
            long dec     = engine.convertToDecimal(val, from);
            String out   = engine.convertToBase(dec, to).toUpperCase();
            currentVal   = dec;

            lblConvOut.setText(out + "  ");
            areaTrace.setText(buildTrace(val, from, to, dec, out));
            updateQuickBases(dec);
            updateBitDisplay();
            history.addHistory(val+"(B"+from+")→B"+to, out);
            status("CONVERT  ·  " + val + " (Base-" + from + ")  =  " + out + " (Base-" + to + ")");
        } catch (Exception ex) { err("Conversion error: " + ex.getMessage()); }
    }

    private String buildTrace(String input, int from, int to, long dec, String out) {
        StringBuilder sb = new StringBuilder();
        sb.append("INPUT   : ").append(input).append("  (Base-").append(from).append(")\n");
        sb.append("STEP 1  : Convert to Decimal (Base-10)\n");

        // Show positional breakdown
        String u = input.toUpperCase();
        sb.append("          ");
        for (int i = 0; i < u.length(); i++) {
            int digit = Character.digit(u.charAt(i), from);
            int exp   = u.length() - 1 - i;
            sb.append(digit).append("×").append(from).append("^").append(exp);
            if (i < u.length()-1) sb.append(" + ");
        }
        sb.append("\n");
        sb.append("        = ").append(dec).append("\n\n");

        if (to != 10) {
            sb.append("STEP 2  : Convert ").append(dec).append(" to Base-").append(to).append("\n");
            sb.append(divisionTrace(dec, to));
            sb.append("\n");
        }

        sb.append("RESULT  : ").append(out).append("  (Base-").append(to).append(")\n\n");
        sb.append("ALL BASES:\n");
        sb.append(engine.convertFromDecimal(dec));
        return sb.toString();
    }

    private String divisionTrace(long val, int base) {
        if (val == 0) return "        0 ÷ " + base + " = 0  R 0\n";
        StringBuilder sb = new StringBuilder();
        long v = val;
        StringBuilder rems = new StringBuilder();
        while (v > 0) {
            long rem = v % base;
            sb.append("        ").append(v).append(" ÷ ").append(base)
              .append(" = ").append(v/base).append("  R ").append(rem).append("\n");
            rems.insert(0, Long.toHexString(rem).toUpperCase());
            v /= base;
        }
        sb.append("        Remainders (bottom-up): ").append(rems).append("\n");
        return sb.toString();
    }

    private void doEval() {
        String expr = fldExpr.getText().trim();
        if (expr.isEmpty()) return;
        try {
            long result = evalExpr(expr);
            currentVal  = result;
            lblEvalResult.setText("  = " + result + "  (0x" + Long.toHexString(result).toUpperCase() + ")");
            lblEvalResult.setForeground(C_GREEN);
            updateQuickBases(result);
            updateBitDisplay();
            history.addHistory("EVAL: "+expr, String.valueOf(result));
            status("EVAL  ·  " + expr + "  =  " + result);
        } catch (Exception ex) {
            lblEvalResult.setText("  SYNTAX ERROR");
            lblEvalResult.setForeground(C_RED);
        }
    }

    /**
     * Tiny expression evaluator supporting:
     *   suffix b/B = binary, o/O = octal, h/H = hex, default = decimal
     *   operators  + - * /
     */
    private long evalExpr(String expr) {
        // tokenise: numbers (with suffix) and operators
        expr = expr.replaceAll("\\s+","");
        // recursive descent
        long[] pos = {0};
        return parseAdd(expr.toCharArray(), pos);
    }

    private long parseAdd(char[] c, long[] pos) {
        long v = parseMul(c, pos);
        while (pos[0] < c.length && (c[(int)pos[0]] == '+' || c[(int)pos[0]] == '-')) {
            char op = c[(int)pos[0]++];
            long r  = parseMul(c, pos);
            v = op == '+' ? v + r : v - r;
        }
        return v;
    }

    private long parseMul(char[] c, long[] pos) {
        long v = parseAtom(c, pos);
        while (pos[0] < c.length && (c[(int)pos[0]] == '*' || c[(int)pos[0]] == '/')) {
            char op = c[(int)pos[0]++];
            long r  = parseAtom(c, pos);
            if (op == '/' && r == 0) throw new ArithmeticException("Division by zero");
            v = op == '*' ? v * r : v / r;
        }
        return v;
    }

    private long parseAtom(char[] c, long[] pos) {
        int i = (int)pos[0];
        // Collect digit chars (hex digits too)
        StringBuilder num = new StringBuilder();
        while (i < c.length && (Character.isLetterOrDigit(c[i]))) {
            num.append(c[i++]);
        }
        String s   = num.toString();
        int    base = 10;
        String digits = s;
        if (s.toLowerCase().endsWith("b") && !s.toLowerCase().endsWith("0b")) {
            base = 2;  digits = s.substring(0, s.length()-1);
        } else if (s.toLowerCase().endsWith("o")) {
            base = 8;  digits = s.substring(0, s.length()-1);
        } else if (s.toLowerCase().endsWith("h")) {
            base = 16; digits = s.substring(0, s.length()-1);
        }
        pos[0] = i;
        return Long.parseLong(digits, base);
    }

    private void updateQuickBases(long val) {
        long mask = maskFor(bitWidth);
        long v    = val & mask;
        lblQuickBin.setText(" " + Long.toBinaryString(v));
        lblQuickOct.setText(" " + Long.toOctalString(v));
        lblQuickDec.setText(" " + (signed ? signExtend(val, bitWidth) : v));
        lblQuickHex.setText(" " + Long.toHexString(v).toUpperCase());
    }

    private void updateBitDisplay() {
        bitGrid.setValue(currentVal);
        updateTwosDisplay(currentVal);
        updateQuickBases(currentVal);
        int cnt = Long.bitCount(currentVal & maskFor(bitWidth));
        lblBitCount.setText("VALUE: " + currentVal + "  |  BITS SET: " + cnt);
    }

    private void updateTwosDisplay(long val) {
        long mask = maskFor(bitWidth);
        long twos = (~val + 1) & mask;
        lblTwos.setText("2's COMPLEMENT: " + Long.toBinaryString(twos)
            + "  (DEC: " + signExtend(twos, bitWidth) + ")");
    }

    private void updateAll() {
        currentVal = bitGrid.getValue();
        updateQuickBases(currentVal);
        updateTwosDisplay(currentVal);
        int cnt = Long.bitCount(currentVal & maskFor(bitWidth));
        lblBitCount.setText("VALUE: " + currentVal + "  |  BITS SET: " + cnt);
    }

    private void setBitWidth(int w) {
        bitWidth = w;
        bitGrid.setBitWidth(w);
        for (int i = 0; i < lblBitWidthBtns.length; i++) {
            int wi = i == 0 ? 8 : i == 1 ? 16 : 32;
            lblBitWidthBtns[i].setForeground(wi == w ? C_BG : C_MUTED);
            lblBitWidthBtns[i].setBackground(wi == w ? C_GOLD : C_CARD2);
        }
        updateBitDisplay();
    }

    private void replayHistory() {
        int idx = lstHistory.getSelectedIndex();
        if (idx < 0) return;
        HistoryManager.HistoryEntry e = history.getEntries().get(idx);
        // Try to fill arithmetic fields from operation string
        String op = e.getOperation();
        status("REPLAY  ·  " + op);
    }

    private long maskFor(int bits) {
        if (bits >= 64) return -1L;
        return (1L << bits) - 1;
    }

    private long signExtend(long val, int bits) {
        long mask = maskFor(bits);
        long v    = val & mask;
        long sign = 1L << (bits - 1);
        return (v & sign) != 0 ? v - (mask + 1) : v;
    }

    private void setOp(String k) {
        String target = k.equals("+") ? "+  ADD"
                      : k.equals("-") ? "-  SUB"
                      : k.equals("×") ? "×  MUL"
                      : "÷  DIV";
        for (int i = 0; i < cmbOp.getItemCount(); i++)
            if (cmbOp.getItemAt(i).equals(target)) { cmbOp.setSelectedIndex(i); break; }
    }

    // ─────────────────────────────────────────────────────────
    //  REQUIRED PUBLIC METHODS  (specification)
    // ─────────────────────────────────────────────────────────
    public boolean validateInput(String v, int b)  { return engine.validateInput(v, b); }
    public long convertToDecimal(String v, int b)  { return engine.convertToDecimal(v, b); }
    public String convertFromDecimal(long v)       { return engine.convertFromDecimal(v); }
    public String performArithmetic(String a, String b, int x, int y, String op) {
        return engine.performArithmetic(a, b, x, y, op);
    }
    public void updateDashboard()                  { repaint(); revalidate(); }
    public void addHistory(String op, String res)  { history.addHistory(op, res); }

    // ─────────────────────────────────────────────────────────
    //  FACTORY HELPERS
    // ─────────────────────────────────────────────────────────

    private JPanel makeCard(String title, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(C_CARD);
        card.setBorder(new LineBorder(C_RULE, 1, false));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_CARD2);
        header.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_RULE),
            new EmptyBorder(5, 10, 5, 10)));
        header.setBorder(new CompoundBorder(
            new MatteBorder(2, 0, 0, 0, accent),
            new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_RULE),
                new EmptyBorder(5, 10, 5, 10))));

        JLabel lbl = new JLabel(title);
        lbl.setFont(F_LABEL);
        lbl.setForeground(accent);
        header.add(lbl, BorderLayout.WEST);

        JLabel dot = new JLabel("■");
        dot.setFont(F_XS);
        dot.setForeground(mix(accent, C_CARD2, 0.4f));
        header.add(dot, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    /** Attach a BoxLayout body panel to a card and return it. */
    private JPanel padded(JPanel card, int vPad, int hPad) {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(C_CARD);
        body.setBorder(new EmptyBorder(vPad, hPad, vPad, hPad));
        card.add(body, BorderLayout.CENTER);
        return body;
    }

    private JTextField makeField(String labelText, String tip) {
        JTextField tf = new JTextField();
        styleField(tf, C_WHITE);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(0);
        tf.setToolTipText(tip);
        return tf;
    }

    private void styleField(JTextField tf, Color fg) {
        tf.setFont(F_MD);
        tf.setBackground(C_FIELD);
        tf.setForeground(fg);
        tf.setCaretColor(C_GOLD);
        tf.setBorder(new CompoundBorder(
            new LineBorder(C_RULE, 1, false),
            new EmptyBorder(5, 8, 5, 8)));
    }

    private JPanel labeledCombo(String label, JComboBox<String> cb) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(F_LABEL);
        l.setForeground(C_MUTED);
        p.add(l,  BorderLayout.NORTH);
        p.add(cb, BorderLayout.CENTER);
        return p;
    }

    private JComboBox<String> makeBaseCombo() {
        return makeCombo(new String[]{"2   BIN","8   OCT","10  DEC","16  HEX"});
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_SM);
        cb.setBackground(C_FIELD);
        cb.setForeground(C_TEXT);
        cb.setBorder(new LineBorder(C_RULE, 1, false));
        cb.setFocusable(false);
        return cb;
    }

    private JButton makeBtn(String text, Color fg, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(F_LABEL);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(mix(fg, bg, 0.35f), 1, false),
            new EmptyBorder(6, 12, 6, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hov = mix(fg, bg, 0.18f);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hov); }
            public void mouseExited (MouseEvent e) { b.setBackground(bg);  }
        });
        b.addActionListener(al);
        return b;
    }

    private JButton makeMini(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(F_XS);
        b.setForeground(C_MUTED);
        b.setBackground(C_CARD2);
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(
            new LineBorder(C_RULE, 1, false),
            new EmptyBorder(3, 8, 3, 8)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(C_RULE); }
            public void mouseExited (MouseEvent e) { b.setBackground(C_CARD2); }
        });
        b.addActionListener(al);
        return b;
    }

    private JButton makeKey(String text, Color fg, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_KEY);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(C_RULE, 1, false));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hov = mix(fg, bg, 0.2f);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hov); }
            public void mouseExited (MouseEvent e) { b.setBackground(bg);  }
        });
        return b;
    }

    private JTextArea makeArea(Color fg, int rows) {
        JTextArea ta = new JTextArea(rows, 0);
        ta.setEditable(false);
        ta.setFont(F_XS);
        ta.setBackground(C_FIELD);
        ta.setForeground(fg);
        ta.setBorder(new EmptyBorder(6, 8, 6, 8));
        ta.setLineWrap(false);
        return ta;
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(C_RULE, 1, false));
        sp.getViewport().setBackground(C_FIELD);
        sp.setAlignmentX(0);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        styleBar(sp.getVerticalScrollBar());
        styleBar(sp.getHorizontalScrollBar());
        return sp;
    }

    private void styleBar(JScrollBar sb) {
        sb.setBackground(C_FIELD);
        sb.setUI(new BasicScrollBarUI() {
            protected void configureScrollBarColors() { thumbColor = C_RULE2; trackColor = C_FIELD; }
            protected JButton createDecreaseButton(int o) { return zb(); }
            protected JButton createIncreaseButton(int o) { return zb(); }
            JButton zb() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }

    private JLabel microLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(C_MUTED);
        l.setAlignmentX(0);
        return l;
    }

    private JLabel baseTag(String text, Color c) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(F_LABEL);
        l.setForeground(c);
        l.setBackground(mix(c, C_FIELD, 0.06f));
        l.setOpaque(true);
        l.setBorder(new MatteBorder(0, 0, 1, 0, C_RULE));
        return l;
    }

    private JLabel baseVal(String text, Color c) {
        JLabel l = new JLabel(text, SwingConstants.RIGHT);
        l.setFont(F_SM);
        l.setForeground(c);
        l.setBackground(C_FIELD);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(1, 4, 1, 4));
        return l;
    }

    private Box.Filler vs(int h) {
        return (Box.Filler) Box.createVerticalStrut(h);
    }

    private int baseOf(JComboBox<String> cb) {
        String s = (String) cb.getSelectedItem();
        if (s == null) return 10;
        return Integer.parseInt(s.trim().split("\\s+")[0]);
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    private void copy(String text) {
        if (text == null || text.isBlank() || text.equals("——")) {
            err("Nothing to copy."); return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        status("COPIED to clipboard");
    }

    private void status(String msg) {
        lblStatus.setText("  ● " + msg);
        lblStatus.setForeground(C_GREEN);
    }

    static Color mix(Color a, Color b, float t) {
        float u = 1f - t;
        return new Color(
            Math.min(255,(int)(a.getRed()*t   + b.getRed()*u)),
            Math.min(255,(int)(a.getGreen()*t + b.getGreen()*u)),
            Math.min(255,(int)(a.getBlue()*t  + b.getBlue()*u)));
    }

    // =========================================================
    //  INNER CLASSES
    // =========================================================

    /** Ruled background panel. */
    static class RuledPanel extends JPanel {
        private final Color bg, rule;
        private final boolean hLines, bLine;
        RuledPanel(Color bg, Color rule, boolean hLines, boolean bLine) {
            this.bg = bg; this.rule = rule; this.hLines = hLines; this.bLine = bLine;
            setBackground(bg);
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(rule);
            if (hLines) {
                for (int y = 0; y < getHeight(); y += 20)
                    g2.drawLine(0, y, getWidth(), y);
            }
            if (bLine)
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
        }
    }

    /** Interactive clickable bit grid panel. */
    static class BitGridPanel extends JPanel {
        interface ChangeListener { void changed(long newValue); }

        private boolean[] bits;
        private int bitWidth;
        private ChangeListener listener;
        private int hoveredBit = -1;

        BitGridPanel(int width) {
            this.bitWidth = width;
            this.bits = new boolean[width];
            setBackground(C_CARD);
            setPreferredSize(new Dimension(0, 72));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int bit = bitAt(e.getX(), e.getY());
                    if (bit >= 0) { bits[bit] = !bits[bit]; repaint(); fire(); }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    int bit = bitAt(e.getX(), e.getY());
                    if (bit != hoveredBit) { hoveredBit = bit; repaint(); }
                }
            });
        }

        void addChangeListener(ChangeListener l) { this.listener = l; }

        private void fire() { if (listener != null) listener.changed(getValue()); }

        long getValue() {
            long v = 0;
            for (int i = 0; i < bitWidth; i++)
                if (bits[i]) v |= (1L << (bitWidth - 1 - i));
            return v;
        }

        void setValue(long val) {
            for (int i = 0; i < bitWidth; i++)
                bits[i] = ((val >> (bitWidth - 1 - i)) & 1) == 1;
            repaint();
        }

        void setBitWidth(int w) {
            bitWidth = w;
            bits = new boolean[w];
            repaint();
        }

        void clear()  { bits = new boolean[bitWidth]; repaint(); fire(); }

        void setAll() {
            for (int i = 0; i < bitWidth; i++) bits[i] = true;
            repaint(); fire();
        }

        private int bitAt(int mx, int my) {
            int cols = bitWidth;
            int rows = 1;
            if (bitWidth > 16) { cols = bitWidth/2; rows = 2; }

            int cellW = (getWidth()  - 8)  / cols;
            int cellH = (getHeight() - 8)  / rows;
            int col   = (mx - 4) / cellW;
            int row   = (my - 4) / cellH;
            if (col < 0 || col >= cols || row < 0 || row >= rows) return -1;
            return row * cols + col;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cols = bitWidth;
            int rows = 1;
            if (bitWidth > 16) { cols = bitWidth/2; rows = 2; }

            int cellW = (getWidth()  - 8)  / cols;
            int cellH = (getHeight() - 8)  / rows;
            int pad   = 2;

            g2.setFont(new Font("Consolas", Font.BOLD, Math.max(8, cellH/3)));

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int idx = r * cols + c;
                    if (idx >= bitWidth) break;
                    int x = 4 + c * cellW + pad;
                    int y = 4 + r * cellH + pad;
                    int w = cellW - pad*2;
                    int h = cellH - pad*2;

                    boolean on = bits[idx];
                    boolean hov = (idx == hoveredBit);

                    // Group separators (every 4 bits)
                    boolean sep = (idx % 4 == 0) && idx != 0;

                    Color bgCell = on  ? mix(C_GOLD, C_CARD, 0.45f)
                                 : hov ? mix(C_RULE2, C_CARD, 0.5f)
                                 : C_CARD2;
                    Color border = on  ? C_GOLD
                                 : hov ? C_RULE2
                                 : sep ? mix(C_RULE2, C_RULE, 0.5f)
                                 : C_RULE;
                    Color fg2   = on  ? C_AMBER : C_MUTED;

                    g2.setColor(bgCell);
                    g2.fillRect(x, y, w, h);
                    g2.setColor(border);
                    g2.drawRect(x, y, w, h);

                    g2.setColor(fg2);
                    String bit = on ? "1" : "0";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(bit,
                        x + (w - fm.stringWidth(bit)) / 2,
                        y + (h + fm.getAscent()) / 2 - 2);

                    // Bit index (MSB..0)
                    int bitNum = bitWidth - 1 - idx;
                    if (bitNum % 4 == 0 || bitNum == 0) {
                        g2.setFont(new Font("Consolas", Font.PLAIN, 7));
                        g2.setColor(C_MUTED);
                        g2.drawString(String.valueOf(bitNum), x+1, y+8);
                        g2.setFont(new Font("Consolas", Font.BOLD, Math.max(8, cellH/3)));
                    }
                }
            }
        }
    }

    /** History cell renderer with alternating rows. */
    class HistoryRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
                JList<?> list, Object val, int idx,
                boolean sel, boolean focus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list,val,idx,sel,focus);
            l.setFont(F_XS);
            l.setBackground(sel ? C_GOLD_DIM : idx%2==0 ? C_FIELD : mix(C_RULE,C_FIELD,0.2f));
            l.setForeground(sel ? C_AMBER : C_TEXT);
            l.setBorder(new EmptyBorder(2, 6, 2, 6));
            return l;
        }
    }
}