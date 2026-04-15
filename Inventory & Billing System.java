// ShopFlow – Inventory & Billing System
// Java Swing Mini Project
// Compile : mkdir bin & javac -d bin -encoding UTF-8 "Inventory & Billing System.java"
// Run     : java -cp bin InventoryManagementSystem

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

class InventoryManagementSystem {

    // ── Theme Colours ─────────────────────────────────────────────────
    static final Color BG    = new Color(245,246,250);
    static final Color SIDE  = new Color(49,46,129);
    static final Color CARD  = Color.WHITE;
    static final Color BRD   = new Color(220,225,240);
    static final Color ACC   = new Color(79,70,229);
    static final Color GRN   = new Color(22,163,74);
    static final Color RED   = new Color(220,38,38);
    static final Color YEL   = new Color(180,120,0);
    static final Color TXT   = new Color(15,23,42);
    static final Color GRY   = new Color(100,116,139);
    static final Color LGRY  = new Color(249,250,251);
    static final Color PURP  = new Color(124,58,237);

    // ── Fonts ─────────────────────────────────────────────────────────
    static final Font FH  = new Font("Segoe UI", Font.BOLD,  20);
    static final Font FH3 = new Font("Segoe UI", Font.BOLD,  13);
    static final Font FBD = new Font("Segoe UI", Font.BOLD,  12);
    static final Font FRG = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FSM = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Data Models ───────────────────────────────────────────────────
    static class Product implements Serializable {
        private static final long serialVersionUID = 1L;
        int id; String name, cat, barcode; double buyPrice, sellPrice, gstRate; int qty, min;
        Product(int i,String n,String c,String b,double bp,double sp,double gst,int q,int m){
            id=i;name=n;cat=c;barcode=b;buyPrice=bp;sellPrice=sp;gstRate=gst;qty=q;min=m;
        }
        boolean low(){return qty<=min;}
        double  val(){return buyPrice*qty;} // Inventory value is based on buy price
    }
    static class BillItem implements Serializable {
        private static final long serialVersionUID = 1L;
        Product p; int qty;
        BillItem(Product p,int q){this.p=p;qty=q;}
        double sub(){return p.sellPrice*qty;}
        double gst(){return sub() * (p.gstRate / 100.0);}
        double profit(){return (p.sellPrice - p.buyPrice) * qty;}
    }
    static class Sale implements Serializable {
        private static final long serialVersionUID = 1L;
        String no,date,customer; List<BillItem> items; double sub,gst,total,profit;
        String invoiceFile;
        Sale(String n,String d,String c,List<BillItem> i,double s,double g,double t,double pr){
            no=n;date=d;customer=c;items=new ArrayList<>(i);sub=s;gst=g;total=t;profit=pr;
        }
    }

    // ── Data Store ────────────────────────────────────────────────────
    static List<Product> products = new ArrayList<>();
    static List<Sale>    sales    = new ArrayList<>();
    static List<String>  customCategories = new ArrayList<>();
    static int PID=1, BILL=1001;

