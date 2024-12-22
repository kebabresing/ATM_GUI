import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class ATM {
    private double saldo;
    private String pin;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final List<String[]> riwayatTransaksi = new ArrayList<>();

    public ATM(double saldoAwal) {
        this.saldo = saldoAwal;
        this.pin = "1234";
    }

    public boolean login(String pin) {
        return this.pin.equals(pin);
    }

    public void logout() {}

    public void cekSaldo() {
        String saldoFormatted = currencyFormatter.format(saldo);
        JOptionPane.showMessageDialog(null, "Saldo Anda " + saldoFormatted, "Saldo", JOptionPane.INFORMATION_MESSAGE);
        riwayatTransaksi.add(new String[]{"Cek Saldo", "-", saldoFormatted});
    }

    public void setorTunai(double jumlah) {
        if (jumlah > 0) {
            saldo += jumlah;
            String saldoFormatted = currencyFormatter.format(saldo);
            JOptionPane.showMessageDialog(null, "Setor tunai berhasil. Saldo baru " + saldoFormatted, "Info", JOptionPane.INFORMATION_MESSAGE);
            riwayatTransaksi.add(new String[]{"Setor Tunai", currencyFormatter.format(jumlah), saldoFormatted});
        } else {
            JOptionPane.showMessageDialog(null, "Jumlah setor tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void tarikTunai(double jumlah) {
        if (jumlah > 0 && jumlah <= saldo) {
            saldo -= jumlah;
            String saldoFormatted = currencyFormatter.format(saldo);
            JOptionPane.showMessageDialog(null, "Penarikan tunai berhasil. Saldo baru " + saldoFormatted, "Info", JOptionPane.INFORMATION_MESSAGE);
            riwayatTransaksi.add(new String[]{"Tarik Tunai", currencyFormatter.format(jumlah), saldoFormatted});
        } else {
            JOptionPane.showMessageDialog(null, "Jumlah tarik tidak valid atau saldo tidak mencukupi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<String[]> getRiwayatTransaksi() {
        return riwayatTransaksi;
    }

    public void simpanTransaksiKeFile() {
        try (FileWriter file = new FileWriter("riwayat_transaksi.json")) {
            file.write("[");
            for (int i = 0; i < riwayatTransaksi.size(); i++) {
                String[] transaksi = riwayatTransaksi.get(i);
                file.write(String.format("{\"jenis\":\"%s\",\"jumlah\":\"%s\",\"saldo\":\"%s\"}", transaksi[0], transaksi[1], transaksi[2]));
                if (i < riwayatTransaksi.size() - 1) file.write(",");
            }
            file.write("]");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan riwayat transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

public class Main extends JFrame {
    private ATM atm;
    private JPasswordField pinField;
    private JButton loginButton;
    private JButton exitButton;
    private JPanel panel;

    private void centerFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((screenSize.getWidth() - this.getWidth()) / 2);
        int y = (int) ((screenSize.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
    }

    public Main() {
        atm = new ATM(1000000);

        panel = new JPanel();
        pinField = new JPasswordField(20);
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");

        panel.add(new JLabel("Masukkan PIN Anda:"));
        panel.add(pinField);
        panel.add(loginButton);
        panel.add(exitButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExit();
            }
        });

        this.add(panel);
        this.setSize(300, 150);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        centerFrame();
        this.setVisible(true);
    }

    private void handleLogin() {
        String pin = new String(pinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PIN tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            if (atm.login(pin)) {
                JOptionPane.showMessageDialog(this, "Login berhasil!", "Info", JOptionPane.INFORMATION_MESSAGE);
                displayMenu();
            } else {
                JOptionPane.showMessageDialog(this, "PIN salah. Silakan coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin keluar?", "Konfirmasi Keluar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            atm.simpanTransaksiKeFile();
            System.exit(0);
        }
    }

    private void displayMenu() {
        String[] options = {"Cek Saldo", "Setor Tunai", "Tarik Tunai", "Lihat Riwayat", "Logout"};
        int pilihan = JOptionPane.showOptionDialog(this, "Pilih opsi:", "Menu", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        switch (pilihan) {
            case 0:
                atm.cekSaldo();
                displayMenu();
                break;
            case 1:
                String setorStr = JOptionPane.showInputDialog(this, "Masukkan jumlah setor:", "Setor Tunai", JOptionPane.QUESTION_MESSAGE);
                if (setorStr != null && !setorStr.isEmpty()) {
                    try {
                        double setor = Double.parseDouble(setorStr);
                        atm.setorTunai(setor);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Jumlah setor tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                displayMenu();
                break;
            case 2:
                String tarikStr = JOptionPane.showInputDialog(this, "Masukkan jumlah tarik:", "Tarik Tunai", JOptionPane.QUESTION_MESSAGE);
                if (tarikStr != null && !tarikStr.isEmpty()) {
                    try {
                        double tarik = Double.parseDouble(tarikStr);
                        atm.tarikTunai(tarik);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Jumlah tarik tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                displayMenu();
                break;
            case 3:
                tampilkanRiwayatTransaksi();
                displayMenu();
                break;
            case 4:
                atm.logout();
                JOptionPane.showMessageDialog(this, "Logout berhasil!", "Info", JOptionPane.INFORMATION_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Opsi tidak valid. Silakan coba lagi.", "Error", JOptionPane.ERROR_MESSAGE);
                displayMenu();
        }
    }

    private void tampilkanRiwayatTransaksi() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Jenis Transaksi", "Jumlah", "Saldo"}, 0);
        atm.getRiwayatTransaksi().forEach(model::addRow);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);

        JOptionPane.showMessageDialog(this, new JScrollPane(table), "Riwayat Transaksi", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new Main();
    }
}
