package ua.gotsman.sms;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ua.smsc.sys.soap.Send;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Controller {

    public Button start;
    public Button stop;
    public Button log;
    public Button accept;
    public Button cancel;
    public TextArea mainTextArea;
    public TextField balanceField;
    public TextField smsCount;
    public Hyperlink siteLink;
    public TextField enterPhone;
    public TextArea enterSms;
    public CheckBox checkBox;
    private String phoneNumber;
    private String message;
    private Sms sms = new Sms();

    @FXML
    void btnStart() throws InterruptedException, IOException {
        checkBox.setSelected(true);
        Task showStatus = new Task<TextArea>() {
            @Override
            protected TextArea call() throws Exception {
                int count = 1;
                while (Sms.stopTime == 0) {
                    if (count < Sms.smsCount) {
                        Thread.sleep(30000);
                        mainTextArea.appendText(sms.showMainInfo());
                        showBalance();
                        smsCount();
                        count++;
                    }
                    Thread.sleep(60000);
                }
                return mainTextArea;
            }
        };
        Thread thread = new Thread(sms);
        Thread thread1 = new Thread(showStatus);
        thread.start();
        thread1.start();
    }

    @FXML
    void btnStop() {
        Sms.stopTime = 1;
        checkBox.setSelected(false);
    }

    @FXML
    void btnHyperLink() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("http://sts.zp.ua"));
    }

    @FXML
    void btnLog() throws IOException {
        Desktop.getDesktop().open(new File("D:\\SMS\\history.txt"));
    }

    @FXML
    private void showBalance() {
        balanceField.setText("Баланс: " + sms.getUserBalance() + " грн.");
    }

    @FXML
    private void smsCount() {
        smsCount.setText("СМС отправлено: " + String.valueOf(Sms.smsCount - 1));
    }

    @FXML
    void accept() {
        phoneNumber = enterPhone.getText();
        message = enterSms.getText();
        sendSms();
        smsCount();
        showBalance();
        enterPhone.clear();
        enterSms.clear();
    }

    @FXML
    void cancel() {
        enterPhone.clear();
        enterSms.clear();
    }

    private void sendSms() {
        Send send = new Send();
        send.setLogin(Sms.getLogin());
        send.setPsw(Sms.getPassword());
        send.setPhones("+38" + phoneNumber);
        send.setMes(message);
        send.setSender("SoftTechno");
        send.setTime("0");
        sms.getPort().sendSms(send);
    }
}
