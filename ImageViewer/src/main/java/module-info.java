module com.example.ailindaca1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens main to javafx.graphics;
    opens com.example.ailindaca1 to javafx.fxml;
    exports com.example.ailindaca1;
    exports main;
}