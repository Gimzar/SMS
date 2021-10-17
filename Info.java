package ua.gotsman.sms;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Информация о клиенте и услуге, исходя из сформированного коммерческого предложения 1С.
 */
class Info {

    private String firstName;              //Имя клиента
    private String lastName;               //Фамилия клиента
    private String phoneNumber;            //Телефонный номер
    private String responsible;            //ФИО мастера
    private String responsiblePhoneNumber; //Номер телефона мастера
    private String proposal;               //Коммерческое предложение номер и дата
    private ArrayList<String> service;     //Список с наименованиями услуг
    private ArrayList<Double> amount;      //Список с количеством услуг
    private ArrayList<Double> price;       //Список с ценами за услуги
    private double total;                  //Общая сумма

    String getFirstName() {
        return firstName;
    }

    private void setFirstName(Cell cell) {
        String[] temp = cell.getContents().split(" ");
        this.firstName = temp[1];
    }

    String getLastName() {
        return lastName;
    }

    private void setLastName(Cell cell) {
        String[] temp = cell.getContents().split(" ");
        this.lastName = temp[0];
    }

    String getPhoneNumber() {
        return phoneNumber;
    }

    private void setPhoneNumber(Cell cell) {
        String temp = cell.getContents();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(temp);
        StringBuilder sb = new StringBuilder();
        int start = 0;
        while (matcher.find(start)) {
            sb.append(temp.substring(matcher.start(), matcher.end()));
            start = matcher.end();
        }
        if (sb.toString().startsWith("0")) {
            this.phoneNumber = sb.toString().substring(0, 10);
        } else if (sb.toString().startsWith("8")) {
            this.phoneNumber = sb.toString().substring(1, 11);
        } else {
            if (sb.toString().charAt(5) == '8') {
                this.phoneNumber = sb.toString().substring(6, 16);
            } else {
                this.phoneNumber = sb.toString().substring(5, 15);
            }
        }
    }

    String getProposal() {
        return proposal;
    }

    private void setProposal(Cell cell) {
        this.proposal = cell.getContents().trim();
    }

    ArrayList<String> getService() {
        return service;
    }

    private void setService(ArrayList<String> service, Cell cell) {
        this.service = service;
        service.add(cell.getContents().trim());
    }

    ArrayList<Double> getAmount() {
        return amount;
    }

    private void setAmount(ArrayList<Double> amount, Cell cell) {
        this.amount = amount;
        amount.add(Double.valueOf(cell.getContents().replaceAll(",", ".")));
    }

    private ArrayList<Double> getPrice() {
        return price;
    }

    private void setPrice(ArrayList<Double> price, Cell cell) {
        this.price = price;
        price.add(Double.valueOf(cell.getContents().replaceAll(",", ".")));
    }

    double getTotal() {
        for (Double d : getPrice()) {
            total += d;
        }
        return total;
    }

    String getResponsible() {
        return responsible;
    }

    private void setResponsible(Cell cell) {
        String[] temp = cell.getContents().split(" ");
        this.responsible = temp[1] + " " + temp[2];
    }

    String getResponsiblePhoneNumber() {
        return responsiblePhoneNumber;
    }

    private void setResponsiblePhoneNumber(Cell cell) {
        String[] temp = cell.getContents().split(" ");
        this.responsiblePhoneNumber = temp[temp.length - 1];
    }

    void loadFromXls(InputStream inputStream) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(inputStream);
        Sheet sheet = workbook.getSheet(0);
        setFirstName(sheet.getCell(2, 1));
        setLastName(sheet.getCell(2, 1));
        setPhoneNumber(sheet.getCell(2, 2));
        setProposal(sheet.getCell(1, 6));
        service = new ArrayList<>();
        amount = new ArrayList<>();
        price = new ArrayList<>();
        Cell[] cells = sheet.getColumn(0);
        for (Cell c : cells) {
            if (c.getContents().matches("\\d+")) {
                setService(service, sheet.getCell(2, c.getRow()));
                setAmount(amount, sheet.getCell(6, c.getRow()));
                setPrice(price, sheet.getCell(8, c.getRow()));
            }
        }
        int lastRow = sheet.getRows() - 1;
        setResponsible(sheet.getCell(3, lastRow));
        setResponsiblePhoneNumber(sheet.getCell(3, lastRow));
    }
}