    @SuppressWarnings("unchecked")
    static void loadData() {
        File f = new File("shopflow_data.ser");
        if (f.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
                products = (List<Product>) ois.readObject();
                sales = (List<Sale>) ois.readObject();
                PID = ois.readInt();
                BILL = ois.readInt();
                try { customCategories = (List<String>) ois.readObject(); } catch (Exception ignored) {}
            } catch (Exception e) {
                System.err.println("Failed to load data: " + e.getMessage());
            }
        }
    }

    static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("shopflow_data.ser"))) {
            oos.writeObject(products);
            oos.writeObject(sales);
            oos.writeInt(PID);
            oos.writeInt(BILL);
            oos.writeObject(customCategories);
        } catch (Exception e) {
            System.err.println("Failed to save data: " + e.getMessage());
        }
    }

    static List<String> getCategories() {
        Set<String> set = new LinkedHashSet<>(Arrays.asList("Electronics", "Groceries", "Clothing", "Footwear", "Furniture", "Kitchen", "Stationery"));
        set.addAll(customCategories);
        for (Product p : products) set.add(p.cat);
        set.remove("Other");
        List<String> list = new ArrayList<>(set);
        list.add("Other");
        return list;
    }

    public static void main(String[] args){
        loadData();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveData()));
        SwingUtilities.invokeLater(()->new MainWin().setVisible(true));
    }

    // ── Shared Utilities ──────────────────────────────────────────────
    static String fmt(double v){return String.format("%,.2f",v);}
    static void   alert(String m){JOptionPane.showMessageDialog(null,m,"ShopFlow",JOptionPane.INFORMATION_MESSAGE);}
    static String rep(String s,int n){StringBuilder b=new StringBuilder();for(int i=0;i<n;i++)b.append(s);return b.toString();}
    static String safe(String s){return s.replace("\\","\\\\").replace("(","\\(").replace(")","\\)");}

    static JLabel lbl(String t,Font f,Color c){JLabel l=new JLabel(t);l.setFont(f);l.setForeground(c);return l;}

    static JButton btn(String t, Color bg){
        JButton b=new JButton(t); b.setFont(FBD); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14)); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); Color dk=bg.darker();
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(dk);}
            public void mouseExited (MouseEvent e){b.setBackground(bg);}
        }); return b;
    }
    static JTextField fld(String v){
        JTextField f=new JTextField(v); f.setFont(FRG);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BRD,1),
            BorderFactory.createEmptyBorder(6,9,6,9))); return f;
    }
    static JPanel card(){
        JPanel p=new JPanel(); p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BRD,1),
            BorderFactory.createEmptyBorder(14,14,14,14))); return p;
    }
    static JScrollPane scroll(Component c){
        JScrollPane s=new JScrollPane(c); s.setBorder(BorderFactory.createLineBorder(BRD));
        s.getViewport().setBackground(CARD); return s;
    }
    static void styleTable(JTable t){
        t.setFont(FRG); t.setRowHeight(33); t.setBackground(CARD); t.setForeground(TXT);
        t.setGridColor(BRD); t.setShowHorizontalLines(true); t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0,0));
        t.setSelectionBackground(new Color(238,242,255)); t.setSelectionForeground(TXT);
        t.setFillsViewportHeight(true); t.setRowSelectionAllowed(true);
        JTableHeader h=t.getTableHeader(); h.setFont(FBD);
        h.setBackground(new Color(248,250,252)); h.setForeground(GRY);
        h.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BRD)); h.setReorderingAllowed(false);
    }

    // ── PDF Generator (no external libraries) ─────────────────────────
    static void savePDF(String path, Sale s){
        try{
            StringBuilder cs=new StringBuilder();
            cs.append("BT\n/F2 15 Tf\n72 800 Td\n(SHOPFLOW  -  TAX INVOICE) Tj\n");
            cs.append("0 -26 Td\n/F1 10 Tf\n(Bill No  : #").append(safe(s.no)).append(") Tj\n");
            cs.append("0 -15 Td\n(Date     : ").append(safe(s.date)).append(") Tj\n");
            cs.append("0 -15 Td\n(Customer : ").append(safe(s.customer)).append(") Tj\n");
            cs.append("0 -20 Td\n(").append(rep("-",74)).append(") Tj\n");
            cs.append("/F2 10 Tf\n0 -14 Td\n(");
            cs.append(String.format("%-26s %11s %4s %15s %14s","PRODUCT","PRICE(Rs)","QTY","GST(Rs)","SUBTOTAL(Rs)"));
            cs.append(") Tj\n/F1 10 Tf\n0 -12 Td\n(").append(rep("-",74)).append(") Tj\n");
            for(BillItem bi:s.items){
                String nm=bi.p.name.length()>26?bi.p.name.substring(0,24)+"..":bi.p.name;
                String gstLabel=bi.p.gstRate+"%("+fmt(bi.gst())+")";
                String ln=String.format("%-26s %11s %4d %15s %14s",nm,fmt(bi.p.sellPrice),bi.qty,gstLabel,fmt(bi.sub()));
                cs.append("0 -14 Td\n(").append(safe(ln)).append(") Tj\n");
            }
            cs.append("0 -14 Td\n(").append(rep("-",74)).append(") Tj\n");
            cs.append("0 -15 Td\n(Subtotal      : Rs. ").append(fmt(s.sub)).append(") Tj\n");
            cs.append("0 -13 Td\n(Total GST     : Rs. ").append(fmt(s.gst)).append(") Tj\n");
            cs.append("0 -13 Td\n(").append(rep("=",74)).append(") Tj\n");
            cs.append("/F2 12 Tf\n0 -18 Td\n(GRAND TOTAL   : Rs. ").append(fmt(s.total)).append(") Tj\n");
            cs.append("/F1 10 Tf\n0 -32 Td\n(  Thank you for shopping with us!) Tj\nET\n");
            int cLen=cs.toString().getBytes("ISO-8859-1").length;
            String[] objs={
                "1 0 obj\n<</Type /Catalog /Pages 2 0 R>>\nendobj\n",
                "2 0 obj\n<</Type /Pages /Kids [3 0 R] /Count 1>>\nendobj\n",
                "3 0 obj\n<</Type /Page /Parent 2 0 R /MediaBox [0 0 595 842]\n"+
                "  /Contents 4 0 R /Resources <</Font <</F1 5 0 R /F2 6 0 R>>>>>>\nendobj\n",
                "4 0 obj\n<</Length "+cLen+">>\nstream\n"+cs+"endstream\nendobj\n",
                "5 0 obj\n<</Type /Font /Subtype /Type1 /BaseFont /Courier>>\nendobj\n",
                "6 0 obj\n<</Type /Font /Subtype /Type1 /BaseFont /Courier-Bold>>\nendobj\n"
            };
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] hdr="%PDF-1.4\n".getBytes("ISO-8859-1");
            out.write(hdr); int pos=hdr.length; int[] off=new int[objs.length];
            for(int i=0;i<objs.length;i++){off[i]=pos;byte[] b=objs[i].getBytes("ISO-8859-1");out.write(b);pos+=b.length;}
            int xpos=pos; StringBuilder xr=new StringBuilder();
            xr.append("xref\n0 ").append(objs.length+1).append("\n0000000000 65535 f \n");
            for(int o:off) xr.append(String.format("%010d 00000 n \n",o));
            xr.append("trailer\n<</Size ").append(objs.length+1).append(" /Root 1 0 R>>\n");
            xr.append("startxref\n").append(xpos).append("\n%%EOF\n");
            out.write(xr.toString().getBytes("ISO-8859-1"));
            Files.write(Paths.get(path),out.toByteArray());
            alert("PDF saved:\n"+new File(path).getAbsolutePath());
            try{Desktop.getDesktop().open(new File(path));}catch(Exception ign){}
        }catch(Exception ex){alert("PDF Error: "+ex.getMessage());}
    }

    // ── Main Window ───────────────────────────────────────────────────
    static MainWin mainWin;
    static class MainWin extends JFrame {
        CardLayout cl=new CardLayout(); JPanel deck=new JPanel(cl);
        JButton[] nav=new JButton[4];
        DashPanel dash; ProdPanel prod; BillPanel bill; SalePanel sale;

        MainWin(){
            mainWin=this;
            setTitle("ShopFlow  |  Inventory & Billing System");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(1180,720); setMinimumSize(new Dimension(980,600));
            setLocationRelativeTo(null); setLayout(new BorderLayout());
            add(buildSidebar(),BorderLayout.WEST);
            deck.setBackground(BG);
            dash=new DashPanel(); prod=new ProdPanel(); bill=new BillPanel(); sale=new SalePanel();
            deck.add(dash,"d"); deck.add(prod,"p"); deck.add(bill,"b"); deck.add(sale,"s");
            add(deck,BorderLayout.CENTER); go(0);
        }

        JPanel buildSidebar(){
            JPanel sb=new JPanel(new BorderLayout()); sb.setBackground(SIDE);
            sb.setPreferredSize(new Dimension(155,0));

            // Title strip
            JPanel ts=new JPanel(new BorderLayout()); ts.setBackground(SIDE);
            JLabel tl=lbl("ShopFlow",new Font("Segoe UI",Font.BOLD,13),Color.WHITE);
            tl.setBorder(BorderFactory.createEmptyBorder(16,14,14,14));
            ts.add(tl,BorderLayout.CENTER);
            JSeparator sep=new JSeparator(); sep.setForeground(new Color(67,56,200));
            ts.add(sep,BorderLayout.SOUTH);
            sb.add(ts,BorderLayout.NORTH);

            // Nav
            String[] lbs={"Dashboard","Products","Billing","Sales History"};
            JPanel np=new JPanel(); np.setBackground(SIDE);
            np.setLayout(new BoxLayout(np,BoxLayout.Y_AXIS));
            np.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
            for(int i=0;i<4;i++){
                final int ix=i;
                JButton b=new JButton(lbs[i]); b.setFont(FSM);
                b.setHorizontalAlignment(SwingConstants.LEFT);
                b.setBackground(SIDE); b.setForeground(new Color(196,181,253));
                b.setBorderPainted(false); b.setFocusPainted(false);
                b.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
                b.setMaximumSize(new Dimension(Integer.MAX_VALUE,32));
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.addActionListener(e->go(ix));
                nav[i]=b; np.add(b); np.add(Box.createVerticalStrut(2));
            }
            sb.add(np,BorderLayout.CENTER);
            return sb;
        }

        void go(int i){
            String[] k={"d","p","b","s"}; cl.show(deck,k[i]);
            if(i==0)dash.refresh(); if(i==2)bill.refreshProd(); if(i==3)sale.refresh();
            Color act=new Color(67,56,202),def=SIDE,aw=Color.WHITE,dw=new Color(196,181,253);
            for(int j=0;j<4;j++){
                nav[j].setBackground(j==i?act:def); nav[j].setForeground(j==i?aw:dw);
                nav[j].setFont(j==i?new Font("Segoe UI",Font.BOLD,11):FSM);
            }
        }
        void refreshAll(){dash.refresh();prod.refresh();sale.refresh();}
    }

    // ── Dashboard Panel ───────────────────────────────────────────────
    static class DashPanel extends JPanel {
        DashPanel(){setBackground(BG);refresh();}

        void refresh(){
            removeAll(); setLayout(new BorderLayout(0,12));
            setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            // Header row
            JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG);
            hdr.add(lbl("Dashboard",FH,TXT),BorderLayout.WEST);
            hdr.add(lbl(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),FSM,GRY),BorderLayout.EAST);
            hdr.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));

            // ── Stat cards (correct layout: NORTH so they don't stretch) ──
            long   low=products.stream().filter(Product::low).count();
            double inv=products.stream().mapToDouble(Product::val).sum();
            double rev=sales.stream().mapToDouble(s->s.total).sum();
            double prof=sales.stream().mapToDouble(s->s.profit).sum();

            JPanel stats=new JPanel(new GridLayout(1,5,10,0)); stats.setBackground(BG);
            stats.add(sCard("Total Products", String.valueOf(products.size()), ACC));
            stats.add(sCard("Inventory Value","Rs."+fmt(inv),                  GRN));
            stats.add(sCard("Low Stock",       String.valueOf(low),             low>0?RED:GRN));
            stats.add(sCard("Total Revenue",  "Rs."+fmt(rev),                  PURP));
            stats.add(sCard("Total Profit",   "Rs."+fmt(prof),                 new Color(16,185,129)));

            // NORTH = header + stat cards (fixed height)
            JPanel top=new JPanel(new BorderLayout(0,10)); top.setBackground(BG);
            top.add(hdr,BorderLayout.NORTH); top.add(stats,BorderLayout.CENTER);

            // CENTER = bottom cards (fills remaining space)
            JPanel bot=new JPanel(new GridLayout(1,2,10,0)); bot.setBackground(BG);
            bot.add(lowCard()); bot.add(recentCard());

            add(top,BorderLayout.NORTH); add(bot,BorderLayout.CENTER);
            revalidate(); repaint();
        }

        JPanel sCard(String title, String val, Color ac){
            JPanel c=new JPanel(); c.setBackground(CARD);
            c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));
            c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3,0,0,0,ac),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BRD,1),
                    BorderFactory.createEmptyBorder(12,14,12,14))));
            JLabel tl=lbl(title,FSM,GRY); tl.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel vl=lbl(val,new Font("Segoe UI",Font.BOLD,22),ac); vl.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.add(tl); c.add(Box.createVerticalStrut(6)); c.add(vl);
            return c;
        }

        JPanel lowCard(){
            JPanel c=card(); c.setLayout(new BorderLayout(0,8));
            JLabel t=lbl("Low Stock Alerts",FH3,RED); t.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
            c.add(t,BorderLayout.NORTH);
            JPanel list=new JPanel(); list.setBackground(CARD); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
            List<Product> lows=products.stream().filter(Product::low).collect(Collectors.toList());
            if(lows.isEmpty()){ list.add(lbl("All stock levels are OK",FRG,GRN)); }
            else for(Product p:lows){
                JPanel r=new JPanel(new BorderLayout(8,0)); r.setBackground(CARD);
                r.setBorder(BorderFactory.createEmptyBorder(4,0,4,0));
                r.add(lbl(p.name,FRG,TXT),BorderLayout.WEST);
                r.add(lbl("Qty: "+p.qty,FBD,RED),BorderLayout.EAST);
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE,r.getPreferredSize().height));
                list.add(r); list.add(new JSeparator());
            }
            JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(CARD);
            wrap.add(list, BorderLayout.NORTH);
            JScrollPane sp=new JScrollPane(wrap); sp.setBorder(null); sp.getViewport().setBackground(CARD);
            c.add(sp,BorderLayout.CENTER); return c;
        }

        JPanel recentCard(){
            JPanel c=card(); c.setLayout(new BorderLayout(0,8));
            JLabel t=lbl("Recent Sales",FH3,TXT); t.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
            c.add(t,BorderLayout.NORTH);
            JPanel list=new JPanel(); list.setBackground(CARD); list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
            if(sales.isEmpty()){ list.add(lbl("No transactions yet.",FRG,GRY)); }
            else{
                List<Sale> rec=new ArrayList<>(sales.subList(Math.max(0,sales.size()-6),sales.size()));
                Collections.reverse(rec);
                for(Sale s:rec){
                    JPanel r=new JPanel(new BorderLayout(8,0)); r.setBackground(CARD);
                    r.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
                    JPanel lp=new JPanel(new GridLayout(2,1,0,2)); lp.setBackground(CARD);
                    lp.add(lbl("#"+s.no+"  "+s.customer,FBD,TXT));
                    lp.add(lbl(s.date.substring(0,16),FSM,GRY));
                    r.add(lp,BorderLayout.WEST); r.add(lbl("Rs."+fmt(s.total),FBD,GRN),BorderLayout.EAST);
                    r.setMaximumSize(new Dimension(Integer.MAX_VALUE, r.getPreferredSize().height));
                    list.add(r); list.add(new JSeparator());
                }
            }
            JPanel wrap=new JPanel(new BorderLayout()); wrap.setBackground(CARD);
            wrap.add(list, BorderLayout.NORTH);
            JScrollPane sp=new JScrollPane(wrap); sp.setBorder(null); sp.getViewport().setBackground(CARD);
            c.add(sp,BorderLayout.CENTER); return c;
        }
    }

    // ── Products Panel ────────────────────────────────────────────────
    static class ProdPanel extends JPanel {
        DefaultTableModel tm; JTable tbl; JTextField srch; JComboBox<String> catBox;

        ProdPanel(){setBackground(BG);build();}

        void build(){
            removeAll(); setLayout(new BorderLayout(0,0));
            setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            // Header
            JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG);
            hdr.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
            hdr.add(lbl("Products",FH,TXT),BorderLayout.WEST);
            
            JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            rightBtns.setBackground(BG);
            JButton addCatBtn = btn("+ Add Category", new Color(124,58,237));
            addCatBtn.addActionListener(e -> {
                String newCat = JOptionPane.showInputDialog(this, "Enter new category name:");
                if (newCat != null && !newCat.trim().isEmpty()) {
                    customCategories.add(newCat.trim());
                    refresh();
                    mainWin.refreshAll();
                }
            });
            JButton addBtn=btn("+ Add Product",ACC); addBtn.addActionListener(e->dlg(null));
            rightBtns.add(addCatBtn); rightBtns.add(addBtn);
            
            hdr.add(rightBtns,BorderLayout.EAST);
            add(hdr,BorderLayout.NORTH);

            // Toolbar
            JPanel tb=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); tb.setBackground(BG);
            tb.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
            srch=fld(""); srch.setPreferredSize(new Dimension(200,30));
            srch.getDocument().addDocumentListener((SimpleDocListener)e->filter());
            catBox=new JComboBox<>(); catBox.setFont(FRG); catBox.setPreferredSize(new Dimension(130,30));
            catBox.addItem("All");
            for(String c:getCategories()) catBox.addItem(c);
            catBox.addActionListener(e->filter());
            tb.add(lbl("Search:",FBD,TXT)); tb.add(srch);
            tb.add(Box.createHorizontalStrut(10));
            tb.add(lbl("Category:",FBD,TXT)); tb.add(catBox);
            long lw=products.stream().filter(Product::low).count();
            if(lw>0) tb.add(lbl("  "+lw+" low stock items",FSM,RED));

            // Table
            String[] cols={"ID","Product Name","Category","Barcode","Buy Price","Sell Price","GST%","Stock","Min Qty","Inv Value","Status"};
            tm=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            tbl=new JTable(tm); styleTable(tbl);
            tbl.getColumnModel().getColumn(0).setMaxWidth(40);
            tbl.getColumnModel().getColumn(2).setPreferredWidth(100);
            tbl.getColumnModel().getColumn(3).setPreferredWidth(90);
            tbl.getColumnModel().getColumn(6).setMaxWidth(60);
            tbl.getColumnModel().getColumn(9).setPreferredWidth(80);
            tbl.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
                @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                    Component co=super.getTableCellRendererComponent(t,v,s,f,r,c);
                    co.setBackground(s?new Color(238,242,255):(r%2==0?CARD:LGRY));
                    String sv=v!=null?v.toString():"";
                    if(c==9) co.setForeground("Low Stock".equals(sv)?RED:GRN);
                    else co.setForeground(c==3||c==4||c==8?ACC:TXT);
                    ((JLabel)co).setHorizontalAlignment(c==0||c==6||c==7?SwingConstants.CENTER:c>=3&&c!=9?SwingConstants.RIGHT:SwingConstants.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0,8,0,8)); return co;
                }
            });
            populate(products);

            // Action buttons
            JPanel ab=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); ab.setBackground(BG);
            ab.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
            JButton eb=btn("Edit",new Color(59,130,246));
            JButton db=btn("Delete",RED);
            JButton rb=btn("Update Stock",GRN);
            eb.addActionListener(e->{int r=tbl.getSelectedRow();if(r<0){alert("Select a product first.");return;}
                int id=(int)tm.getValueAt(r,0);products.stream().filter(x->x.id==id).findFirst().ifPresent(p->dlg(p));});
            db.addActionListener(e->{int r=tbl.getSelectedRow();if(r<0){alert("Select a product first.");return;}
                int id=(int)tm.getValueAt(r,0);
                if(JOptionPane.showConfirmDialog(this,"Delete this product?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
                    products.removeIf(x->x.id==id);refresh();mainWin.refreshAll();}});
            rb.addActionListener(e->{int r=tbl.getSelectedRow();if(r<0){alert("Select a product first.");return;}
                int id=(int)tm.getValueAt(r,0);
                products.stream().filter(x->x.id==id).findFirst().ifPresent(p->{
                    JTextField qf=fld(String.valueOf(p.qty));
                    if(JOptionPane.showConfirmDialog(this,new Object[]{"New quantity for "+p.name+":",qf},"Update Stock",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
                        try{p.qty=Integer.parseInt(qf.getText().trim());refresh();mainWin.refreshAll();}
                        catch(NumberFormatException ex){alert("Enter a valid number.");}
                    }});});
            ab.add(eb); ab.add(db); ab.add(rb);

            JPanel center=new JPanel(new BorderLayout()); center.setBackground(BG);
            JPanel tableArea=new JPanel(new BorderLayout()); tableArea.setBackground(BG);
            tableArea.add(tb,BorderLayout.NORTH);
            tableArea.add(scroll(tbl),BorderLayout.CENTER);
            tableArea.add(ab,BorderLayout.SOUTH);
            center.add(tableArea,BorderLayout.CENTER);
            add(center,BorderLayout.CENTER);
        }

        void filter(){
            String q=srch.getText().toLowerCase(); 
            String cat=(String)catBox.getSelectedItem();
            if(cat==null) cat="All";
            final String finalCat = cat;
            populate(products.stream()
                .filter(p->q.isEmpty()||p.name.toLowerCase().contains(q)||p.cat.toLowerCase().contains(q)||(p.barcode!=null&&p.barcode.toLowerCase().contains(q)))
                .filter(p->"All".equals(finalCat)||p.cat.equals(finalCat)).collect(Collectors.toList()));
        }
        void populate(List<Product> list){
            tm.setRowCount(0);
            for(Product p:list) tm.addRow(new Object[]{p.id,p.name,p.cat,p.barcode==null?"-":p.barcode,fmt(p.buyPrice),fmt(p.sellPrice),p.gstRate+"%",p.qty,p.min,fmt(p.val()),p.low()?"Low Stock":"OK"});
        }
        void dlg(Product ex){
            JDialog d=new JDialog(); d.setTitle(ex==null?"Add Product":"Edit Product");
            d.setModal(true); d.setSize(420,440); d.setLocationRelativeTo(null);
            JPanel p=new JPanel(new GridBagLayout()); p.setBackground(CARD);
            p.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
            GridBagConstraints g=new GridBagConstraints(); g.fill=GridBagConstraints.HORIZONTAL; g.insets=new Insets(5,5,5,5);
            g.gridx=0;g.gridy=0;g.gridwidth=2;
            p.add(lbl(ex==null?"Add New Product":"Edit Product",new Font("Segoe UI",Font.BOLD,16),TXT),g);
            g.gridwidth=1;
            JTextField nf=fld(ex!=null?ex.name:"");
            JComboBox<String> cf=new JComboBox<>();
            for(String c:getCategories()) cf.addItem(c);
            cf.setFont(FRG); 
            if(ex!=null) {
                if (((DefaultComboBoxModel<String>)cf.getModel()).getIndexOf(ex.cat) == -1) cf.insertItemAt(ex.cat, 0);
                cf.setSelectedItem(ex.cat);
            }
            JTextField bcf=fld(ex!=null&&ex.barcode!=null?ex.barcode:"");
            JTextField bpf=fld(ex!=null?fmt(ex.buyPrice).replace(",",""):"");
            JTextField spf=fld(ex!=null?fmt(ex.sellPrice).replace(",",""):"");
            JTextField gtf=fld(ex!=null?String.valueOf(ex.gstRate):"18.0");
            JTextField qf=fld(ex!=null?String.valueOf(ex.qty):"");
            JTextField mf=fld(ex!=null?String.valueOf(ex.min):"5");
            Object[][] rows={{"Name:",nf},{"Category:",cf},{"Barcode:",bcf},{"Buy Price:",bpf},{"Sell Price:",spf},{"GST Rate %:",gtf},{"Stock Qty:",qf},{"Min Stock:",mf}};
            for(int i=0;i<rows.length;i++){
                g.gridx=0;g.gridy=i+1;g.weightx=0.3; p.add(lbl((String)rows[i][0],FBD,TXT),g);
                g.gridx=1;g.weightx=0.7; p.add((Component)rows[i][1],g);
            }
            JPanel bp=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); bp.setBackground(CARD);
            JButton can=btn("Cancel",GRY), sav=btn(ex==null?"Add":"Save",ACC);
            can.addActionListener(e->d.dispose());
            sav.addActionListener(e->{
                try{
                    String n=nf.getText().trim(); if(n.isEmpty()){alert("Name is required!");return;}
                    String cat = (String)cf.getSelectedItem();
                    String bc1=bcf.getText().trim(); if(bc1.isEmpty()) bc1=null;
                    double bp1=Double.parseDouble(bpf.getText().trim());
                    double sp1=Double.parseDouble(spf.getText().trim());
                    double gt1=Double.parseDouble(gtf.getText().trim());
                    int q2=Integer.parseInt(qf.getText().trim()), m2=Integer.parseInt(mf.getText().trim());
                    if(bp1<0||sp1<0||gt1<0||q2<0||m2<0){alert("Values cannot be negative.");return;}
                    if(ex==null) products.add(new Product(PID++,n,cat,bc1,bp1,sp1,gt1,q2,m2));
                    else{ex.name=n;ex.cat=cat;ex.barcode=bc1;ex.buyPrice=bp1;ex.sellPrice=sp1;ex.gstRate=gt1;ex.qty=q2;ex.min=m2;}
                    d.dispose(); refresh(); mainWin.refreshAll();
                }catch(NumberFormatException ex2){alert("Enter valid numbers for prices/quantity.");}
            });
            bp.add(can); bp.add(sav);
            g.gridx=0;g.gridy=rows.length+1;g.gridwidth=2;g.insets=new Insets(12,5,5,5); p.add(bp,g);
            d.add(p); d.setVisible(true);
        }
        void refresh(){build();revalidate();repaint();}
    }

    // ── Billing Panel ─────────────────────────────────────────────────
    static class BillPanel extends JPanel {
        List<BillItem>           cart  =new ArrayList<>();
        DefaultTableModel        cartTM;
        JTable                   cartTbl;
        JTextField               custFld,srch;
        JLabel                   subLbl,gstLbl,totLbl;
        DefaultListModel<String> prodLM=new DefaultListModel<>();
        JList<String>            prodList;

        BillPanel(){setBackground(BG);build();}

        void build(){
            removeAll(); setLayout(new BorderLayout(0,0));
            setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            // Header
            JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG);
            hdr.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
            hdr.add(lbl("Billing",FH,TXT),BorderLayout.WEST);
            hdr.add(lbl("Pick products  \u2192  Set qty  \u2192  Generate PDF",FSM,GRY),BorderLayout.EAST);
            add(hdr,BorderLayout.NORTH);

            // Two column layout
            JPanel main=new JPanel(new GridLayout(1,2,12,0)); main.setBackground(BG);
            main.add(leftPanel()); main.add(rightPanel());
            add(main,BorderLayout.CENTER);
        }

        JPanel leftPanel(){
            JPanel c=card(); c.setLayout(new BorderLayout(0,8));
            c.add(lbl("Product List",FH3,TXT),BorderLayout.NORTH);

            srch=fld(""); srch.putClientProperty("JTextField.placeholderText","Search...");
            srch.getDocument().addDocumentListener((SimpleDocListener)e->refreshProd());

            prodList=new JList<>(prodLM); prodList.setFont(FRG); prodList.setBackground(LGRY);
            prodList.setFixedCellHeight(40); prodList.setSelectionBackground(new Color(238,242,255));
            prodList.setCellRenderer(new DefaultListCellRenderer(){
                @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean s,boolean f){
                    String[] pts=((String)v).split("\\|");
                    JPanel cell=new JPanel(new BorderLayout(6,0));
                    cell.setBackground(s?new Color(238,242,255):(i%2==0?CARD:LGRY));
                    cell.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0,0,1,0,BRD),
                        BorderFactory.createEmptyBorder(6,10,6,10)));
                    cell.add(lbl(pts.length>1?pts[1].trim():((String)v),FBD,s?ACC:TXT),BorderLayout.WEST);
                    cell.add(lbl(pts.length>2?pts[2].trim():"",FSM,GRY),BorderLayout.EAST);
                    return cell;
                }
            });
            refreshProd();

            JPanel addRow=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); addRow.setBackground(CARD);
            JTextField qf=fld("1"); qf.setPreferredSize(new Dimension(50,28));
            JButton addBtn=btn("Add to Cart",ACC);
            addBtn.addActionListener(e->{
                String sel=prodList.getSelectedValue(); if(sel==null){alert("Select a product.");return;}
                try{
                    int q=Integer.parseInt(qf.getText().trim()); if(q<=0){alert("Qty must be > 0");return;}
                    int id=Integer.parseInt(sel.split("\\|")[0].trim());
                    products.stream().filter(x->x.id==id).findFirst().ifPresent(pr->{
                        int inCart=cart.stream().filter(ci->ci.p.id==pr.id).mapToInt(ci->ci.qty).sum();
                        if(inCart+q>pr.qty){alert("Not enough stock! Available: "+(pr.qty-inCart));return;}
                        boolean found=false;
                        for(BillItem bi:cart)if(bi.p.id==pr.id){bi.qty+=q;found=true;break;}
                        if(!found)cart.add(new BillItem(pr,q));
                        updateCart(); qf.setText("1");
                    });
                }catch(NumberFormatException ex){alert("Invalid quantity!");}
            });
            addRow.add(lbl("Qty:",FBD,TXT)); addRow.add(qf); addRow.add(addBtn);

            JPanel mid=new JPanel(new BorderLayout(0,6)); mid.setBackground(CARD);
            mid.add(srch,BorderLayout.NORTH); mid.add(scroll(prodList),BorderLayout.CENTER); mid.add(addRow,BorderLayout.SOUTH);
            c.add(mid,BorderLayout.CENTER); return c;
        }

        JPanel rightPanel(){
            JPanel c=card(); c.setLayout(new BorderLayout(0,10));
            c.add(lbl("Cart & Invoice",FH3,TXT),BorderLayout.NORTH);

            // Customer name — highlighted box
            JPanel custBox=new JPanel(new BorderLayout(6,0)); custBox.setBackground(new Color(240,242,255));
            custBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0,3,0,0,ACC),
                BorderFactory.createEmptyBorder(8,10,8,10)));
            custBox.add(lbl("Customer Name:",FBD,ACC),BorderLayout.WEST);
            custFld=fld(""); custFld.putClientProperty("JTextField.placeholderText","Enter customer name (required)...");
            custFld.setBackground(Color.WHITE); custBox.add(custFld,BorderLayout.CENTER);

            // Cart table
            cartTM=new DefaultTableModel(new String[]{"Product","Unit Price","Qty","GST","Subtotal"},0){
                public boolean isCellEditable(int r,int c){return false;}};
            cartTbl=new JTable(cartTM); styleTable(cartTbl); cartTbl.setRowHeight(32);
            cartTbl.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
                @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                    Component co=super.getTableCellRendererComponent(t,v,s,f,r,c);
                    co.setBackground(s?new Color(238,242,255):(r%2==0?CARD:LGRY)); co.setForeground(c>=1?ACC:TXT);
                    ((JLabel)co).setHorizontalAlignment(c==0?SwingConstants.LEFT:SwingConstants.RIGHT);
                    setBorder(BorderFactory.createEmptyBorder(0,8,0,8)); return co;
                }
            });
            JButton rmv=btn("Remove",RED); rmv.setFont(FSM);
            rmv.addActionListener(e->{int r=cartTbl.getSelectedRow();if(r>=0&&r<cart.size()){cart.remove(r);updateCart();}});
            JPanel rr=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,4)); rr.setBackground(CARD); rr.add(rmv);
            JPanel cartWrap=new JPanel(new BorderLayout()); cartWrap.setBackground(CARD);
            cartWrap.add(scroll(cartTbl),BorderLayout.CENTER); cartWrap.add(rr,BorderLayout.SOUTH);

            // Totals block
            JPanel tots=new JPanel(new GridLayout(3,2,4,5)); tots.setBackground(new Color(248,250,252));
            tots.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1,0,0,0,BRD),BorderFactory.createEmptyBorder(10,6,10,6)));
            subLbl=lbl("Rs. 0.00",FBD,GRY);  subLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            gstLbl=lbl("Rs. 0.00",FBD,YEL);  gstLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            totLbl=lbl("Rs. 0.00",new Font("Segoe UI",Font.BOLD,16),GRN); totLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            tots.add(lbl("Subtotal:",FBD,GRY));   tots.add(subLbl);
            tots.add(lbl("Total GST:",FBD,YEL));  tots.add(gstLbl);
            tots.add(lbl("TOTAL:",FBD,TXT));       tots.add(totLbl);

            JPanel btnRow=new JPanel(new GridLayout(1,2,8,0)); btnRow.setBackground(CARD);
            JButton clr=btn("Clear Cart",GRY), gen=btn("Generate PDF Invoice",GRN);
            clr.addActionListener(e->{cart.clear();updateCart();});
            gen.addActionListener(e->generateBill());
            btnRow.add(clr); btnRow.add(gen);

            JPanel bot=new JPanel(new BorderLayout(0,6)); bot.setBackground(CARD);
            bot.add(tots,BorderLayout.CENTER); bot.add(btnRow,BorderLayout.SOUTH);

            JPanel center=new JPanel(new BorderLayout(0,8)); center.setBackground(CARD);
            center.add(custBox,BorderLayout.NORTH);
            center.add(cartWrap,BorderLayout.CENTER);
            center.add(bot,BorderLayout.SOUTH);
            c.add(center,BorderLayout.CENTER); return c;
        }

        void refreshProd(){
            prodLM.clear(); String q=srch!=null?srch.getText().toLowerCase():"";
            for(Product p:products) {
                if (p.qty <= 0) continue;
                if(q.isEmpty()||p.name.toLowerCase().contains(q)||(p.barcode!=null&&p.barcode.toLowerCase().contains(q)))
                    prodLM.addElement(p.id+" | "+p.name+" | Rs."+fmt(p.sellPrice)+"   Stock:"+p.qty);
            }
        }
        void updateCart(){
            cartTM.setRowCount(0);
            for(BillItem bi:cart) cartTM.addRow(new Object[]{bi.p.name,"Rs."+fmt(bi.p.sellPrice),bi.qty, bi.p.gstRate+"% (Rs."+fmt(bi.gst())+")", "Rs."+fmt(bi.sub())});
            double s=cart.stream().mapToDouble(BillItem::sub).sum(), g=cart.stream().mapToDouble(BillItem::gst).sum(), t=s+g;
            subLbl.setText("Rs. "+fmt(s)); gstLbl.setText("Rs. "+fmt(g)); totLbl.setText("Rs. "+fmt(t));
        }
        void generateBill(){
            if(cart.isEmpty()){alert("Cart is empty!");return;}
            String cust=custFld.getText().trim();
            if(cust.isEmpty()){alert("Please enter the customer name!");custFld.requestFocus();return;}
            double s=cart.stream().mapToDouble(BillItem::sub).sum(), g=cart.stream().mapToDouble(BillItem::gst).sum(), t=s+g;
            double pr=cart.stream().mapToDouble(BillItem::profit).sum();
            String no = String.valueOf(sales.size() + 1);
            String date=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for(BillItem bi:cart) bi.p.qty-=bi.qty;
            Sale sale=new Sale(no,date,cust,new ArrayList<>(cart),s,g,t,pr);
            sales.add(sale);
            File invDir = new File("invoices");
            if(!invDir.exists()) invDir.mkdir();
            String defaultName = no;
            String customName = JOptionPane.showInputDialog(this, "Enter custom name for invoice:", defaultName);
            if (customName == null || customName.trim().isEmpty()) customName = defaultName;
            if (!customName.toLowerCase().endsWith(".pdf")) customName += ".pdf";
            sale.invoiceFile = customName;
            savePDF("invoices" + File.separator + customName, sale);
            cart.clear(); updateCart(); custFld.setText(""); refreshProd(); mainWin.refreshAll();
        }
    }

    // ── Sales History Panel ───────────────────────────────────────────
    static class SalePanel extends JPanel {
        DefaultTableModel tm; JTable tbl;
        SalePanel(){setBackground(BG);refresh();}

        void refresh(){
            removeAll(); setLayout(new BorderLayout(0,0));
            setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

            // Header
            JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(BG);
            hdr.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
            hdr.add(lbl("Sales History",FH,TXT),BorderLayout.WEST);
            double rev=sales.stream().mapToDouble(s->s.total).sum();
            double prof=sales.stream().mapToDouble(s->s.profit).sum();
            JPanel rt=new JPanel(new FlowLayout(FlowLayout.RIGHT,16,0)); rt.setBackground(BG);
            rt.add(lbl("Total Profit: Rs."+fmt(prof),FH3,new Color(16,185,129)));
            rt.add(lbl("Total Revenue: Rs."+fmt(rev),FH3,GRN));
            hdr.add(rt,BorderLayout.EAST);
            add(hdr,BorderLayout.NORTH);

            // Table
            String[] cols={"Bill No","Date & Time","Customer","Items","Subtotal","Total GST","Grand Total","Profit"};
            tm=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
            for(Sale s:sales) tm.addRow(new Object[]{"#"+s.no,s.date,s.customer,s.items.size()+" items","Rs."+fmt(s.sub),"Rs."+fmt(s.gst),"Rs."+fmt(s.total),"Rs."+fmt(s.profit)});
            tbl=new JTable(tm); styleTable(tbl);
            tbl.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){
                @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                    Component co=super.getTableCellRendererComponent(t,v,s,f,r,c);
                    co.setBackground(s?new Color(238,242,255):(r%2==0?CARD:LGRY));
                    if(c==7) co.setForeground(new Color(16,185,129));
                    else co.setForeground(c==6?GRN:c>=4?ACC:TXT);
                    ((JLabel)co).setHorizontalAlignment(c>=4?SwingConstants.RIGHT:SwingConstants.LEFT);
                    setBorder(BorderFactory.createEmptyBorder(0,8,0,8)); return co;
                }
            });

            // Action
            JButton vb=btn("Open PDF Invoice",ACC); vb.addActionListener(e->{
                int r=tbl.getSelectedRow(); if(r<0){alert("Select a sale to view.");return;}
                Sale s=sales.get(r); 
                if (s.invoiceFile == null || s.invoiceFile.isEmpty()) {
                    String defaultName = s.no;
                    String customName = JOptionPane.showInputDialog(this, "Enter custom name for invoice:", defaultName);
                    if (customName == null || customName.trim().isEmpty()) customName = defaultName;
                    if (!customName.toLowerCase().endsWith(".pdf")) customName += ".pdf";
                    s.invoiceFile = customName;
                }
                String fileName = "invoices" + File.separator + s.invoiceFile;
                File f = new File(fileName);
                try{
                    if(f.exists()){Desktop.getDesktop().open(f);}
                    else {
                        File invDir = new File("invoices");
                        if (!invDir.exists()) invDir.mkdir();
                        savePDF(fileName,s);
                    }
                }catch(Exception ex){savePDF(fileName,s);}
            });
            JPanel ab=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); ab.setBackground(BG);
            ab.setBorder(BorderFactory.createEmptyBorder(8,0,0,0)); ab.add(vb);

            JPanel center=new JPanel(new BorderLayout()); center.setBackground(BG);
            center.add(scroll(tbl),BorderLayout.CENTER); center.add(ab,BorderLayout.SOUTH);
            add(center,BorderLayout.CENTER); revalidate(); repaint();
        }
    }

    // ── Functional DocumentListener helper ────────────────────────────
    @FunctionalInterface
    interface SimpleDocListener extends DocumentListener {
        void update(DocumentEvent e);
        default void insertUpdate (DocumentEvent e){update(e);}
        default void removeUpdate (DocumentEvent e){update(e);}
        default void changedUpdate(DocumentEvent e){update(e);}
    }
}
