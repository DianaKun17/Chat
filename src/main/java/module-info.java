module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires java.sql;


    opens com.example.chat to javafx.fxml;
    exports com.example.chat;

}