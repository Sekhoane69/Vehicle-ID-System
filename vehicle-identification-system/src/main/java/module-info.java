module com.vis {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires transitive java.sql;
    requires jbcrypt;

    opens com.vis to javafx.fxml;
    opens com.vis.controller to javafx.fxml;
    opens com.vis.model to javafx.fxml;

    exports com.vis;
    exports com.vis.controller;
    exports com.vis.model;
    exports com.vis.dao;
    exports com.vis.util;
}
