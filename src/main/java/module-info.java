module com.carlosh.nesemulator {
  requires javafx.controls;
  requires javafx.fxml;

  requires java.desktop;
  requires java.net.http;
  requires javafx.swing;

  opens com.carlosh.nesemulator to javafx.fxml;
  exports com.carlosh.nesemulator;
  exports com.carlosh.nesemulator.ui;
  opens com.carlosh.nesemulator.ui to javafx.fxml;
}