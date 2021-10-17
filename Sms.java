package ua.gotsman.sms;

import jxl.read.biff.BiffException;
import ua.smsc.sys.soap.*;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

class Sms implements Runnable {
    private final static String LOGIN = "Severik";
    private final static String PASSWORD = "Derparol12!@";
    private final static Logger log = Logger.getLogger(Sms.class.getName());
    private Path path = Paths.get("Z:\\");
    private Info info;
    private Service service = new Service();
    private ServiceSoap port = service.getServiceSoap();
    volatile static int stopTime = 0;
    volatile static int smsCount = 1;

    @Override
    public void run() {
        try {
            FileHandler handler = new FileHandler("D:\\SMS\\report.txt", 0, 1, true);
            log.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (stopTime == 0) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path, "*xls")) {
                for (Path entry : entries) {
                    InputStream inputStream = new FileInputStream(String.valueOf(entry));
                    info = new Info();
                    info.loadFromXls(inputStream);
                    inputStream.close();
                    sendSms();
                    Files.delete(entry);
                    smsCount++;
                    writeHistory();
                    Thread.sleep(1000);
                }
            } catch (IOException | BiffException e) {
                log.info(e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopTime = 0;
    }

    static String getLogin() {
        return LOGIN;
    }

    static String getPassword() {
        return PASSWORD;
    }

    ServiceSoap getPort() {
        return port;
    }

    String getUserBalance() {
        Balance balance = new Balance();
        balance.setLogin(LOGIN);
        balance.setPsw(PASSWORD);
        BalanceResponse response = port.getBalance(balance);
        return response.getBalanceresult().getBalance();
    }

    private void sendSms() {
        Send send = new Send();
        send.setLogin(LOGIN);
        send.setPsw(PASSWORD);
        send.setId(String.valueOf(smsCount));
        send.setPhones("+38" + info.getPhoneNumber());
        send.setMes("Vashe oborudovanie gotovo, k oplate " + info.getTotal() + " UAH. Spravki po telefonam "
                + info.getResponsiblePhoneNumber() + ", 5-88-80.");
        send.setSender("SoftTechno");
        send.setTime("0");
        port.sendSms(send);
    }

    private void writeHistory() throws IOException {
        FileWriter writer = new FileWriter("D:\\SMS\\history.txt", true);
        LocalDateTime time = LocalDateTime.now();
        writer.write(time.getYear() + "-" + time.getMonth() + "-" + time.getDayOfMonth() + " " + time.getHour() + ":" + time.getMinute() + " " + smsStatus() + "\n");
        writer.write(info.getLastName() + " " + info.getFirstName() + " " + info.getPhoneNumber() + "\n" + info.getProposal() + "\n");
        writer.write("-------------------------------------------------------" + "\n");
        writer.close();
    }

    String showMainInfo() {
        LocalDateTime time = LocalDateTime.now();
        return String.valueOf(time.getYear()) + "-" + time.getMonth() + "-" + time.getDayOfMonth() + " " +
                time.getHour() + ":" + time.getMinute() + "\n" + smsStatus() + "\n" +
                info.getLastName() + " " + info.getFirstName() + " " + info.getPhoneNumber() + "\n" +
                info.getProposal() + "\n" +
                "\n";
    }

    private String smsStatus() {
        Status status = new Status();
        status.setLogin(LOGIN);
        status.setPsw(PASSWORD);
        status.setId(String.valueOf(smsCount - 1));
        status.setPhone("+38" + info.getPhoneNumber());
        status.setAll("1");
        StatusResponse response = port.getStatus(status);
        String text = response.getStatusresult().getStatus();
        String result = "";
        switch (text) {
            case "-3":
                result = "Сообщение не найдено";
                break;
            case "-1":
                result = "Сообщение ожидает отправки";
                break;
            case "0":
                result = "Сообщение передано оператору";
                break;
            case "1":
                result = "Сообщение доставлено";
                break;
            case "3":
                result = "Сообщение просрочено";
                break;
            case "20":
                result = "Сообщение невозможно доставить";
                break;
            case "22":
                result = "Неверный номер";
                break;
            case "23":
                result = "Сообщение запрещено";
                break;
            case "24":
                result = "Недостаточно средств на счету";
                break;
            case "25":
                result = "Недоступный номер";
                break;
        }
        return result;
    }
}
